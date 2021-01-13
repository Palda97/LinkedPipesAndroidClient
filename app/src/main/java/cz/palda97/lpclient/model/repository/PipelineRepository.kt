package cz.palda97.lpclient.model.repository

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.db.dao.PipelineDao
import cz.palda97.lpclient.model.db.dao.PipelineViewDao
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.entities.pipeline.Pipeline
import cz.palda97.lpclient.model.entities.pipeline.PipelineFactory
import cz.palda97.lpclient.model.entities.pipeline.jsonLd
import cz.palda97.lpclient.model.entities.pipelineview.PipelineView
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.network.PipelineRetrofit
import cz.palda97.lpclient.model.repository.PipelineRepository.CacheStatus.Companion.toStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private typealias WrappedPipeline = Either<PipelineRepository.CacheStatus, Pipeline>

class PipelineRepository(
    private val serverDao: ServerInstanceDao,
    //private val pipelineViewDao: PipelineViewDao,
    private val pipelineDao: PipelineDao,
    private val sharedPreferences: SharedPreferences
) {

    private val retrofitScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)

    /*private suspend fun getPipelineRetrofit(server: ServerInstance): Either<PipelineViewRepository.StatusCode, PipelineRetrofit> =
        Injector.pipelineViewRepository.getPipelineRetrofit(server)

    private suspend fun getPipelineRetrofit(pipelineView: PipelineView): Either<PipelineViewRepository.StatusCode, PipelineRetrofit> =
        Injector.pipelineViewRepository.getPipelineRetrofit(pipelineView)*/

    enum class CacheStatus {
        SERVER_NOT_FOUND, DOWNLOAD_ERROR, PARSING_ERROR, NO_PIPELINE_TO_LOAD, INTERNAL_ERROR;

        companion object {
            val String.toStatus
                get() = try {
                    CacheStatus.valueOf(this)
                } catch (e: IllegalArgumentException) {
                    CacheStatus.INTERNAL_ERROR
                }
        }
    }

    private suspend fun downloadPipeline(pipelineView: PipelineView): WrappedPipeline {
        val server = serverDao.findById(pipelineView.serverId)
            ?: return Either.Left(CacheStatus.SERVER_NOT_FOUND)
        val pipelineString =
            when (val res = Injector.pipelineViewRepository.downloadPipelineString(pipelineView)) {
                is Either.Left -> return Either.Left(CacheStatus.DOWNLOAD_ERROR)
                is Either.Right -> res.value
            }
        return PipelineFactory(server, pipelineString).pipeline.mailContent?.let {
            Either.Right<CacheStatus, Pipeline>(it)
        } ?: Either.Left(CacheStatus.PARSING_ERROR)
    }

    private val WrappedPipeline.mail
        get() = when (this) {
            is Either.Left -> MailPackage.brokenPackage(this.value.name)
            is Either.Right -> MailPackage(this.value)
        }

    private suspend fun persistPipeline(pipeline: Pipeline?) {
        if (pipeline == null) {
            pipelineDao.deletePipeline()
        } else {
            pipelineDao.replacePipeline(pipeline)
        }
    }

    private suspend fun persistStatus(cacheStatus: CacheStatus?) {
        if (cacheStatus == null) {
            sharedPreferences.edit()
                .remove(CACHE_STATUS)
                .apply()
        } else {
            sharedPreferences.edit()
                .putString(CACHE_STATUS, cacheStatus.name)
                .apply()
            mediatorPipeline.postValue(MailPackage.brokenPackage(cacheStatus.name))
        }
    }

    private val mediatorPipeline: MediatorLiveData<MailPackage<Pipeline>> = MediatorLiveData()
    private var lastLivePipeline: LiveData<MailPackage<Pipeline>> = MutableLiveData()

    private fun persistIds(pipelineView: PipelineView) {
        sharedPreferences.edit()
            .putString(PIPELINE_ID, pipelineView.id)
            .putLong(PIPELINE_SERVER_ID, pipelineView.serverId)
            .apply()
    }

    val livePipeline: LiveData<MailPackage<Pipeline>>
        get() = mediatorPipeline

    suspend fun savePipeline(pipeline: Pipeline, cacheComponents: Boolean) {
        persistStatus(null)
        persistPipeline(pipeline)
        if (cacheComponents) {
            retrofitScope.launch {
                Injector.componentRepository.cache(pipeline.components)
            }
        }
    }

    private suspend fun saveStatus(cacheStatus: CacheStatus) {
        persistPipeline(null)
        persistStatus(cacheStatus)
    }

    private suspend fun save(wrappedPipeline: WrappedPipeline) = when(wrappedPipeline) {
        is Either.Left -> saveStatus(wrappedPipeline.value)
        is Either.Right -> savePipeline(wrappedPipeline.value, true)
    }

    private val cachePipelineMutex: Mutex = Mutex()

    private fun desyncLivePipeline() {
        mediatorPipeline.removeSource(lastLivePipeline)
        mediatorPipeline.value = MailPackage.loadingPackage()
    }

    private suspend fun desyncLivePipelineOnMain() = withContext(Dispatchers.Main) {
        desyncLivePipeline()
    }

    private suspend fun syncLivePipeline(pipelineId: String) = withContext(Dispatchers.Main) {
        lastLivePipeline = pipelineDao.livePipeline(pipelineId)
        mediatorPipeline.addSource(lastLivePipeline) {
            mediatorPipeline.postValue(it)
        }
    }

    fun cachePipelineInit(pipelineView: PipelineView) {
        desyncLivePipeline()
        retrofitScope.launch {
            cachePipeline(pipelineView, false)
        }
    }

    private suspend fun cachePipeline(pipelineView: PipelineView, desync: Boolean = true) = cachePipelineMutex.withLock {
        if (desync)
            desyncLivePipelineOnMain()
        persistIds(pipelineView)
        val wrappedPipeline = downloadPipeline(pipelineView)
        save(wrappedPipeline)
        syncLivePipeline(pipelineView.id)
    }

    private fun restoreIds(): PipelineView? {
        val serverId = sharedPreferences.getLong(PIPELINE_SERVER_ID, 0)
        if (serverId == 0L) {
            return null
        }
        val pipelineId = sharedPreferences.getString(PIPELINE_ID, null) ?: return null
        return PipelineView("", pipelineId, serverId)
    }

    suspend fun retryCachePipeline() {
        val pipelineView = restoreIds() ?: return saveStatus(CacheStatus.NO_PIPELINE_TO_LOAD)
        cachePipeline(pipelineView)
    }

    private fun restoreStatus(): CacheStatus? = sharedPreferences.getString(CACHE_STATUS, null)?.toStatus

    private fun recover() {
        restoreStatus()?.let {
            mediatorPipeline.postValue(MailPackage.brokenPackage(it.name))
        }
    }

    init {
        recover()
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
        const val PIPELINE_SERVER_ID = "PIPELINE_SERVER_ID"
        const val PIPELINE_ID = "PIPELINE_ID"
        const val CACHE_STATUS = "CACHE_STATUS"
    }
}