package cz.palda97.lpclient.model.repository

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.entities.pipeline.Pipeline
import cz.palda97.lpclient.model.entities.pipeline.PipelineFactory
import cz.palda97.lpclient.model.entities.pipeline.jsonLd
import cz.palda97.lpclient.model.entities.pipelineview.PipelineView
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.network.PipelineRetrofit
import cz.palda97.lpclient.model.repository.PipelineRepository.CacheStatus.Companion.toStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private typealias WrappedPipeline = Either<PipelineRepository.CacheStatus, Pipeline>

class PipelineRepository(
    private val serverDao: ServerInstanceDao,
    private val sharedPreferences: SharedPreferences
) {

    private val retrofitScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)

    private suspend fun getPipelineRetrofit(server: ServerInstance): Either<PipelineViewRepository.StatusCode, PipelineRetrofit> =
        Injector.pipelineViewRepository.getPipelineRetrofit(server)

    private suspend fun getPipelineRetrofit(pipelineView: PipelineView): Either<PipelineViewRepository.StatusCode, PipelineRetrofit> =
        Injector.pipelineViewRepository.getPipelineRetrofit(pipelineView)

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

    private val _currentPipeline: MutableLiveData<MailPackage<Pipeline>> = MutableLiveData()
    val currentPipeline: LiveData<MailPackage<Pipeline>>
        get() = _currentPipeline

    /**
     * @param pipeline Pipeline to be saved to shared preferences. If null, pipeline will be removed from preferences.
     */
    private fun persistPipeline(pipeline: Pipeline?) {
        if (pipeline != null) {
            sharedPreferences.edit()
                .putString(PIPELINE_JSONLD, pipeline.jsonLd())
                .apply()
        } else {
            sharedPreferences.edit()
                .remove(PIPELINE_JSONLD)
                .apply()
        }
    }

    /**
     * @return Either CacheStatus or Pipeline, depending on data in shared preferences.
     */
    private suspend fun restorePipeline(): WrappedPipeline {
        val serverId = sharedPreferences.getLong(PIPELINE_SERVER_ID, 0L)
        val server =
            serverDao.findById(serverId) ?: return Either.Left(CacheStatus.SERVER_NOT_FOUND)
        val pipelineJsonLd = sharedPreferences.getString(PIPELINE_JSONLD, null)
            ?: return Either.Left(CacheStatus.NO_PIPELINE_TO_LOAD)
        val mail = PipelineFactory(server, pipelineJsonLd).pipeline
        return if (mail.mailContent != null) Either.Right(mail.mailContent!!) else Either.Left(
            CacheStatus.PARSING_ERROR
        )
    }

    private fun persistStatus(cacheStatus: CacheStatus?) {
        if (cacheStatus != null) {
            sharedPreferences.edit()
                .putString(CACHE_STATUS, cacheStatus.name)
                .apply()
        } else {
            sharedPreferences.edit()
                .remove(CACHE_STATUS)
                .apply()
        }
    }

    private fun restoreStatus(): CacheStatus? =
        sharedPreferences.getString(CACHE_STATUS, null)?.toStatus

    /**
     * This method is for saving pipeline at the end of lifecycle
     */
    fun savePipeline(pipeline: Pipeline) {
        persistPipeline(pipeline)
        persistStatus(null)
        retrofitScope.launch {
            Injector.componentRepository.cache(pipeline.components, pipeline.pipelineView.serverId)
        }
        _currentPipeline.postValue(MailPackage(pipeline))
    }

    private fun saveStatus(cacheStatus: CacheStatus) {
        persistStatus(cacheStatus)
        persistPipeline(null)
        _currentPipeline.postValue(MailPackage.brokenPackage(cacheStatus.name))
    }

    private fun save(wrappedPipeline: WrappedPipeline) = when (wrappedPipeline) {
        is Either.Left -> saveStatus(wrappedPipeline.value)
        is Either.Right -> savePipeline(wrappedPipeline.value)
    }

    private fun persistIds(pipelineView: PipelineView) {
        sharedPreferences.edit()
            .putLong(PIPELINE_SERVER_ID, pipelineView.serverId)
            .putString(PIPELINE_ID, pipelineView.id)
            .apply()
    }

    private fun restoreIds(): PipelineView? {
        val serverId = sharedPreferences.getLong(PIPELINE_SERVER_ID, 0)
        if (serverId == 0L) {
            return null
        }
        val pipelineId = sharedPreferences.getString(PIPELINE_ID, null) ?: return null
        return PipelineView("", pipelineId, serverId)
    }

    suspend fun cachePipeline(pipelineView: PipelineView) {
        persistIds(pipelineView)
        _currentPipeline.postValue(MailPackage.loadingPackage())
        val wrappedPipeline = downloadPipeline(pipelineView)
        save(wrappedPipeline)
    }

    suspend fun retryCachePipeline() {
        val pipelineView = restoreIds() ?: return saveStatus(CacheStatus.NO_PIPELINE_TO_LOAD)
        cachePipeline(pipelineView)
    }

    private val WrappedPipeline.mail
        get() = when (this) {
            is Either.Left -> MailPackage.brokenPackage(this.value.name)
            is Either.Right -> MailPackage(this.value)
        }

    private suspend fun recover() {
        val status = restoreStatus()
        val wrappedPipeline: WrappedPipeline = if (status != null) {
            Either.Left(status)
        } else {
            restorePipeline()
        }
        _currentPipeline.postValue(wrappedPipeline.mail)
    }

    init {
        retrofitScope.launch {
            recover()
        }
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
        private const val PIPELINE_JSONLD = "PIPELINE_JSONLD"
        private const val PIPELINE_SERVER_ID = "PIPELINE_SERVER_ID"
        private const val PIPELINE_ID = "PIPELINE_ID"
        private const val CACHE_STATUS = "CACHE_STATUS"
    }
}