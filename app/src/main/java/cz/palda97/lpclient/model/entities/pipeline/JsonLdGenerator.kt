package cz.palda97.lpclient.model.entities.pipeline

import com.google.gson.Gson
import cz.palda97.lpclient.model.travelobjects.LdConstants

/**
 * Generate jsonLd from pipeline.
 */
fun Pipeline.jsonLd(): String {

    fun parsePipelineView(sb: StringBuilder) {
        sb.append("{")
        sb.append("\"${LdConstants.ID}\":\"${pipelineView.id}\",")
        sb.append("\"${LdConstants.TYPE}\":[\"${LdConstants.TYPE_PIPELINE}\"],")
        pipelineView.version?.let {
            sb.append("\"${LdConstants.VERSION}\":[{\"${LdConstants.TYPE}\":\"${LdConstants.SCHEMA_INT}\",\"${LdConstants.VALUE}\":\"${it}\"}],")
        }
        profile?.let {
            sb.append("\"${LdConstants.PROFILE}\":[{\"${LdConstants.ID}\":\"${it.id}\"}],")
        }
        if (tags.isNotEmpty()) {
            sb.append("\"${LdConstants.TAG}\":[")
            tags.forEachIndexed { i, tag ->
                if (i != 0)
                    sb.append(",")
                sb.append("{\"${LdConstants.VALUE}\":\"${tag.value}\"}")
            }
            sb.append("],")
        }
        sb.append("\"${LdConstants.PREF_LABEL}\":[{\"${LdConstants.VALUE}\":\"${pipelineView.prefLabel}\"}]")
        sb.append("}")
    }

    fun parseComponent(sb: StringBuilder, component: Component, hasPosition: Boolean = true) {
        sb.append("{")
        sb.append("\"${LdConstants.ID}\":\"${component.id}\",")
        val type = if (hasPosition) LdConstants.TYPE_COMPONENT else LdConstants.TYPE_TEMPLATE
        sb.append("\"${LdConstants.TYPE}\":[\"${type}\"],")
        component.configurationId?.let {
            sb.append("\"${LdConstants.CONFIGURATION_GRAPH}\":[{\"${LdConstants.ID}\":\"${it}\"}],")
        }
        sb.append("\"${LdConstants.TEMPLATE}\":[{\"${LdConstants.ID}\":\"${component.templateId}\"}],")
        if (hasPosition) {
            sb.append("\"${LdConstants.X}\":[{\"${LdConstants.TYPE}\":\"${LdConstants.SCHEMA_INTEGER}\",\"${LdConstants.VALUE}\":\"${component.x}\"}],")
            sb.append("\"${LdConstants.Y}\":[{\"${LdConstants.TYPE}\":\"${LdConstants.SCHEMA_INTEGER}\",\"${LdConstants.VALUE}\":\"${component.y}\"}],")
        }
        component.description?.let {
            sb.append("\"${LdConstants.DESCRIPTION}\":[{\"${LdConstants.VALUE}\":\"${it}\"}],")
        }
        sb.append("\"${LdConstants.PREF_LABEL}\":[{\"${LdConstants.VALUE}\":\"${component.prefLabel}\"}]")
        sb.append("}")
    }

    fun parseConnection(sb: StringBuilder, connection: Connection) {
        sb.append("{")
        sb.append("\"${LdConstants.ID}\":\"${connection.id}\",")
        sb.append("\"${LdConstants.TYPE}\":[\"${LdConstants.TYPE_CONNECTION}\"],")
        sb.append("\"${LdConstants.SOURCE_BINDING}\":[{\"${LdConstants.VALUE}\":\"${connection.sourceBinding}\"}],")
        sb.append("\"${LdConstants.SOURCE_COMPONENT}\":[{\"${LdConstants.ID}\":\"${connection.sourceComponentId}\"}],")
        sb.append("\"${LdConstants.TARGET_BINDING}\":[{\"${LdConstants.VALUE}\":\"${connection.targetBinding}\"}],")
        sb.append("\"${LdConstants.TARGET_COMPONENT}\":[{\"${LdConstants.ID}\":\"${connection.targetComponentId}\"}]")
        sb.append("}")
    }

    fun parseProfile(sb: StringBuilder) {
        require(profile != null)
        sb.append("{")
        sb.append("\"${LdConstants.ID}\":\"${profile.id}\",")
        sb.append("\"${LdConstants.TYPE}\":[\"${LdConstants.TYPE_EXECUTION_PROFILE}\"]")
        profile.repoPolicyId?.let {
            sb.append(",\"${LdConstants.REPO_POLICY}\":[{\"${LdConstants.ID}\":\"${it}\"}]")
        }
        profile.repoTypeId?.let {
            sb.append(",\"${LdConstants.REPO_TYPE}\":[{\"${LdConstants.ID}\":\"${it}\"}]")
        }
        sb.append("}")
    }

    fun firstPart(sb: StringBuilder) {
        sb.append("{\"${LdConstants.GRAPH}\":[")
        parsePipelineView(sb)
        components.forEach {
            sb.append(",")
            parseComponent(sb, it)
        }
        connections.forEach {
            sb.append(",")
            parseConnection(sb, it)
        }
        if (profile != null){
            sb.append(",")
            parseProfile(sb)
        }
        sb.append("],")
        sb.append("\"${LdConstants.ID}\":\"${pipelineView.id}\"")
        sb.append("}")
    }

    fun parseConfig(sb: StringBuilder, config: Config) {
        sb.append("{")
        sb.append("\"${LdConstants.ID}\":\"${config.id}\",")
        sb.append("\"${LdConstants.TYPE}\":[\"${config.type}\"]")
        if (config.settings.isNotEmpty()) {
            sb.append(",")
        }
        val settings = Gson().toJson(config.settings) ?: "{}"
        sb.append(settings.drop(1))
    }

    fun secondPart(sb: StringBuilder) {
        configurations.forEach {
            sb.append(",{\"${LdConstants.GRAPH}\":[")
            it.settings.forEachIndexed { i, it ->
                if (i > 0) {
                    sb.append(",")
                }
                parseConfig(sb, it)
            }

            sb.append("],")
            sb.append("\"${LdConstants.ID}\":\"${it.id}\"")
            sb.append("}")
        }
    }

    fun templatePart(sb: StringBuilder) {
        templates.forEach {
            sb.append("{\"${LdConstants.GRAPH}\":[")
            parseComponent(sb, Component(0, 0, it), false)
            sb.append("],")
            sb.append("\"${LdConstants.ID}\":\"${it.id}\"")
            sb.append("},")
        }
    }

    fun sameAsPart(sb: StringBuilder) {
        if (mapping.isEmpty())
            return
        sb.append("{\"${LdConstants.GRAPH}\":[")
        mapping.forEachIndexed { i, sameAs ->
            if (i != 0)
                sb.append(",")
            sb.append("{\"${LdConstants.ID}\":\"${sameAs.id}\",")
            sb.append("\"${LdConstants.SAME_AS}\":[{\"${LdConstants.ID}\":\"${sameAs.sameAs}\"}]")
            sb.append("}")
        }
        sb.append("],")
        sb.append("\"${LdConstants.ID}\":\"${LdConstants.MAPPING}\"")
        sb.append("},")
    }

    val sb = StringBuilder()
    sb.append("[")
    sameAsPart(sb)
    templatePart(sb)
    firstPart(sb)
    secondPart(sb)
    sb.append("]")
    return sb.toString()
}