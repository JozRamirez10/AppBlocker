package com.app.appblocker.view_models

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.ServiceCompat.startForeground
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

                val updated = profile.copy(isActive = true)
                repo.update(updated)
                ActiveProfileManager.setActiveProfile(updated)

                val intent = Intent(context, ForegroundService::class.java).apply {
                    putExtra("PROFILE_NAME", updated.name)
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
                onResult(true)

            } else {
                val updated = profile.copy(isActive = false)
                repo.update(updated)
                ActiveProfileManager.setActiveProfile(null)
                Utils.ToasUtils.showToast(
                    context,
                    "The profile \"${profile.name}\" has been deactivated"
                )
                onResult(true)
            }
        }
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