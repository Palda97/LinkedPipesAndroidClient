package cz.palda97.lpclient.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.repository.RepositoryRoutines
import cz.palda97.lpclient.model.repository.ServerRepository

class CommonViewModel(application: Application) : AndroidViewModel(application) {

    private val serverRepository: ServerRepository = Injector.serverRepository

    private fun onServerToFilterChange() {
        RepositoryRoutines().onServerToFilterChange()
    }

    private fun registerChanges(server: ServerInstance?) {
        val changed = server != serverRepository.serverToFilter
        serverRepository.serverToFilter = server
        if (changed) {
            onServerToFilterChange()
        }
    }

    var serverToFilter: ServerInstance?
        get() = serverRepository.serverToFilter
        private set(value) {
            registerChanges(value)
        }

    fun setServerToFilterFun(serverInstance: ServerInstance?) {
        serverToFilter = serverInstance
    }
}