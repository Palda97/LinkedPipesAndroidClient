package cz.palda97.lpclient.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.entities.execution.Execution.Companion.areDone
import cz.palda97.lpclient.model.repository.RepositoryRoutines
import cz.palda97.lpclient.model.repository.ServerRepository
import cz.palda97.lpclient.view.Notifications
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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

        private val refreshMutex = Mutex()

        suspend fun refreshAndNotify(): Unit = refreshMutex.withLock {
            val executions = Injector.repositoryRoutines.refresh().areDone
            Notifications.executionNotification(executions)
        }

        suspend fun updateAndNotify(serverInstance: ServerInstance): Unit = refreshMutex.withLock {
            val executions = Injector.repositoryRoutines.update(serverInstance).areDone
            Notifications.executionNotification(executions)
        }
    }
}