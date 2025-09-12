package com.app.appblocker.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.appblocker.adapters.ProfileAdapter
import com.app.appblocker.databinding.ActivityMainBinding
import com.app.appblocker.utils.Utils
import com.app.appblocker.view_models.ProfileViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private val vm : ProfileViewModel by viewModels()
    private lateinit var adapter: ProfileAdapter

    private lateinit var requestNotificationPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestNotificationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (!isGranted) {
                    Utils.ToasUtils.showToast(
                        this,
                        "Notification permission denied"
                    )
                }
            }

        adapter = ProfileAdapter(
            profiles = emptyList(),
            onSwtichChanged = { profile, checked, revert ->
                vm.toogleActive(profile, checked, this) { success ->
                    if (!success) {
                        // Aquí decides qué hacer si falla la activación
                        if (!Utils.PermissionUtils.hasNotificationPermission(this)) {
                            requestNotificationPermissionIfNeeded()
                        }
                        revert()
                    }
                }
            },
            onItemClick = { profile -> goProfile(profile.id) }
        )

        binding.ibSettings.setOnClickListener {
            val intent = Intent(this, VerifyPinActivity::class.java)
            intent.putExtra("mode", "reconfig")
            startActivity(intent)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                vm.profiles.collect(){ profiles ->
                    if(profiles.isEmpty()){
                        binding.recyclerView.visibility = View.GONE
                        binding.mbAddProfile.visibility = View.GONE
                        binding.btnAddProfile.visibility = View.VISIBLE
                    }else{
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.mbAddProfile.visibility = View.VISIBLE
                        binding.btnAddProfile.visibility = View.GONE
                        adapter.setData(profiles)
                    }
                }
            }
        }

        binding.mbAddProfile.setOnClickListener { goProfile() }
        binding.btnAddProfile.setOnClickListener { goProfile() }

        Utils.SwipeToDeleteHelper.attachToRecyclerView(
            recyclerView = binding.recyclerView,
            getItem = { pos -> adapter.getProfileAt(pos) },
            onDeleteConfirmed = {
                profile -> vm.deleteProfile(profile)
                Utils.ToasUtils.showToast(
                    this,
                    "The profile has been deleted successfully"
                )
            },
            context = this,
            title = "Delete Profile?",
            message = "Are you sure you want to delete this profile?"
        )
    }

    private fun goProfile(profileId : Int? = null) {
        val intent = Intent(this, ProfileActivity::class.java)
        profileId?.let {intent.putExtra("profileId", it)}
        startActivity(intent)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
