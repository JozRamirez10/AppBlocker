package com.app.appblocker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.appblocker.R
import com.app.appblocker.databinding.ItemWebLinkBinding

class WebLinkAdapter(
    private val links : List<String>
) : RecyclerView.Adapter<WebLinkAdapter.WebLinkViewHolder>() {

    inner class WebLinkViewHolder(binding : ItemWebLinkBinding) : RecyclerView.ViewHolder(binding.root) {
        val tvLink : TextView = binding.tvLink
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WebLinkViewHolder {
        val binding = ItemWebLinkBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return WebLinkViewHolder(binding)
    }

    override fun getItemCount(): Int = links.size

    override fun onBindViewHolder(holder: WebLinkViewHolder, position: Int) {
        holder.tvLink.text = links[position]
    }

}
