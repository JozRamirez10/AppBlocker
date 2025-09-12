package com.app.appblocker.activities

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.app.appblocker.data.local.entities.Schedule
import com.app.appblocker.databinding.ActivityScheduleBinding
import com.app.appblocker.enums.DaysOfWeek
import com.app.appblocker.utils.Utils
import com.app.appblocker.view_models.ScheduleViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ScheduleActivity : AppCompatActivity() {

    private lateinit var binding : ActivityScheduleBinding
    private val vm : ScheduleViewModel by viewModels()

    private var profileId : Int = -1

    private val formatter12 = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
    private val formatter24 = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadProfile()

        binding.ibBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.chipAllDays.setOnCheckedChangeListener { _, isChecked ->
            markDays(isChecked)
        }

        binding.sAllDay.setOnCheckedChangeListener { _, isChecked ->
            validateChecked(isChecked)
        }

        binding.mbScheduleHourFrom.setOnClickListener {
            showTimePicker("From", binding.mbScheduleHourFrom)
        }

        binding.mbScheduleHourTo.setOnClickListener {
            showTimePicker("To", binding.mbScheduleHourTo)
        }

        binding.mbSave.setOnClickListener {
            saveSchedule()
        }

    }

    private fun markDays(checked: Boolean) {
        binding.chipMonday.isChecked = checked
        binding.chipTuesday.isChecked = checked
        binding.chipWednesday.isChecked = checked
        binding.chipThursday.isChecked = checked
        binding.chipFriday.isChecked = checked
        binding.chipSaturday.isChecked = checked
        binding.chipSunday.isChecked = checked
    }

    private fun validateChecked(checked: Boolean) {
        if(checked){
            binding.mbScheduleHourFrom.visibility = View.GONE
            binding.mbScheduleHourTo.visibility = View.GONE

            binding.tvFrom.visibility = View.GONE
            binding.tvTo.visibility = View.GONE

            binding.vSplit.visibility = View.GONE
        }else{
            binding.mbScheduleHourFrom.visibility = View.VISIBLE
            binding.mbScheduleHourTo.visibility = View.VISIBLE

            binding.tvFrom.visibility = View.VISIBLE
            binding.tvTo.visibility = View.VISIBLE

            binding.vSplit.visibility = View.VISIBLE
        }
    }

    private fun loadProfile() {
        profileId = intent.getIntExtra("profileId", -1)
        if(profileId == -1){
            Utils.ToasUtils.showToast(
                this, "Invalid Profile"
            )
            finish()
        }else{
            chargeLoad()
        }
    }

    private fun chargeLoad() {
        lifecycleScope.launch {
            vm.getScheduleByProfile(profileId).collect{ schedule ->
                schedule?.let {

                    if(it.hourFrom == "00:00" && it.hourTo == "23:59"){
                        binding.sAllDay.isChecked = true
                        validateChecked(true)
                    }else{
                        val parsedFrom = LocalTime.parse(it.hourFrom, formatter24)
                        val parsedTo = LocalTime.parse(it.hourTo, formatter24)

                        binding.mbScheduleHourFrom.text = parsedFrom.format(formatter12)
                        binding.mbScheduleHourTo.text = parsedTo.format(formatter12)

                        validateChecked(false)
                    }

                    val days = it.days.split(", ")
                    binding.chipMonday.isChecked = DaysOfWeek.MONDAY.value in days
                    binding.chipTuesday.isChecked = DaysOfWeek.TUESDAY.value in days
                    binding.chipWednesday.isChecked = DaysOfWeek.WEDNESDAY.value in days
                    binding.chipThursday.isChecked = DaysOfWeek.THURSDAY.value in days
                    binding.chipFriday.isChecked = DaysOfWeek.FRIDAY.value in days
                    binding.chipSaturday.isChecked = DaysOfWeek.SATURDAY.value in days
                    binding.chipSunday.isChecked = DaysOfWeek.SUNDAY.value in days

                    binding.chipAllDays.isChecked = DaysOfWeek.MONDAY.value in days &&
                            DaysOfWeek.TUESDAY.value in days &&
                            DaysOfWeek.WEDNESDAY.value in days &&
                            DaysOfWeek.THURSDAY.value in days &&
                            DaysOfWeek.FRIDAY.value in days &&
                            DaysOfWeek.SATURDAY.value in days &&
                            DaysOfWeek.SUNDAY.value in days
                }
            }
        }
    }

    private fun saveSchedule() {
        val selectedDays = mutableListOf<String>()
        if(binding.chipMonday.isChecked) selectedDays.add(DaysOfWeek.MONDAY.value)
        if(binding.chipTuesday.isChecked) selectedDays.add(DaysOfWeek.TUESDAY.value)
        if(binding.chipWednesday.isChecked) selectedDays.add(DaysOfWeek.WEDNESDAY.value)
        if(binding.chipThursday.isChecked) selectedDays.add(DaysOfWeek.THURSDAY.value)
        if(binding.chipFriday.isChecked) selectedDays.add(DaysOfWeek.FRIDAY.value)
        if(binding.chipSaturday.isChecked) selectedDays.add(DaysOfWeek.SATURDAY.value)
        if(binding.chipSunday.isChecked) selectedDays.add(DaysOfWeek.SUNDAY.value)

        if(!validateDays()){
            Utils.ToasUtils.showToast(
                this, "You must select at least one day"
            )
            return
        }

        val hourFrom : String
        val hourTo : String

        if(binding.sAllDay.isChecked){
            hourFrom = "00:00"
            hourTo = "23:59"
        }else{
            val parsedFrom = LocalTime.parse(binding.mbScheduleHourFrom.text.toString(), formatter12)
            val parsedTo = LocalTime.parse(binding.mbScheduleHourTo.text.toString(), formatter12)

            hourFrom = parsedFrom.format(formatter24)
            hourTo = parsedTo.format(formatter24)
        }

        if(!validateHours(hourFrom, hourTo)){
            Utils.ToasUtils.showToast(
                this, "You must select a valid time range"
            )
            return
        }

        val schedule = Schedule(
            days = selectedDays.joinToString(", "),
            hourFrom = hourFrom,
            hourTo = hourTo,
            profileId = profileId
        )

        lifecycleScope.launch {
            val scheduleId = vm.insertScheduleWithProfile(profileId, schedule)
        }
        Utils.ToasUtils.showToast(
            this,
            "The schedule has been saved successfully"
        )
        finish()
    }

    private fun validateHours(hourFrom: String, hourTo: String) : Boolean {
        return !(hourFrom == "-- : --" || hourTo == "-- : --")
    }

    private fun validateDays() : Boolean {
        return !(!binding.chipMonday.isChecked &&
                !binding.chipTuesday.isChecked &&
                !binding.chipWednesday.isChecked &&
                !binding.chipThursday.isChecked &&
                !binding.chipFriday.isChecked &&
                !binding.chipSaturday.isChecked &&
                !binding.chipSunday.isChecked)
    }

    private fun showTimePicker(title : String, button : MaterialButton) {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setTitleText("Schedule $title")
            .build()

        picker.show(supportFragmentManager, "TIME_PICKER")

        picker.addOnPositiveButtonClickListener {
            val selectedHour = picker.hour
            val selectedMinute = picker.minute

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, selectedHour)
                set(Calendar.MINUTE, selectedMinute)
            }
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            button.text = formatter.format(calendar.time)
        }
    }
}