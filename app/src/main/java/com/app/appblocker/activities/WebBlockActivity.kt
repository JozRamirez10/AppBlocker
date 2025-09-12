package com.app.appblocker.activities

import android.os.Bundle
import android.util.Patterns
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.appblocker.adapters.WebBlockAdapter
import com.app.appblocker.databinding.ActivityWebBlockBinding
import com.app.appblocker.utils.Utils
import com.app.appblocker.view_models.WebLinkViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WebBlockActivity : AppCompatActivity() {

    private lateinit var binding : ActivityWebBlockBinding
    private lateinit var adapter : WebBlockAdapter
    private val vm : WebLinkViewModel by viewModels()

    private var selectedIds = mutableSetOf<Int>()
    private var profileId : Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebBlockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profileId = intent.getIntExtra("profileId", -1)

        setupRecycler()
        loadData()

        binding.ibBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnAddWebLink.setOnClickListener {
            addLink()
        }

        binding.mbSave.setOnClickListener {
            saveSelections()
        }

        Utils.SwipeToDeleteHelper.attachToRecyclerView(
            recyclerView = binding.rvWebBlock,
            getItem = { pos -> adapter.getWebLinkAt(pos) },
            onDeleteConfirmed = { link -> vm.deleteLink(link) },
            context = this,
            title = "Delete Web Page?",
            message = "Are you sure you want to delete this web page?"
        )
    }

    private fun setupRecycler() {
        adapter = WebBlockAdapter(
            webs = emptyList(),
            selectedIds = selectedIds,
            onCheckedChange = { link, isChecked ->
                if(isChecked) selectedIds.add(link.id)
                else selectedIds.remove(link.id)
            }
        )
        binding.rvWebBlock.layoutManager = LinearLayoutManager(this)
        binding.rvWebBlock.adapter = adapter
    }

    private fun loadData() {
        lifecycleScope.launch {
            launch {
                vm.links.collectLatest { links ->
                    adapter = WebBlockAdapter(
                        webs = links,
                        selectedIds = selectedIds,
                        onCheckedChange = { link, isChecked ->
                            if(isChecked) selectedIds.add(link.id)
                            else selectedIds.remove(link.id)
                        }
                    )
                    binding.rvWebBlock.adapter = adapter
                }
            }
            launch {
                vm.getLinksByProfile(profileId).collectLatest { profileLinks ->
                    selectedIds.clear()
                    selectedIds.addAll(profileLinks.map {  it.id })
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun addLink() {
        val url = binding.etAddWebLink.text?.toString()?.trim() ?: ""
        if (url.isEmpty()) {
            binding.etAddWebLink.error = "The field not must empty"
            return
        }

        val normalizeUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "http://$url"
        } else url

        if (!Patterns.WEB_URL.matcher(normalizeUrl).matches()) {
            binding.etAddWebLink.error = "URL invalid"
            return
        }

        lifecycleScope.launch {
            val exists = vm.links.value.any { it.url.equals(normalizeUrl, ignoreCase = true) }
            if (exists) {
                binding.etAddWebLink.error = "This link already exists"
            } else {
                vm.addLink(normalizeUrl)
                binding.etAddWebLink.setText("")
            }
        }
    }

    private fun saveSelections(){
        lifecycleScope.launch {
            val current = vm.getLinksByProfile(profileId).first()

            current.forEach { link ->
                if(!selectedIds.contains(link.id)) {
                    vm.removeListFromProfile(profileId, link.id)
                }
            }

            selectedIds.forEach { webId ->
                vm.addLinkToProfile(profileId, webId)
            }
        }
        finish()
    }
}