package cz.palda97.lpclient.viewmodel.executions

import android.app.Application
import androidx.lifecycle.*
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.*
import cz.palda97.lpclient.model.entities.execution.ServerWithExecutions
import cz.palda97.lpclient.model.repository.*
import cz.palda97.lpclient.viewmodel.CommonViewModel
import kotlinx.coroutines.*

/**
 * ViewModel for the [ExecutionsFragment][cz.palda97.lpclient.view.executions.ExecutionsFragment].
 */
class ExecutionsViewModel(application: Application) : AndroidViewModel(application) {

    private val executionRepository: ExecutionRepository = Injector.executionRepository
    private val serverRepository: ServerRepository = Injector.serverRepository
    private val detailRepository: ExecutionDetailRepository = Injector.executionDetailRepository

    private val retrofitScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)
    private val dbScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)

    private var lastSilent: Boolean = false

    /**
     * LiveData with executions, not intended for deletion, sorted
     * and transformed into list of [ExecutionV].
     */
    val liveExecutions: LiveData<MailPackage<List<ExecutionV>>>
        get() = executionRepository.liveExecutions.map {
            val mail = executionTransformation(it)
            lastSilent = false
            mail
        }

    private fun executionTransformation(it: MailPackage<List<ServerWithExecutions>>?): MailPackage<List<ExecutionV>> {
            val mail = it ?: return MailPackage.loadingPackage<List<ExecutionV>>()
            return when (mail.status) {
                MailPackage.Status.OK -> {
                    mail.mailContent!!
                    val list = mail.mailContent.flatMap { serverWithExecutions ->
                        serverWithExecutions.executionList.filter {
                            !(it.mark != null || executionRepository.deleteRepo.toBeDeleted(it.execution))
                        }.map {
                            ExecutionV(it.execution, serverWithExecutions.server.name)
                        }.sortedByDescending {
                            it.id
                        }
                    }
                    MailPackage(
                        list,
                        MailPackage.Status.OK,
                        if (lastSilent)
                            SCROLL
                        else
                            ""
                    )
                }
                MailPackage.Status.ERROR -> MailPackage.brokenPackage<List<ExecutionV>>(mail.msg)
                MailPackage.Status.LOADING -> MailPackage.loadingPackage<List<ExecutionV>>()
            }
        }

    /** @see RepositoryRoutines.refresh */
    fun refreshExecutionsButton() {
        retrofitScope.launch {
            CommonViewModel.refreshAndNotify()
        }
    }

    private suspend fun deleteRoutine(executionV: ExecutionV) {
        val execution = executionRepository.find(executionV.id) ?: return
        executionRepository.markForDeletion(execution)
        executionRepository.deleteRepo.addPending(execution, DELETE_DELAY)
    }

    /**
     * Add delete request of this execution to [DeleteRepository][cz.palda97.lpclient.model.repository.DeleteRepository].
     */
    fun deleteExecution(executionV: ExecutionV) {
        retrofitScope.launch {
            deleteRoutine(executionV)
        }
    }

    private suspend fun cancelRoutine(executionV: ExecutionV) {
        val execution = executionRepository.find(executionV.id) ?: return
        executionRepository.unMarkForDeletion(execution)
        executionRepository.deleteRepo.cancelDeletion(execution)
    }

    /**
     * Cancel the deletion of this execution.
     */
    fun cancelDeletion(executionV: ExecutionV) {
        retrofitScope.launch {
            cancelRoutine(executionV)
        }
    }

    /**
     * Prepare execution details.
     * @return False if execution could not be found in database. Otherwise true.
     */
    suspend fun viewExecution(executionV: ExecutionV): Boolean = withContext(Dispatchers.IO) {
        val execution = executionRepository.find(executionV.id) ?: return@withContext false
        retrofitScope.launch {
            executionRepository.cacheExecutionSilently(execution)
        }
        detailRepository.cacheComponentsInit(execution)
        return@withContext true
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
        private const val DELETE_DELAY: Long = 5000L
        const val SCROLL = "SCROLL"

        /**
         * Gets an instance of [ExecutionsViewModel] tied to the owner's lifecycle.
         */
        fun getInstance(owner: ViewModelStoreOwner) = ViewModelProvider(owner).get(ExecutionsViewModel::class.java)
    }
}