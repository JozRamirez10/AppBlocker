package com.app.appblocker.data.local.dbs

import androidx.room.Database
import androidx.room.RoomDatabase
import com.app.appblocker.data.local.daos.ProfileAppDao
import com.app.appblocker.data.local.daos.ProfileDao
import com.app.appblocker.data.local.daos.ProfileWebLinkDao
import com.app.appblocker.data.local.daos.ScheduleDao
import com.app.appblocker.data.local.daos.WebLinkDao
import com.app.appblocker.data.local.entities.Profile
import com.app.appblocker.data.local.entities.ProfileApp
import com.app.appblocker.data.local.entities.ProfileWebLink
import com.app.appblocker.data.local.entities.Schedule
import com.app.appblocker.data.local.entities.WebLink

@Database(
    entities = [
        Profile::class,
        ProfileApp::class,
        WebLink::class,
        ProfileWebLink::class,
        Schedule::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao() : ProfileDao
    abstract fun profileAppDao() : ProfileAppDao
    abstract fun webLinkDao() : WebLinkDao
    abstract fun profileWebLinkDao() : ProfileWebLinkDao
    abstract fun scheduleDao() : ScheduleDao
}