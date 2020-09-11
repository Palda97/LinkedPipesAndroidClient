package cz.palda97.lpclient.viewmodel.executions

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.ServerInstance
import cz.palda97.lpclient.model.repository.ExecutionRepository
import cz.palda97.lpclient.model.repository.ServerRepository

class ExecutionsViewModel(application: Application): AndroidViewModel(application) {

    private val executionRepository: ExecutionRepository = Injector.executionRepository
    private val serverRepository: ServerRepository = Injector.serverRepository

    private fun onServerToFilterChange() {
        //TODO
    }
    var serverToFilter: ServerInstance?
        get() = serverRepository.serverToFilter
        private set(value) {
            val changed = value != serverRepository.serverToFilter
            serverRepository.serverToFilter = value
            if (changed) {
                onServerToFilterChange()
            }
        }
    fun setServerToFilterFun(serverInstance: ServerInstance?) {
        serverToFilter = serverInstance
    }

    fun refreshExecutionsButton() {
        TODO()
    }

    companion object {
        private val TAG = Injector.tag(this)
        private fun l(msg: String) = Log.d(TAG, msg)
    }
}