package com.app.appblocker.models

import android.graphics.drawable.Drawable

data class AppModel (
    val appName : String,
    val appIcon : Drawable,
    val packageName : String,
    var isSelected : Boolean = false
)