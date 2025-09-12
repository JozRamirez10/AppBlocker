package com.app.appblocker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.appblocker.databinding.ItemAppBinding
import com.app.appblocker.models.AppModel

class AppListAdapter (
    private val apps : List<AppModel>
) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>(){

    inner class AppViewHolder(binding : ItemAppBinding) : RecyclerView.ViewHolder(binding.root){
        val iv_icon : ImageView = binding.ivAppicon
        val tv_name : TextView = binding.tvAppname
        val cb_selected : CheckBox = binding.cbSelected
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AppViewHolder(binding)
    }

    override fun getItemCount(): Int = apps.size

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        holder.iv_icon.setImageDrawable(app.appIcon)
        holder.tv_name.text = app.appName

        holder.cb_selected.setOnCheckedChangeListener(null)

        holder.cb_selected.isChecked = app.isSelected

        holder.cb_selected.setOnCheckedChangeListener { _, isChecked ->
            app.isSelected = isChecked
        }
    }
}