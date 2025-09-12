package com.app.appblocker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.appblocker.data.local.entities.WebLink
import com.app.appblocker.databinding.ItemWebPageBinding

class WebBlockAdapter (
    private val webs : List<WebLink>,
    private val selectedIds : Set<Int>,
    private val onCheckedChange : (WebLink, Boolean) -> Unit
) : RecyclerView.Adapter<WebBlockAdapter.WebViewHolder>(){

    inner class WebViewHolder(binding : ItemWebPageBinding) : RecyclerView.ViewHolder(binding.root){
        val tv_web_link : TextView = binding.tvWebLink
        val cb_selected : CheckBox = binding.cbSelected
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WebViewHolder {
        val binding = ItemWebPageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return WebViewHolder(binding)
    }

    override fun getItemCount(): Int = webs.size

    override fun onBindViewHolder(holder: WebViewHolder, position: Int) {
        val web = webs[position]

        holder.tv_web_link.text = web.url
        holder.cb_selected.isChecked = selectedIds.contains(web.id)

        holder.cb_selected.setOnCheckedChangeListener{ _, isChecked ->
            onCheckedChange(web, isChecked)
        }
    }

    fun getWebLinkAt(position : Int) : WebLink = webs[position]
}