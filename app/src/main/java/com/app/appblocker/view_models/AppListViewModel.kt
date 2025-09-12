package com.app.appblocker.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.appblocker.data.local.entities.ProfileApp
import com.app.appblocker.models.AppModel
import com.app.appblocker.repositories.ProfileAppRepository
import kotlinx.coroutines.launch

class AppListViewModel (
    private val repo : ProfileAppRepository = ProfileAppRepository()
) : ViewModel(){

    fun saveSelectedApps(profileId : Int, apps : List<AppModel>){
        viewModelScope.launch {
            val toSave = apps.filter { it.isSelected }.map {
                ProfileApp(
                    profileId = profileId,
                    packageName = it.packageName,
                    appName = it.appName
                )
            }
            repo.saveApps(profileId, toSave)
        }
    }

    suspend fun getPackageNameAppByProfile(profileId: Int) : List<String>{
        return repo.getPackageNameAppByProfile(profileId)
    }

    fun getAppsByProfile(profileId: Int) =
        repo.getAppsByProfile(profileId)
}