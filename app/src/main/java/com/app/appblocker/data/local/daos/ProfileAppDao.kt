package com.app.appblocker.data.local.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.appblocker.data.local.entities.ProfileApp
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileAppDao {
    @Query("SELECT * FROM profile_app WHERE profileId =:profileId")
    fun getAppsByProfile(profileId: Int): Flow<List<ProfileApp>>

    @Query("SELECT packageName FROM profile_app WHERE  profileId = :profileId")
    suspend fun getPackageAppByProfile(profileId: Int) : List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<ProfileApp>)

    @Delete
    suspend fun deleteApps(app: ProfileApp)

    @Query("DELETE FROM profile_app WHERE profileId =:profileId")
    suspend fun deleteAllApps(profileId: Int)
}