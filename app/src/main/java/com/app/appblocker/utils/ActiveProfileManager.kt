package com.app.appblocker.utils

import com.app.appblocker.data.local.entities.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object ActiveProfileManager {

    private val _activeProfileFlow = MutableStateFlow<Profile?>(null)
    val activeProfileFlow : StateFlow<Profile?> = _activeProfileFlow

    fun setActiveProfile(profile : Profile?){
        _activeProfileFlow.value = profile
    }

}