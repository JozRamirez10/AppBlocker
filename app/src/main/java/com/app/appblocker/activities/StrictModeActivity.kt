package com.app.appblocker.activities

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.app.appblocker.R
import com.app.appblocker.data.local.entities.Profile
import com.app.appblocker.databinding.ActivityStrictModeBinding
import com.app.appblocker.utils.Utils
import com.app.appblocker.view_models.ProfileViewModel
import kotlinx.coroutines.launch

class StrictModeActivity : AppCompatActivity() {

    private lateinit var  binding : ActivityStrictModeBinding

    private val vm : ProfileViewModel by viewModels()

    private var currentProfile : Profile? = null
    private var profileId : Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStrictModeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadProfile()

        configureRanges()

        val active = intent.getIntExtra("active", -1)
        if(active != -1 && active == 1){
            binding.mbSave.visibility = View.GONE
        }

        binding.ibBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.mbSave.setOnClickListener {
            save()
        }
    }

    private fun loadProfile() {
        profileId = intent.getIntExtra("profileId", -1)
        if(profileId == -1){
            errorProfile()
        }else{
            chargeLoad()
        }
    }

    private fun chargeLoad() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                vm.profileFlow(profileId).collect{ prof ->
                    currentProfile = prof
                    val millis = currentProfile?.strictDurationMillis
                    if(millis != null && millis > 0){
                        val time = Utils.ParseUtils.millisToTimeParts(millis)
                        binding.npDays.value = time.days.toInt()
                        binding.npHours.value = time.hours.toInt()
                        binding.npMinutes.value = time.minutes.toInt()
                    }
                }
            }
        }
    }

    private fun save() {
        val prof = currentProfile
        if(prof == null || profileId == -1){
            errorProfile()
        }else{
            val totalMillis = (
                (binding.npDays.value * 24 * 60 * 60) +
                (binding.npHours.value * 60 * 60) +
                (binding.npMinutes.value * 60)
            ) * 1000L
            vm.updateProfile(prof.copy(strictDurationMillis = totalMillis))
            Utils.ToasUtils.showToast(
                this,
                "Strict mode has been saved successfully"
            )
            finish()
        }
    }

    private fun configureRanges() {
        binding.npDays.minValue = 0
        binding.npDays.maxValue = 30

        binding.npHours.minValue = 0
        binding.npHours.maxValue = 23

        binding.npMinutes.minValue = 0
        binding.npMinutes.maxValue = 59
    }

    private fun errorProfile(){
        Utils.ToasUtils.showToast(
            this,
            "Invalid Profile"
        )
        finish()
    }
}