package com.app.appblocker.activities

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.appblocker.R
import com.app.appblocker.adapters.AppIconAdapter
import com.app.appblocker.adapters.WebLinkAdapter
import com.app.appblocker.data.local.entities.Profile
import com.app.appblocker.databinding.ActivityProfileBinding
import com.app.appblocker.databinding.ContentAppListBinding
import com.app.appblocker.databinding.ContentScheduleBinding
import com.app.appblocker.databinding.ContentWebBlockBinding
import com.app.appblocker.enums.DaysOfWeek
import com.app.appblocker.enums.Destination
import com.app.appblocker.enums.ShortDays
import com.app.appblocker.models.AppModel
import com.app.appblocker.utils.Utils
import com.app.appblocker.view_models.AppListViewModel
import com.app.appblocker.view_models.ProfileViewModel
import com.app.appblocker.view_models.ScheduleViewModel
import com.app.appblocker.view_models.WebLinkViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding : ActivityProfileBinding
    private lateinit var imm : InputMethodManager // Keyboard

    private lateinit var scheduleBinding : ContentScheduleBinding
    private lateinit var appListBinding : ContentAppListBinding
    private lateinit var webBlockBinding : ContentWebBlockBinding

    private val vm : ProfileViewModel by viewModels()
    private val scheduleVm : ScheduleViewModel by viewModels()
    private val appListVm : AppListViewModel by viewModels()
    private val webLinkVm : WebLinkViewModel by viewModels()

    private var currentProfile : Profile? = null
    private var isAutoCreated : Boolean = false

    companion object{
        private const val DEFAULT_PROFILE_NAME = "New Profile"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager // Init Keyboard

        inflateCardContents()
        initProfile()
        nameProfileBehavior()

        binding.ibBack.setOnClickListener {
            validateDeleteProfile()
            onBackPressedDispatcher.onBackPressed()
        }

        binding.ibDelete.setOnClickListener {
            deleteProfile()
        }

        binding.mbSave.setOnClickListener {
            val name = binding.etProfile.text.toString().trim()
            if(name.isEmpty()){
                binding.etProfile.error = "Write a name please"
                return@setOnClickListener
            }
            saveProfile(name)
        }
    }

    private fun loadActive(profileId: Int){
        lifecycleScope.launch {
            val profile = currentProfile ?: return@launch
            if(!profile.isActive) return@launch

            val scheduleOk = isScheduleValid(profileId)
            val blockOk = isBlockValid(profileId)

            if(!scheduleOk || !blockOk){
                vm.toogleActive(profile, false, this@ProfileActivity) {}
                Utils.ToasUtils.showToast(
                    this@ProfileActivity,
                    "The profile has been disabled because settings are missing"
                )
            }
        }
    }

    private suspend fun isScheduleValid(profileId : Int) : Boolean{
        val schedule = scheduleVm.getScheduleByProfile(profileId).firstOrNull()
        return schedule != null
    }

    private suspend fun isBlockValid(profileId : Int) : Boolean{
        val apps = appListVm.getAppsByProfile(profileId).firstOrNull() ?: emptyList()
        val webs = webLinkVm.getLinksByProfile(profileId).firstOrNull() ?: emptyList()
        return apps.isNotEmpty() || webs.isNotEmpty()
    }

    private fun deleteProfile() {
        Utils.DialogUtils.showConfirmDialog(
            context = this,
            title = "Delete Profile",
            message = "Are your sure of delete this profile?",
            onConfirm = {
                vm.deleteProfile(currentProfile!!)
                Utils.ToasUtils.showToast(
                    this,
                    "The profile has been deleted successfully"
                )
                finish()
            }
        )
    }

    private fun validateDeleteProfile() {
        val profileId = currentProfile?.id
        if (isAutoCreated && profileId != null) {
            lifecycleScope.launch {
                val currentNameText = binding.etProfile.text.toString().trim()
                val savedApps = appListVm.getPackageNameAppByProfile(profileId)
                val savedLinks = webLinkVm.getLinksByProfile(profileId).firstOrNull() ?: emptyList()

                if (savedApps.isEmpty() && savedLinks.isEmpty() && currentNameText == DEFAULT_PROFILE_NAME) {
                    vm.profileFlow(profileId).firstOrNull()?.let { vm.deleteProfile(it) }
                }
            }
        }
    }

    private fun saveProfile(name : String) {
        val prof = currentProfile
        if(prof == null){
            vm.createProfile(name)
        }else{
            vm.updateProfile(prof.copy(name = name))
        }
        isAutoCreated = false
        Utils.ToasUtils.showToast(
            this,
            "The profile has been saved successfully"
        )
        finish()
    }

    private fun initProfile() {
        val profileId = intent.getIntExtra("profileId", -1)
        if(profileId != -1){
            chargeProfile(profileId)
        }else{
            loadScheduleEmpty()
        }
    }

    private fun chargeProfile(profileId : Int){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                vm.profileFlow(profileId).collect{ prof ->
                    currentProfile = prof
                    prof?.let { binding.etProfile.setText(it.name) }
                    loadSchedule(profileId)
                    loadIconApps(profileId)
                    loadWebLinks(profileId)
                    loadActive(profileId)
                }
            }
        }
    }

    private fun loadScheduleEmpty() {
        scheduleBinding.tvScheduleDays.text = "${getString(R.string.tv_schedule_days)} ..."
        scheduleBinding.tvScheduleHours.text = "${getString(R.string.tv_schedule_hours)} ..."
    }

    private fun loadSchedule(profileId: Int) {
        lifecycleScope.launch {
            scheduleVm.getScheduleByProfile(profileId).collect{ schedule ->
                if(schedule == null){
                    loadScheduleEmpty()
                }else{
                    if(schedule.hourFrom == "00:00" && schedule.hourTo == "23:59"){
                        scheduleBinding.tvScheduleHours.text =
                            "${getString(R.string.tv_schedule_hours)} All Day"
                    }else{
                        scheduleBinding.tvScheduleHours.text =
                            "${getString(R.string.tv_schedule_hours)} ${schedule.hourFrom} - ${schedule.hourTo}"
                    }

                    val days = schedule.days.split(", ")
                    val monday = DaysOfWeek.MONDAY.value in days
                    val tuesday = DaysOfWeek.TUESDAY.value in days
                    val wednesday = DaysOfWeek.WEDNESDAY.value in days
                    val thursday = DaysOfWeek.THURSDAY.value in days
                    val friday = DaysOfWeek.FRIDAY.value in days
                    val saturday = DaysOfWeek.SATURDAY.value in days
                    val sunday = DaysOfWeek.SUNDAY.value in days

                    if(monday && tuesday && wednesday && thursday && friday && saturday && sunday){
                        scheduleBinding.tvScheduleDays.text =
                            "${getString(R.string.tv_schedule_days)} ${ShortDays.ALLDAYS.value}"
                    }else if(monday && tuesday && wednesday && thursday && friday){
                        scheduleBinding.tvScheduleDays.text =
                            "${getString(R.string.tv_schedule_days)} ${ShortDays.DAYSWEEK.value}"
                    }else if(saturday && sunday){
                        scheduleBinding.tvScheduleDays.text =
                            "${getString(R.string.tv_schedule_days)} ${ShortDays.WEEKEND.value}"
                    }else{
                        scheduleBinding.tvScheduleDays.text =
                            "${getString(R.string.tv_schedule_days)} ${schedule.days}"
                    }
                }

            }
        }
    }

    private fun loadIconApps(profileId: Int) {
        lifecycleScope.launch {
            appListVm.getAppsByProfile(profileId).collect{ savedApps ->
                val pm = packageManager
                val appsWithIcons = savedApps.mapNotNull { profileApp ->
                    try {
                        val icon = pm.getApplicationIcon(profileApp.packageName)
                        AppModel(profileApp.appName, icon, profileApp.packageName)
                    }catch (e : Exception){
                        null
                    }
                }
                appListBinding.rvAppList.layoutManager =
                    LinearLayoutManager(this@ProfileActivity, LinearLayoutManager.HORIZONTAL, false)
                appListBinding.rvAppList.adapter = AppIconAdapter(appsWithIcons)
            }
        }
    }

    private fun loadWebLinks(profileId: Int){
        lifecycleScope.launch {
            webLinkVm.getLinksByProfile(profileId).collect{ savedLinks ->
                val links = savedLinks.map { it.url }
                webBlockBinding.rvWebBlock.layoutManager =
                    LinearLayoutManager(this@ProfileActivity, LinearLayoutManager.HORIZONTAL, false)
                webBlockBinding.rvWebBlock.adapter = WebLinkAdapter(links)
            }
        }
    }

    private fun inflateCardContents() {
        scheduleBinding = ContentScheduleBinding.inflate(layoutInflater, binding.cardSchedule.lyItem, true)
        binding.cardSchedule.tvTitle.visibility = View.GONE

        appListBinding = ContentAppListBinding.inflate(layoutInflater, binding.cardAppList.lyItem, true)
        binding.cardAppList.tvTitle.text = getString(R.string.tv_appList)

        webBlockBinding = ContentWebBlockBinding.inflate(layoutInflater, binding.cardWebBlock.lyItem, true)
        binding.cardWebBlock.tvTitle.text = getString(R.string.tv_webBlock)

        binding.cardSchedule.root.setOnClickListener {
            validateBeforeGo(Destination.SCHEDULE)
        }

        binding.cardAppList.root.setOnClickListener {
            validateBeforeGo(Destination.APP_LIST)
        }

        binding.cardWebBlock.root.setOnClickListener {
            validateBeforeGo(Destination.WEB_BLOCK)
        }
    }

    private fun validateBeforeGo(destination: Destination){
        if(currentProfile == null){
            vm.createProfileAndReturnId(
                binding.etProfile.text.toString().ifEmpty { DEFAULT_PROFILE_NAME }
            ).observe(this) { id ->
                    isAutoCreated = true
                    chargeProfile(id.toInt())
                    goTo(destination, id.toInt())
                }
        }else{
            goTo(destination, currentProfile!!.id)
        }
    }

    private fun goTo(destination: Destination, profileId : Int){
        val intent = when(destination){
            Destination.APP_LIST -> Intent(this, AppListActivity::class.java)
            Destination.WEB_BLOCK -> Intent(this, WebBlockActivity::class.java)
            Destination.SCHEDULE -> Intent(this, ScheduleActivity::class.java)
        }
        intent.putExtra("profileId", profileId)
        startActivity(intent)
    }

    private fun nameProfileBehavior() {
        binding.etProfile.setOnClickListener {
            enableEditing(binding.etProfile)
        }

        binding.etProfile.setOnFocusChangeListener { v, hasFocus ->
            val et = v as EditText
            if(hasFocus) enableEditing(et) else disableEditing(et)
        }

        binding.etProfile.setOnEditorActionListener { v, actionId, event ->
            val et = v as EditText
            if(actionId == EditorInfo.IME_ACTION_DONE){
                disableEditing(et)
                closeKeyboard(v)
                true
            }
            else if(event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN){
                disableEditing(et)
                closeKeyboard(v)
                true
            }else{
                false
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if(ev?.action == MotionEvent.ACTION_DOWN){
            val v = currentFocus
            if(v is EditText){
                val outReact = Rect()
                v.getGlobalVisibleRect(outReact)
                if(!outReact.contains(ev.rawX.toInt(), ev.rawY.toInt())){
                    v.clearFocus()
                    closeKeyboard(v)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun enableEditing(et: EditText) {
        et.isFocusableInTouchMode = true
        et.isCursorVisible = true
        et.requestFocus()
        et.setSelection(et.text.length)

        et.post{
            imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT) // show keyboard
        }
    }

    private fun disableEditing(et: EditText){
        et.isCursorVisible = false
        et.isFocusableInTouchMode = false
        et.isFocusable = false
    }

    private fun closeKeyboard(v : EditText){
        imm.hideSoftInputFromWindow(v.windowToken, 0)
    }
}