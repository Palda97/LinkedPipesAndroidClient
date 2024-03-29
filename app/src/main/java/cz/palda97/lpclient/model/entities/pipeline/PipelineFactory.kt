package cz.palda97.lpclient.model.entities.pipeline

import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.IdGenerator
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.entities.pipelineview.PipelineView
import cz.palda97.lpclient.model.entities.pipelineview.PipelineViewFactory
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.travelobjects.CommonFunctions
import cz.palda97.lpclient.model.travelobjects.LdConstants

/**
 * Factory for transforming jsonLd to [Pipeline].
 * @param server Server corresponding to the Pipeline.
 * Can be null if using this factory to just parse configuration.
 * @param string JsonLd containing the Pipeline.
 */
class PipelineFactory(private val server: ServerInstance?, private val string: String?) {

    data class MutablePipeline(
        var pipelineView: PipelineView? = null,
        var profile: Profile? = null,
        var components: MutableList<Component> = mutableListOf(),
        var connections: MutableList<Connection> = mutableListOf(),
        var configurations: MutableList<Configuration> = mutableListOf(),
        var vertexes: MutableList<Vertex> = mutableListOf(),
        var templates: MutableList<Template> = mutableListOf(),
        var mapping: List<SameAs> = emptyList(),
        var tags: List<Tag> = emptyList()
        ) {
        fun toPipeline(): Pipeline? {
            return Pipeline(
                pipelineView ?: return null,
                profile,
                components,
                connections,
                configurations,
                vertexes,
                templates,
                mapping,
                tags
            )
        }
        constructor(pipeline: Pipeline): this(
            pipeline.pipelineView,
            pipeline.profile,
            pipeline.components.toMutableList(),
            pipeline.connections.toMutableList(),
            pipeline.configurations.toMutableList(),
            pipeline.vertexes.toMutableList(),
            pipeline.templates.toMutableList(),
            pipeline.mapping,
            pipeline.tags.toMutableList()
        )
    }

    /**
     * Parse jsonLd to [Pipeline].
     * @return [MailPackage] with Pipeline.
     */
    fun parse(): MailPackage<Pipeline> {
        require(server != null)
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

    /**
     * Parse jsonLd to [Configuration].
     * @return [MailPackage] with Configuration.
     */
    fun parseConfigurationOnly(componentId: String? = null): MailPackage<Configuration> {
        val arrayList = when (val res = CommonFunctions.getRootArrayList(string)) {
            is Either.Left -> return MailPackage.brokenPackage(res.value)
            is Either.Right -> res.value
        }
        if (arrayList.size != 1) {
            return MailPackage.brokenPackage("size != 1")
        }
        val map = arrayList[0] as? Map<*, *> ?: return MailPackage.brokenPackage("item is not map")
        val mutablePipeline = MutablePipeline()
        parseConfiguration(map, mutablePipeline, componentId)
        if (mutablePipeline.configurations.size != 1) {
            return MailPackage.brokenPackage("parseConfiguration error")
        }
        return MailPackage(mutablePipeline.configurations[0])
    }

    companion object {
        private val l = Injector.generateLogFunction(this)

        /**
         * Gets the top left corner of group of components or vertexes.
         * @return Top left coordinates or null if there are no items.
         */
        fun topLeftCoords(components: List<Coords>): Pair<Int, Int>? {
            val minX = components.minBy {
                it.x
            }?.x ?: return null
            val minY = components.minBy {
                it.y
            }?.y ?: return null
            return minX to minY
        }

        private fun fixNegativeCoords(components: List<Component>, vertexes: List<Vertex>) {
            val (cMinX, cMinY) = topLeftCoords(components) ?: 0 to 0
            val (vMinX, vMinY) = topLeftCoords(vertexes) ?: 0 to 0
            val minX = if (cMinX < vMinX) cMinX else vMinX
            val minY = if (cMinY < vMinY) cMinY else vMinY
            val offsetX = if (minX < 0) - minX else 0
            val offsetY = if (minY < 0) - minY else 0
            if (offsetX == 0 && offsetY == 0)
                return
            components.forEach {
                it.x += offsetX
                it.y += offsetY
            }
            vertexes.forEach {
                it.x += offsetX
                it.y += offsetY
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
            fixNegativeCoords(pipeline.components, pipeline.vertexes)
            return Either.Right(pipeline)
        }

        private fun parseItem(
            item: Map<*, *>,
            mutablePipeline: MutablePipeline,
            server: ServerInstance
        ): Boolean {

            val id = CommonFunctions.giveMeThatId(item)
            if (id != null && id == LdConstants.MAPPING) {
                return parseMapping(item, mutablePipeline)
            }

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
                        val tags = parseTags(map, mutablePipeline)
                        (pipelineView != null && tags).also { if (!it) l("TYPE_PIPELINE") }
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

        private fun parseTags(map: Map<*, *>, mutablePipeline: MutablePipeline): Boolean {
            if (!map.contains(LdConstants.TAG)) {
                return true
            }
            val values = CommonFunctions.giveMeAllStrings(map, LdConstants.TAG, LdConstants.VALUE) ?: return false
            val tags = values.map { Tag(it) }
            mutablePipeline.tags = tags
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
            )
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
            val repoTypeId =
                CommonFunctions.giveMeThatString(map, LdConstants.REPO_TYPE, LdConstants.ID)
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

        private fun parseConfiguration(map: Map<*, *>, mutablePipeline: MutablePipeline, componentId: String? = null): Boolean {
            val id = componentId?.let { IdGenerator.configurationId(it) } ?: CommonFunctions.giveMeThatId(map) ?: return false
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

        private fun parseSameAs(map: Map<*, *>): SameAs? {
            val id = CommonFunctions.giveMeThatId(map) ?: return null
            val sameAs = CommonFunctions.giveMeThatString(map, LdConstants.SAME_AS, LdConstants.ID) ?: return null
            return SameAs(id, sameAs)
        }

        private fun parseMapping(map: Map<*, *>, mutablePipeline: MutablePipeline): Boolean {
            val arrayList = CommonFunctions.prepareSemiRootElement(map) ?: return false
            val list = arrayList.map {
                val sameAsMap = it as? Map<*, *> ?: return false
                val sameAs = parseSameAs(sameAsMap) ?: return false
                sameAs
            }
            mutablePipeline.mapping = list
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