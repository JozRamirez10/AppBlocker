package com.app.appblocker.services

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresPermission
import com.app.appblocker.App
import com.app.appblocker.utils.ActiveProfileManager
import com.app.appblocker.activities.LockActivity
import com.app.appblocker.utils.ScheduleEvaluator
import com.app.appblocker.repositories.ProfileAppRepository
import com.app.appblocker.repositories.ScheduleRepository
import com.app.appblocker.utils.NotificationUtils
import com.app.appblocker.utils.Utils
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class AppBlockerAccessibilityService : AccessibilityService() {

    companion object{
        var lastProfileId: Int? = null
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        val activeProfiles = ActiveProfileManager.activeProfileFlow.value

        if(activeProfiles.isEmpty()){
            stopService(Intent(this, ForegroundService::class.java))
            return
        }

        val intent = Intent(this, ForegroundService::class.java).apply {
            putExtra("PROFILE_NAMES", activeProfiles.joinToString(",") {it.name})
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            startForegroundService(intent)
        }else{
            startService(intent)
        }

        activeProfiles.forEach { profile ->
            val schedule = runBlocking {
                ScheduleRepository().getScheduleByProfile(profile.id).firstOrNull()
            } ?: return@forEach

            val validNow = ScheduleEvaluator.isNowValid(
                profile = profile,
                scheduleDays = Utils.ParseUtils.parseDays(schedule.days),
                startHour = LocalTime.parse(schedule.hourFrom),
                endHour = LocalTime.parse(schedule.hourTo),
                now = LocalDateTime.now()
            )

            if (validNow) {
                val blockedApps = runBlocking {
                    ProfileAppRepository().getPackageNameAppByProfile(profile.id)
                }
                if (blockedApps.contains(packageName)) {
                    val appName = getAppNameFromPackage(packageName)
                    launchLockActivity(appName)
                    return
                }
            }
        }
    }

    private fun getAppNameFromPackage(packageName : String) : String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        }catch (e : PackageManager.NameNotFoundException){
            packageName
        }
    }

    private fun launchLockActivity(target: String) {
        App.isInLockMode = true
        val intent = Intent(this, LockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("target", target)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
        // No es necesario implementar nada
    }
}
