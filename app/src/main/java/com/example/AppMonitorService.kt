package com.example

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AppMonitorService : Service() {

    companion object {
        const val TARGET_PACKAGE = "com.instagram.android"
        const val CHANNEL_ID = "step_lock_monitor_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "ACTION_START_MONITOR"
        const val ACTION_STOP = "ACTION_STOP_MONITOR"
        const val ACTION_SIMULATE_STEPS = "ACTION_SIMULATE_STEPS"

        fun start(context: Context) {
            val intent = Intent(context, AppMonitorService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, AppMonitorService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var monitorJob: Job? = null
    private var stepSensorManager: StepSensorManager? = null

    override fun onCreate() {
        super.onCreate()
        StepLockRepository.init(this)
        createNotificationChannel()
        stepSensorManager = StepSensorManager(this)
        stepSensorManager?.startListening()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForegroundMonitoring()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_SIMULATE_STEPS -> {
                StepLockRepository.addSteps(100)
                updateNotification()
                return START_STICKY
            }
            else -> {
                startForegroundMonitoring()
            }
        }
        return START_STICKY
    }

    private fun startForegroundMonitoring() {
        StepLockRepository.setServiceRunning(true)
        val initialNotification = buildNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val fgsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
            startForeground(NOTIFICATION_ID, initialNotification, fgsType)
        } else {
            startForeground(NOTIFICATION_ID, initialNotification)
        }

        if (monitorJob == null || monitorJob?.isActive == false) {
            monitorJob = serviceScope.launch {
                while (isActive) {
                    checkForegroundAppAndEnforceLock()
                    delay(1000)
                }
            }
        }
    }

    private fun stopForegroundMonitoring() {
        monitorJob?.cancel()
        monitorJob = null
        stepSensorManager?.stopListening()
        StepLockRepository.setServiceRunning(false)
        StepLockRepository.setInstagramActive(false)
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun checkForegroundAppAndEnforceLock() {
        val currentForegroundApp = getActiveForegroundPackage()
        val isInstagramForeground = currentForegroundApp == TARGET_PACKAGE
        StepLockRepository.setInstagramActive(isInstagramForeground)

        val state = StepLockRepository.state.value

        if (isInstagramForeground) {
            if (state.bankedSeconds <= 0) {
                // Banked time is exhausted or 0 -> Enforce Lock!
                launchLockScreenActivity()
            } else {
                // Instagram is actively being used with valid banked screen time -> deduct 1 second per second
                StepLockRepository.consumeScreenTime(1)
            }
        }

        updateNotification()
    }

    private fun launchLockScreenActivity() {
        try {
            val lockIntent = Intent(applicationContext, LockScreenActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(lockIntent)
        } catch (e: Exception) {
            Log.e("AppMonitorService", "Error launching LockScreenActivity: ${e.message}", e)
        }
    }

    private fun getActiveForegroundPackage(): String? {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return null

        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 10_000 // Query last 10 seconds

        // Primary: Query usage events
        try {
            val events = usageStatsManager.queryEvents(beginTime, endTime)
            var lastEventPackage: String? = null
            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                    event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    lastEventPackage = event.packageName
                }
            }
            if (!lastEventPackage.isNullOrEmpty()) {
                return lastEventPackage
            }
        } catch (e: Exception) {
            Log.w("AppMonitorService", "Error querying events: ${e.message}")
        }

        // Secondary fallback: queryUsageStats
        try {
            val statsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                beginTime,
                endTime
            )
            if (!statsList.isNullOrEmpty()) {
                return statsList.maxByOrNull { it.lastTimeUsed }?.packageName
            }
        } catch (e: Exception) {
            Log.w("AppMonitorService", "Error querying usage stats: ${e.message}")
        }

        return null
    }

    private fun buildNotification(): Notification {
        val state = StepLockRepository.state.value
        val openAppIntent = Intent(this, MainActivity::class.java)
        val pendingOpenApp = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action to quickly simulate 100 steps
        val walkIntent = Intent(this, AppMonitorService::class.java).apply {
            action = ACTION_SIMULATE_STEPS
        }
        val pendingWalk = PendingIntent.getService(
            this,
            1,
            walkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (state.isInstagramActive) {
            if (state.bankedSeconds > 0) "Instagram Active (${state.activeProfile.name})" else "Instagram Locked (${state.activeProfile.name})"
        } else {
            "StepLock Active • ${state.activeProfile.name}"
        }

        val content = if (state.isInstagramActive) {
            if (state.bankedSeconds > 0) {
                "Banked Vault: ${state.bankedMinutes}m ${state.bankedSecondsRemainder}s remaining"
            } else {
                "Time expired! Take ${state.stepsPerMinute} steps to earn 1 minute."
            }
        } else {
            "Vault: ${state.bankedMinutes}m ${state.bankedSecondsRemainder}s • ${state.dailySteps} steps today"
        }

        // High priority lock full screen intent when locked
        val lockIntent = Intent(this, LockScreenActivity::class.java)
        val pendingLock = PendingIntent.getActivity(
            this,
            2,
            lockIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(pendingOpenApp)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(0, "+100 Steps", pendingWalk)

        if (state.isInstagramActive && state.bankedSeconds <= 0) {
            builder.setFullScreenIntent(pendingLock, true)
            builder.setPriority(NotificationCompat.PRIORITY_HIGH)
        }

        return builder.build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "StepLock Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors Instagram screen time and physical steps"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForegroundMonitoring()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
