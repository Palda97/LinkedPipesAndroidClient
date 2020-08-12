package cz.palda97.lpclient.model.repository

import androidx.lifecycle.LiveData
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.ServerInstance

abstract class ServerRepository {
    abstract val liveServers: LiveData<MailPackage<List<ServerInstance>>>
}