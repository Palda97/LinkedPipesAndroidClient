package cz.palda97.lpclient.model.repository

import androidx.lifecycle.MutableLiveData
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.db.dao.PipelineDao
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.entities.pipeline.Component
import cz.palda97.lpclient.model.entities.pipeline.Configuration
import cz.palda97.lpclient.model.entities.pipeline.PipelineFactory
import cz.palda97.lpclient.model.entities.pipeline.Template
import cz.palda97.lpclient.model.entities.possiblecomponent.PossibleComponent
import cz.palda97.lpclient.model.entities.possiblecomponent.PossibleComponentFactory
import cz.palda97.lpclient.model.entities.possiblecomponent.PossibleStatus
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.network.PipelineRetrofit
import cz.palda97.lpclient.model.network.PipelineRetrofit.Companion.pipelineRetrofit
import cz.palda97.lpclient.model.network.RetrofitHelper
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Repository for working with [PossibleComponents][PossibleComponent]
 * and [PossibleStatuses][PossibleStatus]. Set [currentServerId] before editing pipeline.
 * @see currentServerId
 */
class PossibleComponentRepository(
    private val serverDao: ServerInstanceDao,
    private val pipelineDao: PipelineDao
) {

    enum class StatusCode {
        NO_CONNECT, INTERNAL_ERROR, DOWNLOADING_ERROR, PARSING_ERROR, OK, DOWNLOAD_IN_PROGRESS, SERVER_NOT_FOUND;

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

    private suspend fun getPipelineRetrofit(server: ServerInstance): Either<StatusCode, PipelineRetrofit> =
        try {
            Either.Right(RetrofitHelper.getBuilder(server, server.frontendUrl).pipelineRetrofit)
        } catch (e: IllegalArgumentException) {
            l("getPipelineRetrofit $e")
            Either.Left(StatusCode.NO_CONNECT)
        }

    private suspend fun downloadPossibleComponents(server: ServerInstance): Either<StatusCode, List<PossibleComponent>> {
        val retrofit = when(val res = getPipelineRetrofit(server)) {
            is Either.Left -> return Either.Left(res.value)
            is Either.Right -> res.value
        }
        return downloadPossibleComponents(retrofit, server.id)
    }
    private suspend fun downloadPossibleComponents(retrofit: PipelineRetrofit, serverId: Long): Either<StatusCode, List<PossibleComponent>> {
        val call = retrofit.componentList()
        val text = RetrofitHelper.getStringFromCall(call) ?: return Either.Left(StatusCode.DOWNLOADING_ERROR)
        val factory = PossibleComponentFactory(text, serverId)
        val list = factory.parse() ?: return Either.Left(StatusCode.PARSING_ERROR)
        return Either.Right(list)
    }

    private suspend fun persistStatus(status: PossibleStatus) {
        pipelineDao.insertPossibleStatus(status)
    }

    private suspend fun persistPossibleComponents(list: List<PossibleComponent>) {
        pipelineDao.insertPossibleComponent(list)
    }

    /**
     * Download and stores [PossibleComponents][PossibleComponent] that belong to the selected server.
     */
    suspend fun cachePossibleComponents(server: ServerInstance) {
        pipelineDao.prepareForPossibleComponentsDownload(server.id)
        //persistStatus(PossibleStatus(server.id, StatusCode.DOWNLOAD_IN_PROGRESS))
        val status = PossibleStatus(server.id, when(val res = downloadPossibleComponents(server)) {
            is Either.Left -> res.value
            is Either.Right -> {
                persistPossibleComponents(res.value)
                StatusCode.OK
            }
        })
        persistStatus(status)
    }

    private suspend fun downloadPossibleComponents(servers: List<ServerInstance>) = coroutineScope {
        val jobs = servers.map {
            async {
                it to downloadPossibleComponents(it)
            }
        }
        jobs.map {
            it.await()
        }
    }

    private suspend fun persistStatus(list: List<PossibleStatus>) {
        pipelineDao.insertPossibleStatus(list)
    }

    /**
     * Download and stores [PossibleComponents][PossibleComponent] that belong to the selected servers.
     */
    suspend fun cachePossibleComponents(servers: List<ServerInstance>) {
        pipelineDao.prepareForPossibleComponentsDownload(servers.map { it.id })
        val list = downloadPossibleComponents(servers)
        val statuses = list.map {
            val (server, either) = it
            PossibleStatus(server.id, when(either) {
                is Either.Left -> either.value
                is Either.Right -> {
                    persistPossibleComponents(either.value)
                    StatusCode.OK
                }
            })
        }
        persistStatus(statuses)
    }

    /**
     * Server id of a pipeline that is being edited.
     */
    var currentServerId: Long = 0L

    /**
     * LiveData with [PossibleStatus] and [PossibleComponents][PossibleComponent]
     * that belong to [current server][currentServerId].
     */
    val liveComponents
        get() = pipelineDao.livePossibleComponents(currentServerId)

    /**
     * Position of last selected [PossibleComponent] in [AddComponentDialog][cz.palda97.lpclient.view.editpipeline.AddComponentDialog].
     */
    var lastSelectedComponentPosition: Int? = null

    /**
     * Location where a new [Component] will be created.
     */
    var coords: Pair<Int, Int>? = null
        private set

    /**
     * Prepare this repository for possible creation of a new [Component].
     */
    fun prepareForNewComponent(newCoords: Pair<Int, Int>) {
        coords = newCoords
        lastSelectedComponentPosition = null
        mutableLiveAddComponentStatus.value = StatusCode.OK
    }

    /**
     * Downloads a [Configuration] for a new [Component] from [current server][currentServerId].
     * @param component This method will download configuration of this component.
     * @param newComponentId Newly generated id for a new [Component].
     * @return Configuration or [StatusCode] on error.
     */
    suspend fun downloadDefaultConfiguration(component: PossibleComponent, newComponentId: String): Either<StatusCode, Configuration> {
        val server = serverDao.findById(currentServerId) ?: return Either.Left(StatusCode.SERVER_NOT_FOUND)
        return downloadDefaultConfiguration(component, newComponentId, server)
    }

    /**
     * Downloads a [Configuration] for a new [Component].
     * @param component This method will download configuration of this component.
     * @param newComponentId Newly generated id for a new [Component].
     * @param server Server from which the configuration will be downloaded.
     * @return Configuration or [StatusCode] on error.
     */
    suspend fun downloadDefaultConfiguration(component: PossibleComponent, newComponentId: String, server: ServerInstance): Either<StatusCode, Configuration> {
        val retrofit = when(val res = getPipelineRetrofit(server)) {
            is Either.Left -> return Either.Left(res.value)
            is Either.Right -> res.value
        }
        val call = retrofit.componentDefaultConfiguration(component.id)
        val text = RetrofitHelper.getStringFromCall(call) ?: return Either.Left(StatusCode.DOWNLOADING_ERROR)
        val factory = PipelineFactory(server, text)
        val configuration = factory.parseConfigurationOnly(newComponentId).mailContent ?: return Either.Left(StatusCode.PARSING_ERROR)
        return Either.Right(configuration)
    }

    /**
     * Insert [Component] to the database.
     */
    suspend fun persistComponent(component: Component) {
        pipelineDao.insertComponent(component)
    }

    /**
     * Insert [Configuration] to the database.
     */
    suspend fun persistConfiguration(configuration: Configuration) {
        pipelineDao.insertConfiguration(configuration)
    }

    /**
     * LiveData with information about creating new [Component].
     */
    val mutableLiveAddComponentStatus = MutableLiveData<StatusCode>()

    /**
     * Downloads a configuration for template.
     */
    private suspend fun downloadTemplateConfiguration(component: PossibleComponent, retrofit: PipelineRetrofit): Either<StatusCode, Configuration> {
        val call = retrofit.templateConfiguration(component.id)
        val text = RetrofitHelper.getStringFromCall(call) ?: return Either.Left(StatusCode.DOWNLOADING_ERROR)
        val factory = PipelineFactory(null, text)
        val configuration =  factory.parseConfigurationOnly().mailContent ?: return Either.Left(StatusCode.PARSING_ERROR)
        return Either.Right(configuration)
    }

    /**
     * Looks for the component's template, the template's template and so on until there is a root template.
     * @return List of all templates it found and the root template.
     */
    private tailrec suspend fun getTemplateBranch(component: PossibleComponent, serverId: Long, collected: MutableList<PossibleComponent> = ArrayList()): Pair<List<PossibleComponent>, PossibleComponent>? {
        if (component.templateId == null) {
            return collected to component
        }
        collected.add(component)
        val parent = pipelineDao.findPossibleComponentByIds(component.templateId, serverId) ?: return null
        return getTemplateBranch(parent, serverId, collected)
    }

    /**
     * Construct a list of all templates of this component paired with their configurations.
     * @return List of all templates of this component paired with their configurations and a root template id.
     */
    suspend fun getTemplatesAndConfigurations(component: PossibleComponent) = coroutineScope<Either<StatusCode, Pair<List<Pair<Template, Configuration>>, String>>> {
        val (possibles, rootTemplate) = getTemplateBranch(component, currentServerId) ?: return@coroutineScope Either.Left(StatusCode.DOWNLOADING_ERROR)
        val server = serverDao.findById(currentServerId) ?: return@coroutineScope Either.Left(StatusCode.SERVER_NOT_FOUND)
        val retrofit = when(val res = getPipelineRetrofit(server)) {
            is Either.Left -> return@coroutineScope Either.Left(res.value)
            is Either.Right -> res.value
        }
        val jobs = possibles.map {
            async {
                it to downloadTemplateConfiguration(it, retrofit)
            }
        }
        val pairs = jobs.map {
            val pair = it.await()
            val configuration = when(val res = pair.second) {
                is Either.Left -> return@coroutineScope Either.Left(res.value)
                is Either.Right -> res.value
            }
            val template = Template(
                configuration.id,
                pair.first.templateId!!,
                pair.first.prefLabel,
                null,
                pair.first.id
            )
            template to configuration
        }
        Either.Right(pairs to rootTemplate.id)
    }

    /**
     * Inserts [Template] list to the database.
     */
    suspend fun persistTemplate(list: List<Template>) {
        pipelineDao.insertTemplate(list)
    }

    /**
     * Inserts [Configuration] list to the database.
     */
    suspend fun persistConfiguration(list: List<Configuration>) {
        pipelineDao.insertConfiguration(list)
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
    }
}