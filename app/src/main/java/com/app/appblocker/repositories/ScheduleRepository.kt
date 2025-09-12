package com.app.appblocker.repositories

import com.app.appblocker.App
import com.app.appblocker.data.local.entities.Schedule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ScheduleRepository {
    private val scheduleDao = App.db.scheduleDao()
    private val profileDao = App.db.profileDao()

    suspend fun insertOrUpdateSchedule(profileId : Int , schedule : Schedule){
        val existingSchedule = scheduleDao.getScheduleByProfileId(profileId)

        if(existingSchedule != null){
            val updated = schedule.copy(id = existingSchedule.id, profileId = profileId)
            scheduleDao.updateSchedule(updated)
        }else{
            val newSchedule = schedule.copy(profileId = profileId)
            scheduleDao.insertSchedule(newSchedule)
        }
    }

    fun getScheduleByProfile(profileId : Int) : Flow<Schedule?> {
        return scheduleDao.getScheduleByProfileFlow(profileId)
    }
}