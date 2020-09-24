package cz.palda97.lpclient.viewmodel.executions

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.*
import cz.palda97.lpclient.model.repository.ExecutionRepository
import cz.palda97.lpclient.model.repository.ServerRepository
import java.util.*

class ExecutionsViewModel(application: Application) : AndroidViewModel(application) {

    private val executionRepository: ExecutionRepository = Injector.executionRepository
    private val serverRepository: ServerRepository = Injector.serverRepository

    private val executionVList: List<ExecutionV> = listOf(
        ExecutionV("1", "Home", "random pipeline", "1.8.2020", ExecutionStatus.FINISHED),
        ExecutionV("2", "Work", "serious pl", "1.9.2020", ExecutionStatus.FINISHED),
        ExecutionV("3", "Test", "haha", "15.9.2020", ExecutionStatus.RUNNING),
        ExecutionV("4", "Home", "asdf", "9.6.2020", ExecutionStatus.FAILED),
        ExecutionV("5", "Home", "my pipeline", "20.9.2020", ExecutionStatus.FINISHED)
    )

    private val _liveExecutions: MutableLiveData<MailPackage<List<ExecutionV>>> =
        MutableLiveData(MailPackage(executionVList))
    val liveExecutions: LiveData<MailPackage<List<ExecutionV>>>
        get() = _liveExecutions

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