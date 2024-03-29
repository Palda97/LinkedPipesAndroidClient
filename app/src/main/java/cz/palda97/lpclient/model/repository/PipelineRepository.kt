package cz.palda97.lpclient.model.repository

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.db.dao.PipelineDao
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.entities.pipeline.Pipeline
import cz.palda97.lpclient.model.entities.pipeline.PipelineFactory
import cz.palda97.lpclient.model.entities.pipeline.jsonLd
import cz.palda97.lpclient.model.entities.pipelineview.PipelineView
import cz.palda97.lpclient.model.entities.pipelineview.PipelineViewFactory
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.network.PipelineRetrofit
import cz.palda97.lpclient.model.network.PipelineRetrofit.Companion.pipelineRetrofit
import cz.palda97.lpclient.model.network.RetrofitHelper
import cz.palda97.lpclient.model.network.WebUrlGenerator
import cz.palda97.lpclient.model.repository.PipelineRepository.CacheStatus.Companion.toStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private typealias WrappedPipeline = Either<PipelineRepository.CacheStatus, Pipeline>

/**
 * Repository for working with [Pipeline].
 */
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
        // no connect       downloading     parsing         neutral                 loading
        companion object {
            val String.toStatus
                get() = try {
                    CacheStatus.valueOf(this)
                } catch (e: IllegalArgumentException) {
                    CacheStatus.INTERNAL_ERROR
                }
        }
    }

    /**
     * Downloads and parses the pipeline.
     * @param pipelineView Pipeline id and server id of the pipeline to download.
     * @return [Pipeline] or [CacheStatus] on error.
     */
    suspend fun downloadPipeline(pipelineView: PipelineView): WrappedPipeline {
        val server = serverDao.findById(pipelineView.serverId)
            ?: return Either.Left(CacheStatus.SERVER_NOT_FOUND)
        val pipelineString =
            when (val res = Injector.pipelineViewRepository.downloadPipelineString(pipelineView)) {
                is Either.Left -> return Either.Left(CacheStatus.DOWNLOAD_ERROR)
                is Either.Right -> res.value
            }
        val factory = PipelineFactory(server, pipelineString)
        return factory.parse().mailContent?.let {
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

    /**
     * LiveData with the whole [Pipeline].
     */
    val livePipeline: LiveData<MailPackage<Pipeline>>
        get() = mediatorPipeline

    /**
     * Replace the pipeline in database. Optionally cache components.
     * @param pipeline Pipeline to be saved.
     * @param cacheComponents If [ComponentRepository.cache] should be called.
     * @return [Job] related to the components caching.
     */
    suspend fun savePipeline(pipeline: Pipeline, cacheComponents: Boolean): Job? {
        persistStatus(null)
        persistPipeline(pipeline)
        if (cacheComponents) {
            return retrofitScope.launch {
                Injector.componentRepository.cache(pipeline.components)
            }
        }
        return null
    }

    private suspend fun saveStatus(cacheStatus: CacheStatus) {
        persistPipeline(null)
        persistStatus(cacheStatus)
    }

    private suspend fun save(wrappedPipeline: WrappedPipeline) = when(wrappedPipeline) {
        is Either.Left -> saveStatus(wrappedPipeline.value)
        is Either.Right -> {
            savePipeline(wrappedPipeline.value, true)
            Unit
        }
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

    var currentPipelineId = ""
    var currentServerId = 0L

    /**
     * Sets [livePipeline] to [loading][MailPackage.Status.LOADING], then downloads and stores the pipeline.
     * @param pipelineView [PipelineView] containing the pipeline id and server id.
     * @return [Job] related to the pipeline caching.
     */
    fun cachePipelineInit(pipelineView: PipelineView): Job {
        currentPipelineId = pipelineView.id
        currentServerId = pipelineView.serverId
        desyncLivePipeline()
        return retrofitScope.launch {
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

    /**
     * Try downloading and storing pipeline again.
     */
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

    private suspend fun getPipelineRetrofit(): Either<CacheStatus, PipelineRetrofit> {
        val server = serverDao.findById(currentServerId) ?: return Either.Left(CacheStatus.SERVER_NOT_FOUND)
        return getPipelineRetrofit(server)
    }
    private suspend fun getPipelineRetrofit(server: ServerInstance): Either<CacheStatus, PipelineRetrofit> = try {
        Either.Right(RetrofitHelper.getBuilder(server, server.frontendUrl).pipelineRetrofit)
    } catch (e: IllegalArgumentException) {
        Either.Left(CacheStatus.SERVER_NOT_FOUND)
    }

    private suspend fun createPipelineRequest(server: ServerInstance): Either<CacheStatus, PipelineView> {
        val retrofit = when(val res = getPipelineRetrofit(server)) {
            is Either.Left -> return Either.Left(res.value)
            is Either.Right -> res.value
        }
        val call = retrofit.createPipeline(RetrofitHelper.stringToFormData(PipelineRetrofit.OPTIONS))
        val text = RetrofitHelper.getStringFromCall(call) ?: return Either.Left(CacheStatus.PARSING_ERROR)
        val factory = PipelineViewFactory(server, text)
        val wrapper = factory.serverWithPipelineViews.mailContent ?: return Either.Left(CacheStatus.PARSING_ERROR)
        if (wrapper.pipelineViewList.size != 1) {
            return Either.Left(CacheStatus.PARSING_ERROR)
        }
        val pipelineView = wrapper.pipelineViewList.first().pipelineView
        return Either.Right(pipelineView)
    }

    private val _liveNewPipeline = MutableLiveData<Either<CacheStatus, PipelineView>>()

    /**
     * LiveData with information about new pipeline creation. Contains either [CacheStatus] or
     * [PipelineView] on success.
     */
    val liveNewPipeline: LiveData<Either<CacheStatus, PipelineView>>
        get() = _liveNewPipeline

    /**
     * Sets [liveNewPipeline] to [NO_PIPELINE_TO_LOAD][CacheStatus.NO_PIPELINE_TO_LOAD].
     */
    fun resetLiveNewPipeline() {
        _liveNewPipeline.value = Either.Left(CacheStatus.NO_PIPELINE_TO_LOAD)
    }

    /**
     * Sends request to create new pipeline and propagates result through [liveNewPipeline].
     * @param server Server to create pipeline on.
     * @return [Job] related to the pipeline creation process.
     */
    fun createPipelineInit(server: ServerInstance): Job {
        _liveNewPipeline.value = Either.Left(CacheStatus.INTERNAL_ERROR)
        return retrofitScope.launch {
            createPipeline(server)
        }
    }

    private suspend fun createPipeline(server: ServerInstance) {
        val pipelineView = when(val res = createPipelineRequest(server)) {
            is Either.Left -> {
                _liveNewPipeline.postValue(res)
                return
            }
            is Either.Right -> res.value
        }
        Injector.pipelineViewRepository.insertPipelineView(pipelineView)
        _liveNewPipeline.postValue(Either.Right(pipelineView))
    }

    enum class StatusCode {
        NO_CONNECT, INTERNAL_ERROR, NEUTRAL, UPLOADING_ERROR, PARSING_ERROR, OK, UPLOAD_IN_PROGRESS;

        companion object {
            val String?.toStatus
                get() = if (this == null) {
                    INTERNAL_ERROR
                } else {
                    try {
                        valueOf(this)
                    } catch (e: IllegalArgumentException) {
                        INTERNAL_ERROR
                    }
                }
        }
    }

    private suspend fun uploadPipelineRequest(): StatusCode {
        val pipeline = pipelineDao.exportPipeline(currentPipelineId) ?: return StatusCode.PARSING_ERROR
        val retrofit = when(val res = getPipelineRetrofit()) {
            is Either.Left -> return StatusCode.NO_CONNECT
            is Either.Right -> res.value
        }
        val call = retrofit.updatePipeline(pipeline.pipelineView.idNumber, RetrofitHelper.stringToFormData(pipeline.jsonLd()))
        val text = RetrofitHelper.getStringFromCall(call) ?: return StatusCode.UPLOADING_ERROR
        if (text.isNotEmpty()) {
            return StatusCode.INTERNAL_ERROR
        }
        return StatusCode.OK
    }

    private val _liveUploadStatus = MutableLiveData<StatusCode>()

    /**
     * LiveData with information about about pipeline uploading.
     */
    val liveUploadStatus: LiveData<StatusCode>
        get() = _liveUploadStatus

    /**
     * Sets [liveUploadStatus] to [NEUTRAL][StatusCode.NEUTRAL].
     */
    fun resetUploadStatus() {
        _liveUploadStatus.value = StatusCode.NEUTRAL
    }

    /**
     * Sets [liveUploadStatus] to [INTERNAL_ERROR][StatusCode.INTERNAL_ERROR].
     */
    fun cannotSavePipelineForUpload() {
        _liveUploadStatus.value = StatusCode.INTERNAL_ERROR
    }

    /**
     * Sets [liveUploadStatus] to [UPLOAD_IN_PROGRESS][StatusCode.UPLOAD_IN_PROGRESS],
     * upload current pipeline to the server and propagate result through liveUploadStatus.
     */
    suspend fun uploadPipeline() {
        _liveUploadStatus.postValue(StatusCode.UPLOAD_IN_PROGRESS)
        val status = uploadPipelineRequest()
        _liveUploadStatus.postValue(status)
    }

    var currentPipelineView: PipelineView? = null

    /**
     * Insert [currentPipelineView] to the database.
     */
    suspend fun insertCurrentPipelineView() = currentPipelineView?.let {
        Injector.pipelineViewRepository.insertPipelineView(it)
    }

    /**
     * Generate web link for current pipeline.
     * @return Web frontend URL for the pipeline.
     * @see [WebUrlGenerator.pipeline]
     */
    suspend fun pipelineLink(): String? {
        val server = serverDao.findById(currentServerId) ?: return null
        return WebUrlGenerator.pipeline(server.frontendUrl, currentPipelineId)
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
        const val PIPELINE_SERVER_ID = "PIPELINE_SERVER_ID"
        const val PIPELINE_ID = "PIPELINE_ID"
        const val CACHE_STATUS = "CACHE_STATUS"
    }
}