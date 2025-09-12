package com.app.appblocker.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.appblocker.data.local.entities.WebLink
import com.app.appblocker.repositories.WebLinkRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WebLinkViewModel (
    private val repo : WebLinkRepository = WebLinkRepository()
) : ViewModel() {

    val links = repo.listWebLinks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addLink(url : String){
        viewModelScope.launch {
            repo.create(WebLink(url = url))
        }
    }

    fun deleteLink(link: WebLink){
        viewModelScope.launch {
            repo.delete(link)
        }
    }

    fun getLinksByProfile(profileId : Int) = repo.getLinksByProfile(profileId)

    fun addLinkToProfile(profileId: Int, webLinkId : Int){
        viewModelScope.launch {
            repo.addLinkToProfile(profileId, webLinkId)
        }
    }

    fun removeListFromProfile(profileId: Int, webLinkId: Int){
        viewModelScope.launch {
            repo.removeLinkFromProfile(profileId, webLinkId)
        }
    }
}