package com.app.appblocker.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weblink")
data class WebLink (
    @PrimaryKey(autoGenerate = true) val id : Int = 0,
    val url : String
)