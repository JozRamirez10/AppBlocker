package com.app.appblocker.data.local.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.appblocker.data.local.entities.Profile
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    // Flow para observar cambios en tiempo real (UI reactiva)
    @Query("SELECT * FROM profile ORDER BY name ASC")
    fun listProfiles() : Flow<List<Profile>>

    // suspend : Se ejecuta en corrutina, es decir, no bloquea el hilo principal
    @Query("SELECT * FROM profile")
    suspend fun getProfiles() : List<Profile>

    @Query("SELECT * FROM profile WHERE id = :profileId")
    suspend fun getProfileById(profileId : Int) : Profile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: Profile) : Long

    @Update
    suspend fun updateProfile(profile: Profile)

    @Delete
    suspend fun deleteProfile(profile: Profile)
}