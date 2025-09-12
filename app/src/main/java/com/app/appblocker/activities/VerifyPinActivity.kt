package com.app.appblocker.activities

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.app.appblocker.databinding.ActivityVerifyPinBinding
import com.app.appblocker.utils.PinManager

class VerifyPinActivity : AppCompatActivity() {

    private lateinit var binding : ActivityVerifyPinBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyPinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val type = PinManager.getType(this)
        val mode = intent.getStringExtra("mode")

        if(mode != "reconfig"){
            binding.ibBack.visibility = View.GONE
        }

        if(type == "PIN"){
            binding.etPin.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            binding.etPin.hint = "Enter your PIN"
        }else{
            binding.etPin.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.etPin.hint = "Enter your password"
        }

        binding.etPin.requestFocus()

        binding.ibBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.mbVerify.setOnClickListener {
            val entered = binding.etPin.text.toString().trim()
            binding.tilPin.error = null

            if(entered.isEmpty()){
                binding.tilPin.error = "This field cannot be empty"
                binding.etPin.requestFocus()
                return@setOnClickListener
            }

            if(PinManager.verifyPin(this, entered)) {
                 if(mode == "reconfig"){
                    val setupIntent = Intent(this, SetupPinActivity::class.java)
                    startActivity(setupIntent)
                    finish()
                }else{
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }else{
                binding.tilPin.error = "Incorrect PIN/Password"
                binding.etPin.requestFocus()
            }
        }

    }
}