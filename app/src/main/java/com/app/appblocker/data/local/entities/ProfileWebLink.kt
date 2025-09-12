package com.app.appblocker.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "profile_weblink",
    primaryKeys = ["profileId", "webLinkId"],
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WebLink::class,
            parentColumns = ["id"],
            childColumns = ["webLinkId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ]
)
data class ProfileWebLink (
    val profileId : Int,
    val webLinkId : Int
)