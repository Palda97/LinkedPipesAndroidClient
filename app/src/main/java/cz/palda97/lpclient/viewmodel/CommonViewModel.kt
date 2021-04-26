package cz.palda97.lpclient.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.repository.RepositoryRoutines
import cz.palda97.lpclient.model.repository.ServerRepository

/**
 * ViewModel with methods that are needed at more places.
 */
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

    /**
     * [ServerInstance] used as a filter for displaying purposes.
     */
    var serverToFilter: ServerInstance?
        get() = serverRepository.serverToFilter
        private set(value) {
            registerChanges(value)
        }

    /**
     * Wrapper for setter of the [serverToFilter] property.
     */
    fun setServerToFilterFun(serverInstance: ServerInstance?) {
        serverToFilter = serverInstance
    }

    companion object {

        /**
         * Gets an instance of [CommonViewModel] tied to the owner's lifecycle.
         */
        fun getInstance(owner: ViewModelStoreOwner) = ViewModelProvider(owner).get(CommonViewModel::class.java)
    }
}