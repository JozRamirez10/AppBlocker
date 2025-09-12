package com.app.appblocker.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.app.appblocker.utils.ActiveProfileManager
import com.app.appblocker.activities.LockActivity
import com.app.appblocker.R
import com.app.appblocker.utils.ScheduleEvaluator
import com.app.appblocker.data.local.entities.Profile
import com.app.appblocker.repositories.ProfileAppRepository
import com.app.appblocker.repositories.ProfileRepository
import com.app.appblocker.repositories.ScheduleRepository
import com.app.appblocker.repositories.WebLinkRepository
import com.app.appblocker.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class AccesWatcherServices : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val profileRepository : ProfileRepository = ProfileRepository()
    private val scheduleRepository : ScheduleRepository = ScheduleRepository()
    private val appListRepository : ProfileAppRepository = ProfileAppRepository()
    private val webLinkRepository : WebLinkRepository = WebLinkRepository()

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        observeActiveProfile()
    }

    private fun startForegroundService() {
        val channelId = "access_watcher_channel"
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                channelId,
                "AppBlocker Active Profile",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notificacion = NotificationCompat.Builder(this, channelId)
            .setContentTitle("AppBlocker")
            .setContentText("Profile Active")
            .setSmallIcon(R.drawable.ic_main)
            .build()

        startForeground(1, notificacion)
    }

    private fun observeActiveProfile(){
        scope.launch {
            ActiveProfileManager.activeProfileFlow.collect { profile ->
                if(profile != null){
                    startWatching(profile)
                }
            }
        }
    }

    private fun startWatching(profile : Profile) {
        scope.launch {
            while(isActive){
                try{
                    val currentApp = getCurrentForegroundApp()
                    if(currentApp.isNullOrEmpty()){
                        delay(800)
                        continue
                    }

                    val schedule = scheduleRepository.getScheduleByProfile(profile.id).first()
                    if(schedule != null){
                        val validNow = ScheduleEvaluator.isNowValid(
                            profile = profile,
                            scheduleDays = Utils.ParseUtils.parseDays(schedule.days),
                            startHour = LocalTime.parse(schedule.hourFrom),
                            endHour = LocalTime.parse(schedule.hourTo),
                            now = LocalDateTime.now()
                        )
                        if(validNow){
                            val blockedApps : List<String> = appListRepository.getPackageNameAppByProfile(profile.id)
                            if(blockedApps.isNotEmpty() && blockedApps.contains(currentApp)){
                                launchLockActivity(currentApp)
                            }
                        }
                    }
                    delay(800)
                } catch (e : Exception){
                    delay(1_000)
                }

            }
        }
    }

    private fun getCurrentForegroundApp() : String? {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val begin = end - 10_000 // Last 10 seconds

        val usageEvents = usm.queryEvents(begin, end)
        var lastPackage : String? = null
        val event = UsageEvents.Event()

        while(usageEvents.hasNextEvent()){
            usageEvents.getNextEvent(event)
            if(event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND){
                lastPackage = event.packageName
            }
        }
        return lastPackage
    }

    private fun launchLockActivity(target : String) {
        val intent = Intent(this, LockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("target", target)
            // putExtra("type", type.name)
        }
        startActivity(intent)
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onDestroy(){
        super.onDestroy()
        scope.cancel()
    }

}