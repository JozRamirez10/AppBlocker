package com.app.appblocker.data.local.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.appblocker.data.local.entities.ProfileWebLink
import com.app.appblocker.data.local.entities.WebLink
import kotlinx.coroutines.flow.Flow

@Dao
interface WebLinkDao {
    @Query("SELECT * FROM weblink ORDER BY url ASC")
    fun listWebLinks(): Flow<List<WebLink>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWebLink(link: WebLink) : Long

    @Delete
    suspend fun deleteWebLink(link: WebLink)
}

@Dao
interface ProfileWebLinkDao {
    @Query("SELECT weblink.* FROM weblink " +
            "INNER JOIN profile_weblink ON weblink.id = profile_weblink.webLinkId " +
            "WHERE profile_weblink.profileId = :profileId")
    fun getLinksByProfile(profileId : Int) : Flow<List<WebLink>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfileWebLink(pwl : ProfileWebLink)

    @Delete
    suspend fun deleteProfileWebLink(pwl : ProfileWebLink)
}