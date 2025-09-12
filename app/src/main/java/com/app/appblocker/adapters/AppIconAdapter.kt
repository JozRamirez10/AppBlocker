package com.app.appblocker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.app.appblocker.R
import com.app.appblocker.models.AppModel

class AppIconAdapter (
    private val apps : List<AppModel>
) : RecyclerView.Adapter<AppIconAdapter.IconViewHolder>(){

    inner class IconViewHolder(val imageView : ImageView) : RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_icon, parent, false) as ImageView
        return IconViewHolder(view)
    }

    override fun getItemCount(): Int = apps.size

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.imageView.setImageDrawable(apps[position].appIcon)
    }
}