package com.app.appblocker

import android.app.Application
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.room.Room
import com.app.appblocker.activities.VerifyPinActivity
import com.app.appblocker.data.local.dbs.AppDatabase
import com.app.appblocker.utils.PinManager
import com.jakewharton.threetenabp.AndroidThreeTen

class App : Application(), LifecycleObserver {

    companion object{
        lateinit var  db : AppDatabase
            private  set
        var isInLockMode : Boolean = false
    }

    override fun onCreate() {
        super.onCreate()

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "AppBlockerDB"
        )
            .fallbackToDestructiveMigration()
            .build()

        AndroidThreeTen.init(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onEnterForeground(){
        if(!isInLockMode && PinManager.hasPin(this)){
            val intent = Intent(this, VerifyPinActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }
    }
}