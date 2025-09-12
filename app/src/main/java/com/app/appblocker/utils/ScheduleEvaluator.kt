package com.app.appblocker.utils

import com.app.appblocker.data.local.entities.Profile
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

object ScheduleEvaluator {

    fun isNowValid(
        profile: Profile,
        scheduleDays: List<Int>,
        startHour: LocalTime,
        endHour: LocalTime,
        now : LocalDateTime = LocalDateTime.now()
    ) : Boolean {

        if(!profile.isActive) return false

        val today = now.dayOfWeek.value
        if(!scheduleDays.contains(today)) return false

        val current = now.toLocalTime()
        return isHourInRange(current, startHour, endHour)

    }

    private fun isHourInRange(current: LocalTime, start: LocalTime, end: LocalTime): Boolean {
        return if (end.isAfter(start) || end == start) {
            // Rango normal: start <= current <= end
            !current.isBefore(start) && !current.isAfter(end)
        } else {
            // Rango cruza medianoche: (current >= start) OR (current <= end)
            !current.isBefore(start) || !current.isAfter(end)
        }
    }

}