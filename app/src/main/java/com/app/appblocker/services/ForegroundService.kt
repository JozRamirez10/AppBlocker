package com.app.appblocker.services

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.app.appblocker.utils.ActiveProfileManager
import com.app.appblocker.utils.AppConstants
import com.app.appblocker.utils.NotificationUtils


class ForegroundService : Service() {

    companion object{
        const val ACTION_NOTIFICATION_DISMISSED = "NOTIFICATION_DISMISSED"
    }

    override fun onCreate() {
        super.onCreate()
        NotificationUtils.createChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(ActiveProfileManager.activeProfileFlow.value.isEmpty()){
            ActiveProfileManager.loadProfiles(applicationContext)
        }

        val profileNames = intent?.getStringExtra(AppConstants.PROFILE_NAMES)
            ?: ActiveProfileManager.activeProfileFlow.value.joinToString(", ") { it.name }
                .ifEmpty { "Unknown" }

        val notification = NotificationUtils.buildProfileNotification(this, profileNames)
        startForeground(NotificationUtils.NOTIFICATION_ID, notification)

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        Handler(Looper.getMainLooper()).postDelayed({
            val restartIntent = Intent(applicationContext, ForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                applicationContext.startForegroundService(restartIntent)
            }else{
                applicationContext.startService(restartIntent)
            }
        }, 1000)
    }

    override fun onBind(p0: Intent?): IBinder? = null

}