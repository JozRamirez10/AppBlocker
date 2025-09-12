package com.app.appblocker.data.local.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.appblocker.data.local.entities.Schedule
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule : Schedule) : Long

    @Update
    suspend fun updateSchedule(schedule: Schedule)

    @Delete
    suspend fun deleteSchedule(schedule: Schedule)

    @Query("SELECT * FROM schedule WHERE id = :id")
    suspend fun getScheduleById(id : Int) : Schedule

    @Query("SELECT * FROM schedule WHERE profileId = :profileId LIMIT 1")
    suspend fun getScheduleByProfileId(profileId : Int) : Schedule?

    @Query("SELECT * FROM schedule WHERE profileId = :profileId LIMIT 1")
    fun getScheduleByProfileFlow(profileId : Int) : Flow<Schedule?>

    @Query("SELECT * FROM schedule")
    fun getAllSchedule() : Flow<List<Schedule>>
}