package com.app.appblocker.adapters

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.appblocker.data.local.entities.Profile
import com.app.appblocker.databinding.ItemProfileBinding
import com.app.appblocker.utils.Utils

class ProfileAdapter(
    private var profiles : List<Profile>,
    private val onSwitchChanged : (Profile, Boolean, revert: () -> Unit) -> Unit,
    private val onItemClick : (Profile) -> Unit
) : RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>(){

    inner class ProfileViewHolder(val binding : ItemProfileBinding) : RecyclerView.ViewHolder(binding.root){

        private var handler : Handler? = null
        private var runnable : Runnable? = null

        fun bind(profile : Profile){
            binding.tvNombreProfile.text = profile.name

            stopCountdown()

            binding.switchProfile.setOnCheckedChangeListener(null)
            binding.switchProfile.isChecked = profile.isActive

            val isLocked = profile.isActive && profile.isStrictModeEnabled

            if(isLocked){
                binding.switchProfile.isEnabled = false
            }else{
                binding.switchProfile.isEnabled = true
            }

            if(profile.isStrictModeEnabled){
                binding.tvStrictModeStatus.visibility = View.VISIBLE

                if(profile.isActive){
                    startCountdown(profile)
                }else{
                    val time = Utils.ParseUtils.millisToTimeParts(profile.strictDurationMillis)
                    binding.tvStrictModeStatus.text = "Strict Mode: ${time.days}d ${time.hours}h ${time.minutes}m ${time.seconds}"
                }
            }else{
                binding.tvStrictModeStatus.visibility = View.GONE
            }

            binding.switchProfile.setOnCheckedChangeListener{_, isChecked ->
                onSwitchChanged(profile, isChecked){
                    binding.switchProfile.isChecked = profile.isActive
                }
            }

            binding.root.setOnClickListener {
                onItemClick(profile)
            }
        }

        private fun startCountdown(profile : Profile){
            stopCountdown()

            runnable = profile.strictStartedAt?.let {
                Utils.CountdownUtils.start(
                    handler = Handler(Looper.getMainLooper()),
                    totalMillis = profile.strictDurationMillis,
                    startedAt = it,
                    onTick = {formattedTime ->
                        binding.tvStrictModeStatus.text = "Strict Mode Active: $formattedTime"
                    },
                    onFinished = {
                        binding.tvStrictModeStatus.text = "Strict Mode: Finished"
                        binding.switchProfile.isEnabled = true
                    }
                )
            }
        }

        fun stopCountdown(){
            handler?.removeCallbacks(runnable ?: return)
            handler = null
            runnable = null
        }
    }

    override fun onViewRecycled(holder: ProfileViewHolder) {
        super.onViewRecycled(holder)
        holder.stopCountdown()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val binding = ItemProfileBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProfileViewHolder(binding)
    }

    override fun getItemCount(): Int = profiles.size

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        holder.bind(profiles[position])
    }

    fun setData(newList : List<Profile>){
        profiles = newList
        notifyDataSetChanged()
    }

    fun getProfileAt(position : Int) : Profile = profiles[position]

}