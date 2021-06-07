package cz.palda97.lpclient.model.repository

import androidx.lifecycle.MutableLiveData
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.db.dao.ExecutionDetailDao
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.entities.execution.*
import cz.palda97.lpclient.model.entities.pipeline.Component
import cz.palda97.lpclient.model.entities.pipelineview.PipelineView
import cz.palda97.lpclient.model.network.ExecutionRetrofit
import cz.palda97.lpclient.model.network.RetrofitHelper
import cz.palda97.lpclient.model.network.WebUrlGenerator
import kotlinx.coroutines.*

class ExecutionDetailRepository(
    private val executionDao: ExecutionDetailDao,
    private val serverDao: ServerInstanceDao
) {

    enum class ExecutionDetailRepositoryStatus {
        OK, LOADING, INTERNAL_ERROR, DOWNLOADING_ERROR, PARSING_ERROR;

        companion object {
            val String?.toStatus
                get() = if (this == null) {
                    INTERNAL_ERROR
                } else {
                    try {
                        valueOf(this)
                    } catch (_: IllegalArgumentException) {
                        INTERNAL_ERROR
                    }
                }
        }
    }

    var currentExecutionId: String = ""
        private set
    var currentServerId: Long = 0L
        private set
    var currentPipelineId: String = ""
        private set
    var currentPipelineName: String = ""
        private set

    private fun initIds(execution: Execution) {
        currentExecutionId = execution.id
        currentServerId = execution.serverId
        currentPipelineId = execution.pipelineId
        currentPipelineName = execution.pipelineName
    }

    val liveDetail
        get() = executionDao.liveExecutionDetail(currentExecutionId)

    fun cacheComponentsInit(execution: Execution): Job {
        initIds(execution)
        return CoroutineScope(Dispatchers.IO).launch {
            cacheComponents(execution)
        }
    }

    val liveUpdateError = MutableLiveData<ExecutionDetailRepositoryStatus>(ExecutionDetailRepositoryStatus.OK)

    private suspend fun cacheComponents(execution: Execution) {
        executionDao.insertStatus(ExecutionDetailStatus(execution.id, ExecutionDetailRepositoryStatus.LOADING.name))
        val pipelineView = PipelineView("", execution.pipelineId, execution.serverId)
        val pipeline = when(val res = Injector.pipelineRepository.downloadPipeline(pipelineView)) {
            is Either.Left -> {
                val status = when(res.value) {
                    PipelineRepository.CacheStatus.SERVER_NOT_FOUND -> ExecutionDetailRepositoryStatus.INTERNAL_ERROR
                    PipelineRepository.CacheStatus.DOWNLOAD_ERROR -> ExecutionDetailRepositoryStatus.DOWNLOADING_ERROR
                    PipelineRepository.CacheStatus.PARSING_ERROR -> ExecutionDetailRepositoryStatus.PARSING_ERROR
                    PipelineRepository.CacheStatus.NO_PIPELINE_TO_LOAD -> ExecutionDetailRepositoryStatus.INTERNAL_ERROR
                    PipelineRepository.CacheStatus.INTERNAL_ERROR -> ExecutionDetailRepositoryStatus.INTERNAL_ERROR
                }
                executionDao.insertStatus(ExecutionDetailStatus(execution.id, status.name))
                liveUpdateError.postValue(status)
                return
            }
            is Either.Right -> res.value
        }
        val components = when(val res = downloadExecutionComponents(execution, pipeline.components)) {
            is Either.Left -> {
                executionDao.insertStatus(ExecutionDetailStatus(execution.id, res.value.name))
                liveUpdateError.postValue(res.value)
                return
            }
            is Either.Right -> res.value
        }
        executionDao.insertComponent(components)
        executionDao.insertStatus(ExecutionDetailStatus(execution.id, ExecutionDetailRepositoryStatus.OK.name))
    }

    private suspend fun getExecutionRetrofit(execution: Execution): Either<ExecutionDetailRepositoryStatus, ExecutionRetrofit> {
        return when(val res = Injector.executionRepository.getExecutionRetrofit(execution)) {
            is Either.Left -> Either.Left(ExecutionDetailRepositoryStatus.DOWNLOADING_ERROR)
            is Either.Right -> Either.Right(res.value)
        }
    }

    private suspend fun downloadExecutionComponents(execution: Execution, pipelineComponents: List<Component>): Either<ExecutionDetailRepositoryStatus, List<ExecutionDetailComponent>> {
        val retrofit = when(val res = getExecutionRetrofit(execution)) {
            is Either.Left -> return Either.Left(res.value)
            is Either.Right -> res.value
        }
        val call = retrofit.execution(execution.idNumber)
        val json = RetrofitHelper.getStringFromCall(call) ?: return Either.Left(ExecutionDetailRepositoryStatus.DOWNLOADING_ERROR)
        val factory = ExecutionDetailFactory(json, execution.id, pipelineComponents)
        val components = factory.parse() ?: return Either.Left(ExecutionDetailRepositoryStatus.PARSING_ERROR)
        return Either.Right(components)
    }

    suspend fun executionLink(): String? {
        val server = serverDao.findById(currentServerId) ?: return null
        return WebUrlGenerator.execution(server.frontendUrl, currentExecutionId, currentPipelineId)
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
    }
}