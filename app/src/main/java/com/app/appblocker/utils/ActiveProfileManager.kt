package com.app.appblocker.utils

import android.content.Context
import com.app.appblocker.data.local.entities.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNot
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.content.edit

object ActiveProfileManager {

    private const val PREF_NAME = "active_profile_prefs"
    private const val KEY_ACTIVE_PROFILES = "active_profiles"

    private val _activeProfilesFlow = MutableStateFlow<List<Profile>>(emptyList())
    val activeProfileFlow : StateFlow<List<Profile>> = _activeProfilesFlow

    fun setActivesProfiles(context: Context, profiles: List<Profile>){
        _activeProfilesFlow.value = profiles
        saveProfiles(context, profiles )
    }

    fun addActiveProfile(context: Context, profile: Profile){
        if(_activeProfilesFlow.value.none {it.id == profile.id} ){
            val updated = _activeProfilesFlow.value + profile
            _activeProfilesFlow.value = updated
            saveProfiles(context, updated)
        }
    }

    fun removeActiveProfile(context: Context, profileId : Int){
        val updated = _activeProfilesFlow.value.filterNot { it.id == profileId }
        _activeProfilesFlow.value = updated
        saveProfiles(context, updated)
    }

    fun clear(context: Context){
        _activeProfilesFlow.value = emptyList()
        saveProfiles(context, emptyList())
    }

    fun loadProfiles(context: Context){
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_ACTIVE_PROFILES, null) ?: return
        val jsonArray = JSONArray(json)
        val list = mutableListOf<Profile>()
        for(i in 0 until jsonArray.length()){
            val obj = jsonArray.getJSONObject(i)
            list.add(
                Profile(
                    id = obj.getInt("id"),
                    name = obj.getString("name"),
                    isActive = true,
                    strictStartedAt = if(obj.has("strictStartedAt")) obj.getLong("strictStartedAt") else null,
                    strictDurationMillis = obj.optLong("strictDurationMillis", 0L),
                    isStrictModeEnabled = obj.optBoolean("isStrictModeEnabled", false)
                )
            )
        }
        _activeProfilesFlow.value = list
    }

    private fun saveProfiles(context: Context, profiles: List<Profile>){
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val arr = JSONArray()
        profiles.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("name", it.name)
            obj.put("isStrictModeEnabled", it.isStrictModeEnabled)
            obj.put("strictDurationMillis", it.strictDurationMillis)
            it.strictStartedAt?.let { time -> obj.put("strictStartedAt", time)}
            arr.put(obj)
        }
        prefs.edit { putString(KEY_ACTIVE_PROFILES, arr.toString()) }
    }

}