package com.app.appblocker.repositories

import com.app.appblocker.App
import com.app.appblocker.data.local.entities.ProfileWebLink
import com.app.appblocker.data.local.entities.WebLink

class WebLinkRepository {
    private val dao = App.db.webLinkDao()
    private val profileLinkDao = App.db.profileWebLinkDao()

    fun listWebLinks() = dao.listWebLinks()

    suspend fun create(link : WebLink) : Long = dao.insertWebLink(link)
    suspend fun delete(link : WebLink) = dao.deleteWebLink(link)

    fun getLinksByProfile(profileId : Int) = profileLinkDao.getLinksByProfile(profileId)

    suspend fun addLinkToProfile(profileId: Int, webLinkId: Int) =
        profileLinkDao.insertProfileWebLink(ProfileWebLink(profileId, webLinkId))

    suspend fun removeLinkFromProfile(profileId: Int, webLinkId: Int) =
        profileLinkDao.deleteProfileWebLink(ProfileWebLink(profileId, webLinkId))
}