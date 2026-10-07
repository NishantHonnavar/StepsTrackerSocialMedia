package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("BootReceiver", "Received broadcast action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            StepLockRepository.init(context)
            // Verify morning auto-lock on reboot/restart
            StepLockRepository.checkDailyMidnightOrMorningReset()
            // Always auto-start the persistent background step monitor & screen-time service
            AppMonitorService.start(context)
            Log.d("BootReceiver", "StepLock background monitor service auto-started successfully after reboot.")
        }
    }
}
