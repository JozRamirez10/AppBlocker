package com.app.appblocker.utils

import android.annotation.SuppressLint
import android.view.View
import android.widget.EditText

object ViewUtils {

    fun setViewEnabled(view: View, isEnabled: Boolean){
        view.isEnabled = isEnabled
        view.isClickable = isEnabled
        view.isFocusable = isEnabled

        if(view is EditText){
            view.isCursorVisible = isEnabled
            view.isFocusableInTouchMode = isEnabled
        }

        // Opacity
        // view.alpha = if(isEnabled) 1.0f else 0.5f
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setViewReadOnly(view: View, isReadOnly: Boolean){
        if(isReadOnly){
            view.setOnTouchListener{_, _ -> true}
            view.isFocusable = false
        }else{
            view.setOnTouchListener(null)
            view.isFocusable = true
        }
    }

    fun setVisibility(view : View, visibility : Boolean){
        view.visibility = if(visibility) View.VISIBLE else View.GONE
    }
}