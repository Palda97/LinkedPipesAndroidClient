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
        var configurations: MutableList<Configuration> = mutableListOf(),
        var vertexes: MutableList<Vertex> = mutableListOf()
    ) {
        fun toPipeline(): Pipeline? {
            return Pipeline(
                pipelineView ?: return null,
                profile ?: return null,
                components,
                connections,
                configurations,
                vertexes
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
                    is Either.Left -> {
                        l(res.value)
                        MailPackage.brokenPackage(res.value)
                    }
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
                    val map = it as? Map<*, *> ?: return Either.Left("configuration is not map")
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
                LdConstants.TYPE_VERTEX -> parseVertex(item, mutablePipeline)
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
            val description =
                CommonFunctions.giveMeThatString(map, LdConstants.DESCRIPTION, LdConstants.VALUE)
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
                    description,
                    id
                )
            )
            return true
        }

        private fun parseVertexIds(connectionMap: Map<*, *>): List<String>? {
            if (!connectionMap.contains(LdConstants.VERTEX)) {
                return emptyList()
            }
            val arrayList = connectionMap[LdConstants.VERTEX] as? ArrayList<*> ?: return null
            return arrayList.map {
                val map = it as? Map<*, *> ?: return null
                CommonFunctions.giveMeThatId(map) ?: return null
            }
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

            val vertexIds = parseVertexIds(map) ?: return false

            mutablePipeline.connections.add(
                Connection(
                    sourceBinding,
                    sourceComponentId,
                    targetBinding,
                    targetComponentId,
                    vertexIds,
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
            val settings = CommonFunctions.prepareSemiRootElement(map) ?: return false
            mutablePipeline.configurations.add(Configuration(settings, id))
            return true
        }

        private fun parseVertex(map: Map<*, *>, mutablePipeline: MutablePipeline): Boolean {
            val orderString =
                CommonFunctions.giveMeThatString(map, LdConstants.ORDER, LdConstants.VALUE)
                    ?: return false
            val xString = CommonFunctions.giveMeThatString(map, LdConstants.X, LdConstants.VALUE)
                ?: return false
            val yString = CommonFunctions.giveMeThatString(map, LdConstants.Y, LdConstants.VALUE)
                ?: return false
            val id = CommonFunctions.giveMeThatId(map) ?: return false
            val order = orderString.toIntOrNull() ?: return false
            val x = xString.toIntOrNull() ?: return false
            val y = yString.toIntOrNull() ?: return false
            mutablePipeline.vertexes.add(Vertex(order, x, y, id))
            return true
        }
    }
}