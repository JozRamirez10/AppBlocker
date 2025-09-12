package com.app.appblocker.enums

enum class DaysOfWeek(val value: String, val intValue : Int) {
    MONDAY("Monday", 1),
    TUESDAY("Tuesday", 2),
    WEDNESDAY("Wednesday", 3),
    THURSDAY("Thursday", 4),
    FRIDAY("Friday", 5),
    SATURDAY("Saturday", 6),
    SUNDAY("Sunday", 7);

    companion object {
        fun fromString(value : String) : DaysOfWeek? {
            return entries.find { it.value.equals(value, ignoreCase = true) }
        }
    }
}

enum class ShortDays(val value : String){
    ALLDAYS("All Days"),
    DAYSWEEK("Days Week"),
    WEEKEND("Weekend")
}