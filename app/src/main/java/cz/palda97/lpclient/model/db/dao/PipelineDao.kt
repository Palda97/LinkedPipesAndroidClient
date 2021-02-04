package cz.palda97.lpclient.model.db.dao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.room.*
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.entities.pipeline.*
import cz.palda97.lpclient.model.entities.pipelineview.PipelineView
import cz.palda97.lpclient.model.entities.possiblecomponent.PossibleComponent
import cz.palda97.lpclient.model.entities.possiblecomponent.PossibleStatus
import cz.palda97.lpclient.model.entities.possiblecomponent.StatusWithPossibles
import cz.palda97.lpclient.model.repository.PossibleComponentRepository

@Dao
abstract class PipelineDao {

    //Insert

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertProfile(profile: Profile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertComponent(component: Component)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertComponent(list: List<Component>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertConnection(connection: Connection)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertConnection(list: List<Connection>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertConfiguration(connection: Configuration)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertConfiguration(list: List<Configuration>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertVertex(connection: Vertex)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertVertex(list: List<Vertex>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertTemplate(connection: Template)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertTemplate(list: List<Template>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertConfigInput(configInput: ConfigInput)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertConfigInput(list: List<ConfigInput>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertDialogJs(dialogJs: DialogJs)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertDialogJs(list: List<DialogJs>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertBinding(binding: Binding)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertBinding(list: List<Binding>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertStatus(status: ConfigDownloadStatus)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertStatus(list: List<ConfigDownloadStatus>)

    //LiveData

    @Query("select * from profile")
    abstract fun liveProfile(): LiveData<List<Profile>>

    @Query("select * from component")
    abstract fun liveComponent(): LiveData<List<Component>>

    @Query("select * from connection")
    abstract fun liveConnection(): LiveData<List<Connection>>

    @Query("select * from configuration")
    abstract fun liveConfiguration(): LiveData<List<Configuration>>

    @Query("select * from vertex")
    abstract fun liveVertex(): LiveData<List<Vertex>>

    @Query("select * from template")
    abstract fun liveTemplate(): LiveData<List<Template>>

    @Query("select * from configinput where componentId = :componentId")
    abstract fun liveConfigInput(componentId: String): LiveData<List<ConfigInput>>

    @Query("select * from dialogjs where componentId = :componentId")
    abstract fun liveDialogJs(componentId: String): LiveData<List<DialogJs>>

    @Query("select * from binding where templateId = :templateId")
    abstract fun liveBinding(templateId: String): LiveData<List<Binding>>

    @Transaction
    @Query("select * from configdownloadstatus where componentId = :componentId and type = ${ConfigDownloadStatus.TYPE_CONFIG_INPUT}")
    abstract fun liveConfigWithStatus(componentId: String): LiveData<StatusWithConfigInput>

    @Transaction
    @Query("select * from configdownloadstatus where componentId = :componentId and type = ${ConfigDownloadStatus.TYPE_DIALOG_JS}")
    abstract fun liveDialogJsWithStatus(componentId: String): LiveData<StatusWithDialogJs>

    @Transaction
    @Query("select * from configdownloadstatus where componentId = :templateId and type = ${ConfigDownloadStatus.TYPE_BINDING}")
    abstract fun liveBindingWithStatus(templateId: String): LiveData<StatusWithBinding>

    @Transaction
    @Query("select * from configdownloadstatus where type = ${ConfigDownloadStatus.TYPE_CONFIG_INPUT}")
    abstract fun liveConfigWithStatus(): LiveData<List<StatusWithConfigInput>>

    @Transaction
    @Query("select * from configdownloadstatus where type = ${ConfigDownloadStatus.TYPE_DIALOG_JS}")
    abstract fun liveDialogJsWithStatus(): LiveData<List<StatusWithDialogJs>>

    @Transaction
    @Query("select * from configdownloadstatus where type = ${ConfigDownloadStatus.TYPE_BINDING}")
    abstract fun liveBindingWithStatus(): LiveData<List<StatusWithBinding>>

    //Delete

    @Delete
    abstract suspend fun deleteProfile(profile: Profile)

    @Delete
    abstract suspend fun deleteComponent(component: Component)

    @Delete
    abstract suspend fun deleteConnection(connection: Connection)

    @Delete
    abstract suspend fun deleteConfiguration(configuration: Configuration)

    @Delete
    abstract suspend fun deleteVertex(vertex: Vertex)

    @Delete
    abstract suspend fun deleteTemplate(template: Template)

    @Delete
    abstract suspend fun deleteConfigInput(configInput: ConfigInput)

    @Delete
    abstract suspend fun deleteDialogJs(dialogJs: DialogJs)

    @Delete
    abstract suspend fun deleteBinding(binding: Binding)

    //DeleteAll

    @Query("delete from profile")
    abstract suspend fun deleteAllProfiles()

    @Query("delete from component")
    abstract suspend fun deleteAllComponents()

    @Query("delete from connection")
    abstract suspend fun deleteAllConnections()

    @Query("delete from configuration")
    abstract suspend fun deleteAllConfigurations()

    @Query("delete from vertex")
    abstract suspend fun deleteAllVertexes()

    @Query("delete from template")
    abstract suspend fun deleteAllTemplates()

    @Query("delete from configinput")
    abstract suspend fun deleteAllConfigInputs()

    @Query("delete from dialogjs")
    abstract suspend fun deleteAllDialogJs()

    @Query("delete from binding")
    abstract suspend fun deleteAllBindings()

    //Routines

    @Transaction
    open suspend fun deletePipeline() {
        deleteAllProfiles()
        deleteAllComponents()
        deleteAllConnections()
        deleteAllConfigurations()
        deleteAllVertexes()
        deleteAllTemplates()
    }

    @Transaction
    open suspend fun deleteDefinitions() {
        deleteAllConfigInputs()
        deleteAllDialogJs()
    }

    @Transaction
    open suspend fun deleteAll() {
        deletePipeline()
        deleteDefinitions()
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPipelineView(pipelineView: PipelineView)

    @Transaction
    open suspend fun insertPipeline(pipeline: Pipeline) {
        with(pipeline) {
            insertPipelineView(pipelineView)
            insertProfile(profile)
            insertComponent(components)
            insertConnection(connections)
            insertConfiguration(configurations)
            insertVertex(vertexes)
            insertTemplate(templates)
        }
    }

    @Transaction
    open suspend fun replacePipeline(pipeline: Pipeline) {
        deletePipeline()
        insertPipeline(pipeline)
    }

    private val PipelineFactory.MutablePipeline.mail: MailPackage<Pipeline>
        get() = toPipeline()?.let {
            MailPackage(it)
        } ?: MailPackage.loadingPackage()

    @Query("select * from pipelineview where id = :pipelineId")
    abstract fun livePipelineView(pipelineId: String): LiveData<PipelineView>

    fun livePipeline(pipelineId: String): LiveData<MailPackage<Pipeline>> {
        val mutablePipeline = PipelineFactory.MutablePipeline()
        val mediator: MediatorLiveData<MailPackage<Pipeline>> = MediatorLiveData()
        val readyStatuses = mutableListOf<Boolean>().apply {
            for (i in 1..7) {
                add(false)
            }
        }
        with(mediator) {
            fun post(value: MailPackage<Pipeline>) {
                if (readyStatuses.contains(false))
                    return
                postValue(value)
            }
            addSource(liveProfile()) {
                if (it != null && it.size == 1) {
                    mutablePipeline.profile = it[0]
                    readyStatuses[0] = true
                    post(mutablePipeline.mail)
                }
            }
            addSource(livePipelineView(pipelineId)) {
                if (it != null) {
                    mutablePipeline.pipelineView = it
                    readyStatuses[1] = true
                    post(mutablePipeline.mail)
                }
            }
            addSource(liveComponent()) {
                if (it != null) {
                    mutablePipeline.components = it.toMutableList()
                    readyStatuses[2] = true
                    post(mutablePipeline.mail)
                }
            }
            addSource(liveConnection()) {
                if (it != null) {
                    mutablePipeline.connections = it.toMutableList()
                    readyStatuses[3] = true
                    post(mutablePipeline.mail)
                }
            }
            addSource(liveConfiguration()) {
                if (it != null) {
                    mutablePipeline.configurations = it.toMutableList()
                    readyStatuses[4] = true
                    post(mutablePipeline.mail)
                }
            }
            addSource(liveVertex()) {
                if (it != null) {
                    mutablePipeline.vertexes = it.toMutableList()
                    readyStatuses[5] = true
                    post(mutablePipeline.mail)
                }
            }
            addSource(liveTemplate()) {
                if (it != null) {
                    mutablePipeline.templates = it.toMutableList()
                    readyStatuses[6] = true
                    post(mutablePipeline.mail)
                }
            }
        }
        return mediator
    }

    @Query("delete from vertex where id in (:ids)")
    abstract suspend fun deleteVertexes(ids: List<String>)

    @Query("select * from vertex where id in (:ids)")
    abstract suspend fun findVertexesByConnectionIds(ids: List<String>): List<Vertex>

    @Transaction
    open suspend fun deleteConnectionWithVertexes(connection: Connection): List<Vertex> {
        val vertexes = findVertexesByConnectionIds(connection.vertexIds)
        deleteVertexes(connection.vertexIds)
        deleteConnection(connection)
        return vertexes
    }

    @Query("select * from connection where sourceComponentId = :componentId or targetComponentId = :componentId")
    abstract suspend fun selectComponentConnections(componentId: String): List<Connection>

    @Delete
    abstract suspend fun deleteConnection(connections: List<Connection>)

    @Transaction
    open suspend fun purgeComponent(component: Component) {
        val connections = selectComponentConnections(component.id)
        val vertexes = connections.flatMap { it.vertexIds }
        deleteVertexes(vertexes)
        deleteConnection(connections)
        deleteComponent(component)
    }

    //Find

    @Query("select * from template where id = :id")
    abstract suspend fun findTemplateById(id: String): Template?

    @Query("select * from configdownloadstatus where componentId = :componentId and type = :type")
    abstract suspend fun findStatus(componentId: String, type: Int): ConfigDownloadStatus?

    @Query("select * from component where id = :componentId")
    abstract fun liveComponentById(componentId: String): LiveData<Component>

    @Query("select * from component where id = :componentId")
    abstract suspend fun findComponentById(componentId: String): Component?

    @Query("select * from configuration where id = :configurationId")
    abstract suspend fun findConfigurationById(configurationId: String): Configuration?

    @Transaction
    open suspend fun findConfigurationByComponentId(componentId: String): Configuration? {
        val component = findComponentById(componentId) ?: return null
        return findConfigurationById(component.configurationId)
    }

    @Query("select * from configuration where id = :configurationId")
    abstract fun liveConfigurationById(configurationId: String): LiveData<Configuration>

    @Query("select * from connection where targetComponentId = :componentId")
    abstract fun liveInputConnectionsByComponentId(componentId: String): LiveData<List<Connection>>

    @Query("select * from connection where sourceComponentId = :componentId")
    abstract fun liveOutputConnectionsByComponentId(componentId: String): LiveData<List<Connection>>

    @Query("select * from component where id != :componentId")
    abstract fun liveComponentExceptThisOne(componentId: String): LiveData<List<Component>>

    //Possible Components

    @Transaction
    @Query("select * from possiblestatus where serverId = :serverId")
    abstract fun livePossibleComponents(serverId: Long): LiveData<StatusWithPossibles>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPossibleStatus(status: PossibleStatus)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPossibleStatus(list: List<PossibleStatus>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPossibleComponent(component: PossibleComponent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPossibleComponent(list: List<PossibleComponent>)

    /**
     * DO NOT USE FOR RENEWAL OF COMPONENTS.
     * Use prepareForPossibleComponentsDownload instead.
     * @see prepareForPossibleComponentsDownload
     */
    @Query("delete from possiblecomponent where serverId = :serverId")
    abstract suspend fun deletePossibleComponents(serverId: Long)

    @Transaction
    open suspend fun prepareForPossibleComponentsDownload(serverId: Long) {
        insertPossibleStatus(PossibleStatus(serverId, PossibleComponentRepository.StatusCode.DOWNLOAD_IN_PROGRESS))
        deletePossibleComponents(serverId)
    }

    /**
     * DO NOT USE FOR RENEWAL OF COMPONENTS.
     * Use prepareForPossibleComponentsDownload instead.
     * @see prepareForPossibleComponentsDownload
     */
    @Query("delete from possiblecomponent where serverId in (:serverIds)")
    abstract suspend fun deletePossibleComponents(serverIds: List<Long>)

    @Transaction
    open suspend fun prepareForPossibleComponentsDownload(serverIds: List<Long>) {
        val statusCode = PossibleComponentRepository.StatusCode.DOWNLOAD_IN_PROGRESS
        val statuses = serverIds.map {
            PossibleStatus(it, statusCode)
        }
        insertPossibleStatus(statuses)
        deletePossibleComponents(serverIds)
    }

    @Query("select * from possiblecomponent where id = :id and serverId = :serverId")
    abstract suspend fun findPossibleComponentByIds(id: String, serverId: Long): PossibleComponent?

    //Extract Pipeline

    @Query("select * from pipelineview where id = :pipelineId")
    abstract suspend fun getPipelineView(pipelineId: String): PipelineView?

    @Query("select * from profile")
    abstract suspend fun getAllProfiles(): List<Profile>

    @Query("select * from component")
    abstract suspend fun getAllComponents(): List<Component>

    @Query("select * from connection")
    abstract suspend fun getAllConnections(): List<Connection>

    @Query("select * from configuration")
    abstract suspend fun getAllConfiguration(): List<Configuration>

    @Query("select * from vertex")
    abstract suspend fun getAllVertex(): List<Vertex>

    @Query("select * from template")
    abstract suspend fun getAllTemplates(): List<Template>

    @Transaction
    open suspend fun exportPipeline(pipelineId: String): Pipeline? {
        val pipelineView = getPipelineView(pipelineId) ?: return null
        val profile = getAllProfiles().let {
            if (it.size != 1) null else it.first()
        } ?: return null
        val components = getAllComponents()
        val connections = getAllConnections()
        val configurations = getAllConfiguration()
        val vertexes = getAllVertex()
        val templates = getAllTemplates()
        return Pipeline(
            pipelineView,
            profile,
            components,
            connections,
            configurations,
            vertexes,
            templates
        )
    }
}