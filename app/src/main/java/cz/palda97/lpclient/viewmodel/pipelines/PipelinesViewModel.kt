package cz.palda97.lpclient.viewmodel.pipelines

import android.app.Application
import androidx.lifecycle.*
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.*
import cz.palda97.lpclient.model.entities.pipelineview.PipelineView
import cz.palda97.lpclient.model.entities.pipelineview.ServerWithPipelineViews
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.network.RetrofitHelper
import cz.palda97.lpclient.model.repository.*
import cz.palda97.lpclient.viewmodel.CommonViewModel
import cz.palda97.lpclient.viewmodel.editpipeline.EditPipelineViewModel
import cz.palda97.lpclient.viewmodel.executions.ExecutionV
import kotlinx.coroutines.*

/**
 * ViewModel for the [PipelinesFragment][cz.palda97.lpclient.view.pipelines.PipelinesFragment].
 */
class PipelinesViewModel(application: Application) : AndroidViewModel(application) {

    private val pipelineViewRepository: PipelineViewRepository = Injector.pipelineViewRepository
    private val serverRepository: ServerRepository = Injector.serverRepository
    private val executionRepository: ExecutionRepository = Injector.executionRepository
    private val pipelineRepository: PipelineRepository = Injector.pipelineRepository
    private val possibleRepository: PossibleComponentRepository = Injector.possibleComponentRepository

    private val retrofitScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)
    private val dbScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)

    /**
     * LiveData with pipelineViews, not intended for deletion and sorted.
     */
    val livePipelineViews: LiveData<MailPackage<List<PipelineView>>>
        get() = pipelineViewRepository.liveServersWithPipelineViews.map {
            pipelineViewTransform(it)
        }

    private fun pipelineViewTransform(it: MailPackage<List<ServerWithPipelineViews>>?): MailPackage<List<PipelineView>> {
            l("pipelineViewTransform thread: ${Thread.currentThread().name}")
            val mail = it ?: return MailPackage.loadingPackage<List<PipelineView>>()
            if (mail.isOk) {
                mail.mailContent!!
                mail.mailContent.forEach {
                    l("pipelineViewTransform - ${it.server.id} - ${it.server.name}")
                }
                val list = mail.mailContent.flatMap {
                    it.pipelineViewList.filter { !(it.mark != null || pipelineViewRepository.deleteRepo.toBeDeleted(it.pipelineView)) }.map { it.pipelineView }.apply {
                        forEach { pipelineView ->
                            pipelineView.serverName = it.server.name
                        }
                    }.sortedByDescending {
                        it.id
                    }
                }
                l("pipelineViewTransform before ok return")
                return MailPackage(list)
            }
            if (mail.isError)
                return MailPackage.brokenPackage<List<PipelineView>>(mail.msg)
            l("still loading")
            return MailPackage.loadingPackage<List<PipelineView>>()
        }

    /** @see RepositoryRoutines.refresh */
    fun refreshButton() {
        retrofitScope.launch {
            CommonViewModel.refreshAndNotify()
        }
    }

    private suspend fun deletePipelineRoutine(pipelineView: PipelineView) {
        pipelineViewRepository.markForDeletion(pipelineView)
        pipelineViewRepository.deleteRepo.addPending(pipelineView, DELETE_DELAY)
    }

    /**
     * Add delete request of this pipelineView to [DeleteRepository][cz.palda97.lpclient.model.repository.DeleteRepository].
     */
    fun deletePipeline(pipelineView: PipelineView) {
        retrofitScope.launch {
            deletePipelineRoutine(pipelineView)
        }
    }

    private suspend fun cancelRoutine(pipelineView: PipelineView) {
        pipelineViewRepository.unMarkForDeletion(pipelineView)
        pipelineViewRepository.deleteRepo.cancelDeletion(pipelineView)
    }

    /**
     * Cancel the deletion of this pipelineView.
     */
    fun cancelDeletion(pipelineView: PipelineView) {
        dbScope.launch {
            cancelRoutine(pipelineView)
        }
    }

    enum class LaunchStatus {
        PIPELINE_NOT_FOUND, SERVER_NOT_FOUND, CAN_NOT_CONNECT, INTERNAL_ERROR, SERVER_ERROR, WAITING, OK, PROTOCOL_PROBLEM
    }

    private val _launchStatus: MutableLiveData<LaunchStatus> = MutableLiveData(LaunchStatus.WAITING)

    /**
     * LiveData with information about launching a pipeline.
     */
    val launchStatus: LiveData<LaunchStatus>
        get() = _launchStatus

    /**
     * Sets the [launchStatus] to [WAITING][LaunchStatus.WAITING].
     */
    fun resetLaunchStatus() {
        _launchStatus.value = LaunchStatus.WAITING
    }

    private fun launchStatusCodeToLiveData(statusCode: PipelineViewRepository.StatusCode) {
        _launchStatus.postValue(
            when (statusCode) {
                PipelineViewRepository.StatusCode.SERVER_ID_NOT_FOUND -> LaunchStatus.SERVER_NOT_FOUND
                PipelineViewRepository.StatusCode.NO_CONNECT -> LaunchStatus.PROTOCOL_PROBLEM
                PipelineViewRepository.StatusCode.NULL_RESPONSE -> LaunchStatus.PIPELINE_NOT_FOUND
                PipelineViewRepository.StatusCode.INTERNAL_ERROR -> LaunchStatus.CAN_NOT_CONNECT
                else -> LaunchStatus.INTERNAL_ERROR
            }
        )
    }

    private suspend fun launchPipelineRoutine(pipelineView: PipelineView) {
        val pipelineString =
            when (val res = pipelineViewRepository.downloadPipelineString(pipelineView)) {
                is Either.Left -> {
                    launchStatusCodeToLiveData(res.value)
                    return
                }
                is Either.Right -> res.value
            }
        val pipelineRetrofit =
            when (val res = pipelineViewRepository.getPipelineRetrofit(pipelineView)) {
                is Either.Left -> return
                is Either.Right -> res.value
            }
        val call = pipelineRetrofit.executePipeline(RetrofitHelper.stringToFormData(pipelineString))
        val text = RetrofitHelper.getStringFromCall(call)
        if (text == null) {
            _launchStatus.postValue(LaunchStatus.SERVER_ERROR)
            return
        }
        l(text)
        _launchStatus.postValue(LaunchStatus.OK)
    }

    /**
     * Downloads the whole pipeline based on the given pipelineView and lunches it.
     */
    fun launchPipeline(pipelineView: PipelineView) {
        retrofitScope.launch {
            launchPipelineRoutine(pipelineView)
            val server = serverRepository.activeLiveServers.value?.mailContent?.find {
                it.id == pipelineView.serverId
            }
            server?.let {
                executionRepository.cacheExecutions(Either.Left(it), true)
            }
        }
    }

    /**
     * Downloads the whole pipeline based on the given executionV and lunches it.
     */
    fun launchPipeline(executionV: ExecutionV) {
        retrofitScope.launch {
            val pipelineView = executionRepository.find(executionV.id)?.let {
                pipelineViewRepository.findPipelineViewById(it.pipelineId)
            }
            if (pipelineView == null) {
                _launchStatus.postValue(LaunchStatus.INTERNAL_ERROR)
                return@launch
            }
            launchPipelineRoutine(pipelineView)
        }
    }

    /**
     * Prepare [PipelineRepository] for editing this pipeline.
     * @param pipelineView Pipeline to be edited.
     * @param isItNewOne Is it a newly created pipeline or not.
     */
    fun editPipeline(pipelineView: PipelineView, isItNewOne: Boolean) {
        if (isItNewOne)
            pipelineRepository.resetLiveNewPipeline()
        pipelineRepository.cachePipelineInit(pipelineView)
        possibleRepository.currentServerId = pipelineView.serverId
        EditPipelineViewModel.scroll = true
    }

    /** @see PipelineRepository.createPipelineInit */
    fun createPipeline(server: ServerInstance) {
        pipelineRepository.createPipelineInit(server)
    }

    /** @see PipelineRepository.liveNewPipeline */
    val liveNewPipeline
        get() = pipelineRepository.liveNewPipeline

    companion object {
        private val l = Injector.generateLogFunction(this)

        private const val DELETE_DELAY: Long = 5000L

        /**
         * Gets an instance of [PipelinesViewModel] tied to the owner's lifecycle.
         */
        fun getInstance(owner: ViewModelStoreOwner) = ViewModelProvider(owner).get(PipelinesViewModel::class.java)
    }
}