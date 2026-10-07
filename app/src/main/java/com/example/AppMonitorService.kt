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
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
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

    private var floatingBubbleView: TextView? = null
    private var windowManager: WindowManager? = null
    private val mainHandler = Handler(Looper.getMainLooper())

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
            else -> {
                startForegroundMonitoring()
            }
        }
        return START_STICKY
    }

    private fun startForegroundMonitoring() {
        StepLockRepository.setServiceRunning(true)
        val initialNotification = buildNotification()

        try {
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
        } catch (e: Exception) {
            Log.e("AppMonitorService", "Failed to startForeground: ${e.message}", e)
            try {
                startForeground(NOTIFICATION_ID, initialNotification)
            } catch (inner: Exception) {
                Log.e("AppMonitorService", "Fallback startForeground failed: ${inner.message}", inner)
            }
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
        mainHandler.post { removeFloatingBubble() }
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun checkForegroundAppAndEnforceLock() {
        // Enforce daily morning lock and date rollover 24/7 in background
        StepLockRepository.checkDailyMidnightOrMorningReset()

        val currentForegroundApp = getActiveForegroundPackage()
        val isInstagramForeground = currentForegroundApp == TARGET_PACKAGE
        StepLockRepository.setInstagramActive(isInstagramForeground)

        val state = StepLockRepository.state.value

        if (isInstagramForeground) {
            if (state.bankedSeconds <= 0) {
                // Banked time is exhausted or 0 -> Remove bubble & Enforce Lock!
                mainHandler.post { removeFloatingBubble() }
                launchLockScreenActivity()
            } else {
                // Instagram is actively being used with valid banked screen time -> deduct 1 second per second
                StepLockRepository.consumeScreenTime(1)
                mainHandler.post { updateFloatingBubble(state, isInstagramForeground = true) }
            }
        } else {
            mainHandler.post { removeFloatingBubble() }
        }

        updateNotification()
    }

    private fun updateFloatingBubble(state: StepLockData, isInstagramForeground: Boolean) {
        if (!Settings.canDrawOverlays(this)) return

        if (windowManager == null) {
            windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        }

        if (isInstagramForeground && state.bankedSeconds > 0) {
            val bubbleText = "⚡ %02d:%02d".format(state.bankedMinutes, state.bankedSecondsRemainder)
            if (floatingBubbleView == null) {
                val tv = TextView(this).apply {
                    text = bubbleText
                    setTextColor(android.graphics.Color.WHITE)
                    textSize = 12f
                    typeface = Typeface.DEFAULT_BOLD
                    setPadding(30, 14, 30, 14)
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = 40f
                        setColor(android.graphics.Color.parseColor("#E6141C28"))
                        setStroke(2, android.graphics.Color.parseColor("#00E5FF"))
                    }
                }

                val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                }

                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    layoutType,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.TOP or Gravity.END
                    x = 36
                    y = 120
                }

                try {
                    windowManager?.addView(tv, params)
                    floatingBubbleView = tv
                } catch (e: Exception) {
                    Log.w("AppMonitorService", "Failed to add floating bubble: ${e.message}")
                }
            } else {
                floatingBubbleView?.text = bubbleText
            }
        } else {
            removeFloatingBubble()
        }
    }

    private fun removeFloatingBubble() {
        floatingBubbleView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                // View may already be removed
            }
            floatingBubbleView = null
        }
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

        val title = if (state.isInstagramActive) {
            if (state.bankedSeconds > 0) "Instagram Active (${state.activeProfile.name})" else "Scroll Tax Due • Instagram Locked"
        } else {
            "Scroll Tax Active • ${state.activeProfile.name}"
        }

        val content = if (state.isInstagramActive) {
            if (state.bankedSeconds > 0) {
                "Time remaining: ${state.bankedMinutes}m ${state.bankedSecondsRemainder}s"
            } else {
                "Locked: Walk ${state.stepsToNextMinute} more steps to unlock 1 minute"
            }
        } else {
            "Today: %,d steps • Vault: %dm %ds available".format(
                state.dailySteps,
                state.bankedMinutes,
                state.bankedSecondsRemainder
            )
        }

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
                "Scroll Tax Monitor",
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
