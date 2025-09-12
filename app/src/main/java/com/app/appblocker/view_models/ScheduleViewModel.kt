package com.app.appblocker.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.appblocker.data.local.entities.Schedule
import com.app.appblocker.repositories.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ScheduleViewModel (
    private val repo : ScheduleRepository = ScheduleRepository()
) : ViewModel() {

    fun insertScheduleWithProfile(profileId : Int, schedule : Schedule) = viewModelScope.launch {
        repo.insertOrUpdateSchedule(profileId, schedule)
    }

    fun getScheduleByProfile(profileId: Int) : Flow<Schedule?> {
        return repo.getScheduleByProfile(profileId)
    }
}