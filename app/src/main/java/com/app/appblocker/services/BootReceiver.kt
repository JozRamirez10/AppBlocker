package com.app.appblocker.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.app.appblocker.utils.ActiveProfileManager
import com.app.appblocker.utils.AppConstants

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED || intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            // Cargar perfiles activos guardados
            ActiveProfileManager.loadProfiles(context)
            val activeProfiles = ActiveProfileManager.activeProfileFlow.value

            if (activeProfiles.isNotEmpty()) {
                // Reiniciar el servicio foreground con los perfiles cargados
                val activeNames = activeProfiles.joinToString(",") { it.name }
                val serviceIntent = Intent(context, ForegroundService::class.java).apply {
                    putExtra(AppConstants.PROFILE_NAMES, activeNames)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }
}