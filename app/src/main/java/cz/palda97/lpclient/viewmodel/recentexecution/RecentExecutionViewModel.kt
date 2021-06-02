package cz.palda97.lpclient.viewmodel.recentexecution

import android.app.Application
import androidx.lifecycle.*
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.viewmodel.executions.ExecutionV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for the [RecentExecutionFragment][cz.palda97.lpclient.view.recentexecution.RecentExecutionFragment].
 */
class RecentExecutionViewModel(application: Application) : AndroidViewModel(application) {

    private val noveltyRepository = Injector.executionNoveltyRepository

    private val dbScope
        get() = CoroutineScope(Dispatchers.IO)

    val liveExecution: LiveData<List<ExecutionV>>
        get() = noveltyRepository.liveRecent.map {
            it.map {
                ExecutionV(it)
            }.sortedByDescending {
                it.id
            }
        }

    fun resetRecent(executions: List<ExecutionV>) = dbScope.launch {
        noveltyRepository.resetRecent(
            executions.map { it.id }
        )
    }

    companion object {
        private val l = Injector.generateLogFunction(this)

        /**
         * Gets an instance of [RecentExecutionViewModel] tied to the owner's lifecycle.
         */
        fun getInstance(owner: ViewModelStoreOwner) = ViewModelProvider(owner).get(RecentExecutionViewModel::class.java)
    }
}