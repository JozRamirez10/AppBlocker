package com.app.appblocker.repositories

import com.app.appblocker.App
import com.app.appblocker.data.local.entities.Profile
import kotlinx.coroutines.flow.Flow

class ProfileRepository {

    private val dao = App.db.profileDao()

    fun listProfiles() : Flow<List<Profile>> = dao.listProfiles()
    suspend fun create(profile: Profile) : Long = dao.insertProfile(profile)
    suspend fun update(profile: Profile) = dao.updateProfile(profile)
    suspend fun delete(profile: Profile) = dao.deleteProfile(profile)
}