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
        var vertexes: MutableList<Vertex> = mutableListOf(),
        var templates: MutableList<Template> = mutableListOf()
        ) {
        fun toPipeline(): Pipeline? {
            return Pipeline(
                pipelineView ?: return null,
                profile ?: return null,
                components,
                connections,
                configurations,
                vertexes,
                templates
            )
        }
        constructor(pipeline: Pipeline): this(
            pipeline.pipelineView,
            pipeline.profile,
            pipeline.components.toMutableList(),
            pipeline.connections.toMutableList(),
            pipeline.configurations.toMutableList(),
            pipeline.vertexes.toMutableList(),
            pipeline.templates.toMutableList()
        )
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

            rootArrayList.forEach {
                val map = it as? Map<*, *> ?: return Either.Left("semiroot element is not map")
                if (!parseItem(map, mutablePipeline, server)) {
                    return Either.Left("some first part item could not be parsed")
                }
            }

            val pipeline = mutablePipeline.toPipeline()
                ?: return Either.Left("mutable pipeline could not be converted to pipeline")
            return Either.Right(pipeline)
        }

        private fun parseItem(
            item: Map<*, *>,
            mutablePipeline: MutablePipeline,
            server: ServerInstance
        ): Boolean {
            val graph = CommonFunctions.prepareSemiRootElement(item) ?: return false.also { l("no graph") }
            graph.forEach {
                val map = it as? Map<*, *> ?: return false.also {
                    l("no map")
                }
                val type = CommonFunctions.giveMeThatType(map) ?: return false.also { l("no type") }
                val ok: Boolean = when(type) {
                    LdConstants.TYPE_PIPELINE -> {
                        val pipelineView = PipelineViewFactory.makePipelineView(map, server)
                        mutablePipeline.pipelineView = pipelineView
                        (pipelineView != null).also { if (!it) l("TYPE_PIPELINE") }
                    }
                    LdConstants.TYPE_COMPONENT -> parseComponent(map, mutablePipeline).also { if (!it) l("TYPE_COMPONENT") }
                    LdConstants.TYPE_CONNECTION -> parseConnection(map, mutablePipeline).also { if (!it) l("TYPE_CONNECTION") }
                    LdConstants.TYPE_EXECUTION_PROFILE -> parseProfile(map, mutablePipeline).also { if (!it) l("TYPE_EXECUTION_PROFILE") }
                    LdConstants.TYPE_VERTEX -> parseVertex(map, mutablePipeline).also { if (!it) l("TYPE_VERTEX") }
                    LdConstants.TYPE_TEMPLATE -> parseTemplate(map, mutablePipeline).also { if (!it) l("TYPE_TEMPLATE") }
                    else -> return parseConfiguration(item, mutablePipeline).also { if (!it) l("Configuration") }
                }
                if (!ok) {
                    return false.also { l("!ok") }
                }
            }
            return true
        }

        private fun parseTemplate(map: Map<*, *>, mutablePipeline: MutablePipeline): Boolean {
            val template = makeTemplate(map) ?: return false
            mutablePipeline.templates.add(template)
            return true
        }

        private fun makeTemplate(map: Map<*, *>): Template? {
            val configurationId = CommonFunctions.giveMeThatString(
                map,
                LdConstants.CONFIGURATION_GRAPH,
                LdConstants.ID
            ) ?: return null
            val templateId =
                CommonFunctions.giveMeThatString(map, LdConstants.TEMPLATE, LdConstants.ID)
                    ?: return null
            val prefLabel =
                CommonFunctions.giveMeThatString(map, LdConstants.PREF_LABEL, LdConstants.VALUE)
                    ?: return null
            val description =
                CommonFunctions.giveMeThatString(map, LdConstants.DESCRIPTION, LdConstants.VALUE)
            val id = CommonFunctions.giveMeThatId(map) ?: return null

            return Template(configurationId, templateId, prefLabel, description, id)
        }

        private fun parseComponent(map: Map<*, *>, mutablePipeline: MutablePipeline): Boolean {
            val xString = CommonFunctions.giveMeThatString(map, LdConstants.X, LdConstants.VALUE)
                ?: return false
            val yString = CommonFunctions.giveMeThatString(map, LdConstants.Y, LdConstants.VALUE)
                ?: return false

            val x = xString.toIntOrNull() ?: return false
            val y = yString.toIntOrNull() ?: return false

            val template = makeTemplate(map) ?: return false

            mutablePipeline.components.add(
                Component(
                    x,
                    y,
                    template
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

        private fun parseConfig(map: Map<*, *>): Config? {
            val id = CommonFunctions.giveMeThatId(map) ?: return null
            val type = CommonFunctions.giveMeThatType(map) ?: return null
            val configMap = map.filterNot {
                it.key == LdConstants.ID || it.key == LdConstants.TYPE
            }
            return Config(configMap.toMutableMap(), type, id)
        }

        private fun parseConfiguration(map: Map<*, *>, mutablePipeline: MutablePipeline): Boolean {
            val id = CommonFunctions.giveMeThatId(map) ?: return false
            val arrayList = CommonFunctions.prepareSemiRootElement(map) ?: return false
            val configs = arrayList.map {
                val configMap = it as? Map<*, *> ?: return false
                val config = parseConfig(configMap) ?: return false
                config
            }
            mutablePipeline.configurations.add(
                Configuration(configs, id)
            )
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