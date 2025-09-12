package com.app.appblocker.activities

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import com.app.appblocker.R
import com.app.appblocker.databinding.ActivitySetupPinBinding
import com.app.appblocker.utils.PinManager
import com.app.appblocker.utils.Utils

class SetupPinActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySetupPinBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupPinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rgType.setOnCheckedChangeListener { _, checkedId ->
            val inputType = if(checkedId == R.id.rbPin){
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            }else{
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            binding.etPin.inputType = inputType
            binding.etConfirmPin.inputType = inputType
        }

        binding.btnSave.setOnClickListener {
            val pinOrPass = binding.etPin.text.toString()
            val confirm = binding.etConfirmPin.text.toString()

            val type = if(binding.rbPin.isChecked) "PIN" else "PASSWORD"

            binding.tilPin.error = null
            binding.tilConfirmPin.error = null

            if(type == "PIN"){
                if(pinOrPass.length < 4 || pinOrPass.length > 8){
                    binding.tilPin.error = "The PIN must be between 4 and 8 digits"
                    binding.etPin.requestFocus()
                    return@setOnClickListener
                }else if(!pinOrPass.all { it.isDigit() }){
                    binding.tilPin.error = "The PIN can only contain numbers"
                    binding.etPin.requestFocus()
                    return@setOnClickListener
                }
            }else{
                if(pinOrPass.length < 6){
                    binding.tilPin.error = "The password must be at least 6 characters long"
                    binding.etPin.requestFocus()
                    return@setOnClickListener
                }
            }

            if(pinOrPass != confirm){
                binding.tilConfirmPin.error = "The PIN/password does not match"
                binding.etConfirmPin.requestFocus()
                return@setOnClickListener
            }

            PinManager.savePin(this, pinOrPass, type)

            val mode = intent.getStringExtra("mode")
            if(mode == "reconfig"){
                Utils.ToasUtils.showToast(
                    this,
                    "Password update successfully"
                )
            }else {
                Utils.ToasUtils.showToast(
                    this,
                    "Security successfully configured"
                )
            }
            startActivity(Intent(this, MainActivity::class.java))
            finish()

        }

    }
}