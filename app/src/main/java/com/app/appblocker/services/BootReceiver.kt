package com.app.appblocker.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.app.appblocker.utils.ActiveProfileManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent : Intent?) {
        if(intent?.action == Intent.ACTION_BOOT_COMPLETED && context != null){
            ActiveProfileManager.loadProfiles(context)
            val activeProfiles = ActiveProfileManager.activeProfileFlow.value
            if(activeProfiles.isNotEmpty()){
                val names = activeProfiles.joinToString(", ") {it.name}
                val serviceIntent = Intent(context, ForegroundService::class.java)
                serviceIntent.putExtra("PROFILE_NAMES", names)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                }else{
                    context.startService(serviceIntent)
                }
            }
        }
    }
}