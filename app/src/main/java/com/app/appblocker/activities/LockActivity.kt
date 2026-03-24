package com.app.appblocker.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.app.appblocker.App
import com.app.appblocker.databinding.ActivityLockBinding

class LockActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLockBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        val blockedApp = intent.getStringExtra("target") ?: "This app"

        val packageName = intent.getStringExtra("packageName")

        binding.tvLock.text = "$blockedApp is locked"

        if(packageName != null){
            try{
                val icon = packageManager.getApplicationIcon(packageName)
                binding.ivLockedAppIcon.setImageDrawable(icon)
            }catch(e : PackageManager.NameNotFoundException){
                e.printStackTrace()
            }
        }

        binding.bClose.setOnClickListener {
            App.isInLockMode = false
            val home = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(home)
            finish()
        }

    }
}