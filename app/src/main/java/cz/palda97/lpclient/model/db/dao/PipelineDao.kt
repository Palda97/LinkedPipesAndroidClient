package cz.palda97.lpclient.model.db.dao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.room.*
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.entities.pipeline.*
import cz.palda97.lpclient.model.entities.pipelineview.PipelineView

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

    @Query("select * from binding where componentId = :componentId")
    abstract fun liveBinding(componentId: String): LiveData<List<Binding>>

    @Transaction
    @Query("select * from configdownloadstatus where componentId = :componentId and type = ${ConfigDownloadStatus.TYPE_CONFIG_INPUT}")
    abstract fun liveConfigWithStatus(componentId: String): LiveData<StatusWithConfigInput>

    @Transaction
    @Query("select * from configdownloadstatus where componentId = :componentId and type = ${ConfigDownloadStatus.TYPE_DIALOG_JS}")
    abstract fun liveDialogJsWithStatus(componentId: String): LiveData<StatusWithDialogJs>

    @Transaction
    @Query("select * from configdownloadstatus where componentId = :componentId and type = ${ConfigDownloadStatus.TYPE_BINDING}")
    abstract fun liveBindingWithStatus(componentId: String): LiveData<StatusWithBinding>

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

    @Transaction
    open suspend fun insertPipeline(pipeline: Pipeline) {
        with(pipeline) {
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

    //Find

    @Query("select * from template where id = :id")
    abstract suspend fun findTemplateById(id: String): Template?

    @Query("select * from configdownloadstatus where componentId = :componentId and type = :type")
    abstract suspend fun findStatus(componentId: String, type: Int): ConfigDownloadStatus?

    @Query("select * from component where id = :componentId")
    abstract fun liveComponentById(componentId: String): LiveData<Component>
}