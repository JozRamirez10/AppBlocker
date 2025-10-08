package com.app.appblocker.view_models

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.ServiceCompat.startForeground
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.app.appblocker.utils.ActiveProfileManager
import com.app.appblocker.data.local.entities.Profile
import com.app.appblocker.repositories.ProfileAppRepository
import com.app.appblocker.repositories.ProfileRepository
import com.app.appblocker.repositories.ScheduleRepository
import com.app.appblocker.repositories.WebLinkRepository
import com.app.appblocker.services.ForegroundService
import com.app.appblocker.utils.NotificationUtils
import com.app.appblocker.utils.Utils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repo : ProfileRepository = ProfileRepository(),
    private val repoApp : ProfileAppRepository = ProfileAppRepository(),
    private val repoSchedule : ScheduleRepository = ScheduleRepository(),
    private val repoWebLink : WebLinkRepository = WebLinkRepository()
) : ViewModel () {

    val profiles = repo.listProfiles()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun toogleActive(
        profile: Profile,
        active : Boolean,
        context: Context,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            if (active) {
                // 1. Validar Accessibility
                if (!Utils.PermissionUtils.isAccessibilityServiceEnabled(context)) {
                    Utils.ToasUtils.showToast(context, "Enable Accessibility permission")
                    Utils.PermissionUtils.openAccessibilitySettings(context)
                    onResult(false)
                    return@launch
                }

                if (!Utils.PermissionUtils.hasNotificationPermission(context)) {
                    onResult(false)
                    return@launch
                }

                val schedule = repoSchedule.getScheduleByProfile(profile.id).first()
                if (schedule == null) {
                    Utils.ToasUtils.showToast(
                        context,
                        "You must set a schedule to activate it"
                    )
                    repo.update(profile.copy(isActive = false))
                    onResult(false)
                    return@launch
                }

                val apps = repoApp.getAppsByProfile(profile.id).first()
                val webs = repoWebLink.getLinksByProfile(profile.id).first()

                if (apps.isEmpty() && webs.isEmpty()) {
                    Utils.ToasUtils.showToast(
                        context,
                        "You must set apps or pages web to activate it"
                    )
                    repo.update(profile.copy(isActive = false))
                    onResult(false)
                    return@launch
                }

                if(profile.isStrictModeEnabled){
                    Utils.DialogUtils.showConfirmDialog(
                        context = context,
                        title = "Are you sure you want to activate this profile with strict mode? ",
                        message = "You won't be able to deactivate it until strict mode ends.",
                        onConfirm = {
                            (context as? LifecycleOwner)?.lifecycleScope?.launch {
                                activateProfile(
                                    profile = profile, context = context
                                )
                                onResult(true)
                            }
                        },
                        onCancel ={
                            onResult(false)
                        }
                    )
                }else{
                    activateProfile(profile, context)
                    onResult(true)
                }
            } else {

                if(profile.isStrictModeEnabled){
                    val timeDifference = profile.strictDurationMillis - (System.currentTimeMillis() - profile.strictStartedAt!!)
                    if(timeDifference > 0){
                        val time = Utils.ParseUtils.millisToTimeParts(timeDifference)

                        Utils.ToasUtils.showToast(
                            context,
                            "The strict mode is actived. You can desactivate this profile in " +
                                    "${time.days}d ${time.hours}h ${time.minutes}m ${time.seconds}s"
                        )

                        onResult(false)
                        return@launch
                    }
                }

                val updated = profile.copy(isActive = false)
                repo.update(updated)
                ActiveProfileManager.removeActiveProfile(context, profile.id)
                Utils.ToasUtils.showToast(
                    context,
                    "The profile \"${profile.name}\" has been deactivated"
                )
                onResult(true)
            }
        }
    }

    private suspend fun activateProfile(
        profile: Profile,
        context: Context
    ) {
        val updated = profile.copy(
            isActive = true,
            strictStartedAt = System.currentTimeMillis()
        )
        repo.update(updated)
        ActiveProfileManager.addActiveProfile(context, updated)

        val activeNames = ActiveProfileManager.activeProfileFlow.value.joinToString(",") {it.name}

        val intent = Intent(context, ForegroundService::class.java).apply {
            putExtra("PROFILE_NAMES", activeNames)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }

        Utils.ToasUtils.showToast(
            context,
            "The profile \"${profile.name}\" has been activated"
        )
    }

    fun createProfile(name : String){
        viewModelScope.launch {
            repo.create(Profile(name = name, isActive = false))
        }
    }

    fun createProfileAndReturnId(name : String) : LiveData<Long>{
        val result = MutableLiveData<Long>()
        viewModelScope.launch {
            val id = repo.create(Profile(name = name, isActive = false))
            result.postValue(id)
        }
        return result
    }

    fun profileFlow(id: Int) = profiles
        .map { list -> list.find { it.id == id} }

    fun updateProfile(profile : Profile){
        viewModelScope.launch {
            repo.update(profile)
        }
    }

    fun deleteProfile(profile: Profile){
        viewModelScope.launch {
            repo.delete(profile)
        }
    }
}