package com.app.appblocker.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "schedule",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Schedule (
    @PrimaryKey(autoGenerate = true) val id : Int = 0,
    val profileId : Int,
    val days : String, // "MONDAY, TUESDAY, etc."
    val hourFrom: String, // "08:00"
    val hourTo: String // "17:00"
)