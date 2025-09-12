package com.app.appblocker.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.appblocker.databinding.ActivitySplashBinding
import com.app.appblocker.utils.PinManager

class SplashActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val nextIntent = if(!PinManager.hasPin(this)){
            Intent(this, SetupPinActivity::class.java)
        }else{
            Intent(this, VerifyPinActivity::class.java)
        }

        startActivity(nextIntent)
        finish()
    }
}