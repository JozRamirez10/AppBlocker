package com.app.appblocker.repositories

import com.app.appblocker.App
import com.app.appblocker.data.local.entities.ProfileApp

class ProfileAppRepository {

    private val dao = App.db.profileAppDao()

    fun getAppsByProfile(profileId : Int) = dao.getAppsByProfile(profileId)

    suspend fun getPackageNameAppByProfile(profileId: Int) = dao.getPackageAppByProfile(profileId)

    suspend fun saveApps(profileId: Int, apps: List<ProfileApp>){
        dao.deleteAllApps(profileId)
        dao.insertApps(apps)
    }
}