package cz.palda97.lpclient.model.entities.pipeline

import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.entities.pipelineview.PipelineView
import cz.palda97.lpclient.model.entities.pipelineview.PipelineViewFactory
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.travelobjects.CommonFunctions
import cz.palda97.lpclient.model.travelobjects.LdConstants

class PipelineFactory(val pipeline: MailPackage<Pipeline>) {

    data class MutablePipeline(
        var pipelineView: PipelineView? = null,
        var profile: Profile? = null,
        var components: MutableList<Component> = mutableListOf(),
        var connections: MutableList<Connection> = mutableListOf(),
        var configurations: MutableList<Configuration> = mutableListOf()
    ) {
        fun toPipeline(): Pipeline? {
            return Pipeline(
                pipelineView ?: return null,
                profile ?: return null,
                components,
                connections,
                configurations
            )
        }
    }

    constructor(server: ServerInstance, string: String?) : this(fromJson(server, string))

    companion object {
        private val l = Injector.generateLogFunction(this)

        private fun fromJson(server: ServerInstance, string: String?): MailPackage<Pipeline> {
            return when (val res = CommonFunctions.getRootArrayList(string)) {
                is Either.Left -> MailPackage.brokenPackage(res.value)
                is Either.Right -> when (val res = parsePipeline(server, res.value)) {
                    is Either.Left -> MailPackage.brokenPackage(res.value)
                    is Either.Right -> MailPackage(res.value)
                }
            }
        }

        private fun parsePipeline(
            server: ServerInstance,
            rootArrayList: ArrayList<*>
        ): Either<String, Pipeline> {
            if (rootArrayList.isEmpty())
                return Either.Left("root arraylist is empty")

            val mutablePipeline = MutablePipeline()

            val firstPartArrayList = CommonFunctions.prepareSemiRootElement(rootArrayList[0])
                ?: return Either.Left("firstPartArrayList not found")

            firstPartArrayList.forEach {
                if (!parseFirstPartItem(it, mutablePipeline, server)) {
                    return Either.Left("some first part item could not be parsed")
                }
            }

            rootArrayList.forEachIndexed { index, it ->
                if (index > 0) {
                    val graph = CommonFunctions.prepareSemiRootElement(it)
                        ?: return Either.Left("element not arraylist while parsing configurations")
                    if (graph.size != 1)
                        return Either.Left("configuration graph.size != 1")
                    val map = graph[0] as? Map<*, *>
                        ?: return Either.Left("configuration graph[0] is not map")
                    if (!parseConfiguration(map, mutablePipeline)) {
                        return Either.Left("some configuration could not be parsed")
                    }
                }
            }

            val pipeline = mutablePipeline.toPipeline()
                ?: return Either.Left("mutable pipeline could not be converted to pipeline")
            return Either.Right(pipeline)
        }

        private fun parseFirstPartItem(
            item: Any?,
            mutablePipeline: MutablePipeline,
            server: ServerInstance
        ): Boolean {
            if (item !is Map<*, *>)
                return false
            val type = CommonFunctions.giveMeThatType(item) ?: return false
            return when (type) {
                LdConstants.TYPE_PIPELINE -> {
                    val pipelineView = PipelineViewFactory.makePipelineView(item, server)
                    mutablePipeline.pipelineView = pipelineView
                    pipelineView != null
                }
                LdConstants.TYPE_COMPONENT -> parseComponent(item, mutablePipeline)
                LdConstants.TYPE_CONNECTION -> parseConnection(item, mutablePipeline)
                LdConstants.TYPE_EXECUTION_PROFILE -> parseProfile(item, mutablePipeline)
                else -> false
            }
        }

        private fun parseComponent(map: Map<*, *>, mutablePipeline: MutablePipeline): Boolean {
            val configurationId = CommonFunctions.giveMeThatString(
                map,
                LdConstants.CONFIGURATION_GRAPH,
                LdConstants.ID
            ) ?: return false
            val templateId =
                CommonFunctions.giveMeThatString(map, LdConstants.TEMPLATE, LdConstants.ID)
                    ?: return false
            val xString = CommonFunctions.giveMeThatString(map, LdConstants.X, LdConstants.VALUE)
                ?: return false
            val yString = CommonFunctions.giveMeThatString(map, LdConstants.Y, LdConstants.VALUE)
                ?: return false
            val prefLabel =
                CommonFunctions.giveMeThatString(map, LdConstants.PREF_LABEL, LdConstants.VALUE)
                    ?: return false
            val id = CommonFunctions.giveMeThatId(map) ?: return false

            val x = xString.toIntOrNull() ?: return false
            val y = yString.toIntOrNull() ?: return false

            mutablePipeline.components.add(
                Component(
                    configurationId,
                    templateId,
                    x,
                    y,
                    prefLabel,
                    id
                )
            )
            return true
        }

        private fun parseConnection(map: Map<*, *>, mutablePipeline: MutablePipeline): Boolean {
            val sourceBinding =
                CommonFunctions.giveMeThatString(map, LdConstants.SOURCE_BINDING, LdConstants.VALUE)
                    ?: return false
            val sourceComponentId =
                CommonFunctions.giveMeThatString(map, LdConstants.SOURCE_COMPONENT, LdConstants.ID)
                    ?: return false
            val targetBinding =
                CommonFunctions.giveMeThatString(map, LdConstants.TARGET_BINDING, LdConstants.VALUE)
                    ?: return false
            val targetComponentId =
                CommonFunctions.giveMeThatString(map, LdConstants.TARGET_COMPONENT, LdConstants.ID)
                    ?: return false
            val id = CommonFunctions.giveMeThatId(map) ?: return false
            mutablePipeline.connections.add(
                Connection(
                    sourceBinding,
                    sourceComponentId,
                    targetBinding,
                    targetComponentId,
                    id
                )
            )
            return true
        }

        private fun parseProfile(map: Map<*, *>, mutablePipeline: MutablePipeline): Boolean {
            val repoPolicyId =
                CommonFunctions.giveMeThatString(map, LdConstants.REPO_POLICY, LdConstants.ID)
                    ?: return false
            val repoTypeId =
                CommonFunctions.giveMeThatString(map, LdConstants.REPO_TYPE, LdConstants.ID)
                    ?: return false
            val id = CommonFunctions.giveMeThatId(map) ?: return false
            mutablePipeline.profile = Profile(repoPolicyId, repoTypeId, id)
            return true
        }

        private fun parseConfiguration(map: Map<*, *>, mutablePipeline: MutablePipeline): Boolean {
            val id = CommonFunctions.giveMeThatId(map) ?: return false
            val type = CommonFunctions.giveMeThatType(map) ?: return false
            val configurationMap = map.filterNot {
                it.key == LdConstants.ID || it.key == LdConstants.TYPE
            }
            mutablePipeline.configurations.add(Configuration(configurationMap, type, id))
            return true
        }
    }
}