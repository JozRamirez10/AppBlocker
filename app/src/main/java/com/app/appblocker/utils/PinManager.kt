package com.app.appblocker.utils

import android.content.Context
import java.security.MessageDigest
import androidx.core.content.edit

object PinManager {
    private const val PREFS_NAME = "app_lock_prefs"
    private const val KEY_PIN = "user_pin"
    private const val KEY_TYPE = "pin_type" // PIN or Password

    private fun sha256(input : String) : String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") {"%02x".format(it)}
    }

    fun savePin(context : Context, pin : String, type : String){
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val hashedPin = sha256(pin)
        prefs.edit {
            putString(KEY_PIN, hashedPin)
                .putString(KEY_TYPE, type)
        }
    }

    fun verifyPin(context: Context, pin : String) : Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stored = prefs.getString(KEY_PIN, null) ?: return false
        return stored == sha256(pin)
    }

    fun hasPin(context: Context) : Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.contains(KEY_PIN)
    }

    fun getType(context: Context) : String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_TYPE, null)
    }
}