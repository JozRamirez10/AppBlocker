package com.app.appblocker.utils

import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

object ActivityUtils {

    // Minimize app
    fun setMinimizeOnBack(activity: AppCompatActivity){
        activity.onBackPressedDispatcher.addCallback(activity, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                activity.moveTaskToBack(true)
            }
        })
    }

}