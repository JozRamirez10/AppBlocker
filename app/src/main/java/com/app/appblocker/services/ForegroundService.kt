package com.app.appblocker.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import androidx.annotation.RequiresPermission
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
        when(intent?.action){
            ACTION_NOTIFICATION_DISMISSED -> {
                AppBlockerAccessibilityService.lastProfileId = null
                stopSelf()
                return START_NOT_STICKY
            }
        }
        val profileName = intent?.getStringExtra("PROFILE_NAME") ?: "Unknow"
        val notification = NotificationUtils.buildProfileNotification(this, profileName)
        startForeground(NotificationUtils.NOTIFICATION_ID, notification)

        return START_STICKY
    }

    @SuppressLint("ScheduleExactAlarm")
    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext, ForegroundService::class.java).apply {
            setPackage(packageName)
        }
        val restartPendingIntent = PendingIntent.getService(
            applicationContext,
            1,
            restartServiceIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 1000,
            restartPendingIntent
        )

        super.onTaskRemoved(rootIntent)
    }

    override fun onBind(p0: Intent?): IBinder? = null

}