package com.app.appblocker.activities

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.appblocker.adapters.AppListAdapter
import com.app.appblocker.databinding.ActivityAppListBinding
import com.app.appblocker.models.AppModel
import com.app.appblocker.utils.Utils
import com.app.appblocker.utils.BlacklistApps
import com.app.appblocker.view_models.AppListViewModel
import kotlinx.coroutines.launch

class AppListActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAppListBinding
    private lateinit var adapter : AppListAdapter
    private val appList = mutableListOf<AppModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = AppListAdapter(appList)
        binding.RVApplist.layoutManager = LinearLayoutManager(this)

        val profileId = intent.getIntExtra("profileId", -1)

        if(profileId != -1){
            loadInstalledApps(profileId)
        }else{
            loadInstalledApps(-1)
        }

        binding.ibBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.mbSave.setOnClickListener {
            saveApps()
        }
    }

    private fun saveApps() {
        val selectedApps = appList.filter { it.isSelected }
        val profileId = intent.getIntExtra("profileId", -1)

        if(profileId != -1){
            val vm : AppListViewModel by viewModels()
            vm.saveSelectedApps(profileId, appList)
            Utils.ToasUtils.showToast(
                this,
                "The applications have been saved successfully"
            )
        }
        finish()
    }

    private fun loadInstalledApps(profileId : Int) {
        val pm = this.packageManager

        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val resolveInfos = pm.queryIntentActivities(intent, 0)

        for (resolveInfo in resolveInfos) {
            val packageName = resolveInfo.activityInfo.packageName

            if(packageName in BlacklistApps.packages) continue
            // if(isSystemApp(pm, packageName)) continue

            val appName = resolveInfo.loadLabel(pm).toString()
            val appIcon = resolveInfo.loadIcon(pm)

            appList.add(AppModel(appName, appIcon, packageName))
        }

        appList.sortBy { it.appName.lowercase() }

        val vm : AppListViewModel by viewModels()
        lifecycleScope.launch {
            val savedApps = vm.getPackageNameAppByProfile(profileId)
            appList.forEach { app ->
                if(savedApps.contains(app.packageName)){
                    app.isSelected = true
                }
            }
            adapter.notifyDataSetChanged()
            binding.RVApplist.adapter = adapter
        }
    }

    private fun isSystemApp(pm:PackageManager, packageName : String) : Boolean {
        return try{
            val appInfo = pm.getApplicationInfo(packageName, 0)
            (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (e:PackageManager.NameNotFoundException){
            false
        }
    }
}