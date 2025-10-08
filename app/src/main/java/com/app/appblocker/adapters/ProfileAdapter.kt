package com.app.appblocker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.appblocker.data.local.entities.Profile
import com.app.appblocker.databinding.ItemProfileBinding

class ProfileAdapter(
    private var profiles : List<Profile>,
    private val onSwtichChanged : (Profile, Boolean, revert: () -> Unit) -> Unit,
    private val onItemClick : (Profile) -> Unit
) : RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>(){

    inner class ProfileViewHolder(val binding : ItemProfileBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(profile : Profile){
            binding.tvNombreProfile.text = profile.name
            binding.switchProfile.isChecked = profile.isActive

            binding.switchProfile.setOnCheckedChangeListener{_, isChecked ->
                onSwtichChanged(profile, isChecked){
                    binding.switchProfile.isChecked = profile.isActive
                }
            }

            binding.root.setOnClickListener {
                onItemClick(profile)
            }
        }
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