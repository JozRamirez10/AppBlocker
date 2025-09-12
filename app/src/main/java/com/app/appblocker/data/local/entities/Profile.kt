package com.app.appblocker.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class Profile (
    @PrimaryKey(autoGenerate = true) val id : Int = 0,
    val name : String,
    val isActive : Boolean = false,
)
