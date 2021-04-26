package cz.palda97.lpclient.model.entities.possiblecomponent

import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.travelobjects.CommonFunctions
import cz.palda97.lpclient.model.travelobjects.LdConstants

/**
 * Factory for transforming jsonLd to list of [PossibleComponent].
 */
class PossibleComponentFactory(private val json: String, private val serverId: Long) {

    /**
     * Parse jsonLd to [PossibleComponents][PossibleComponent].
     * @return list of [PossibleComponents][PossibleComponent] or null on error.
     */
    fun parse(): List<PossibleComponent>? {
        val rootArrayList = when (val res = CommonFunctions.getRootArrayList(json)) {
            is Either.Left -> return null
            is Either.Right -> res.value
        }
        return rootArrayList.map {
            val graph = CommonFunctions.prepareSemiRootElement(it) ?: return null
            val component = parseGraph(graph) ?: return null
            component
        }
    }

    private fun parseGraph(graph: ArrayList<*>): PossibleComponent? {
        graph.forEach {
            val map = it as? Map<*, *> ?: return null
            when(CommonFunctions.giveMeThatType(map)) {
                LdConstants.TYPE_TEMPLATE_JAR -> return parseTemplateJar(map)
                LdConstants.TYPE_TEMPLATE -> return parseTemplate(map)
            }
        }
        return null
    }

    private fun parseTemplateJar(map: Map<*, *>): PossibleComponent? {
        val id = CommonFunctions.giveMeThatId(map) ?: return null
        val prefLabel = CommonFunctions.giveMeThatString(map, LdConstants.PREF_LABEL, LdConstants.VALUE) ?: return null
        return PossibleComponent(id, serverId, prefLabel, null)
    }

    private fun parseTemplate(map: Map<*, *>): PossibleComponent? {
        val templateId = CommonFunctions.giveMeThatString(map, LdConstants.TEMPLATE, LdConstants.ID) ?: return null
        val tmp = parseTemplateJar(map) ?: return null
        return PossibleComponent(tmp.id, serverId, tmp.prefLabel, templateId)
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
    }
}