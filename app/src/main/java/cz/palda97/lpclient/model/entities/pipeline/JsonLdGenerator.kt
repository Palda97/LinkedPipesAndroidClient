package cz.palda97.lpclient.model.entities.pipeline

import com.google.gson.Gson
import cz.palda97.lpclient.model.travelobjects.LdConstants

fun Pipeline.jsonLd(): String {

    fun parsePipelineView(sb: StringBuilder) {
        sb.append("{")
        sb.append("\"${LdConstants.ID}\":\"${pipelineView.id}\",")
        sb.append("\"${LdConstants.TYPE}\":[\"${LdConstants.TYPE_PIPELINE}\"],")
        pipelineView.version?.let {
            sb.append("\"${LdConstants.VERSION}\":[{\"${LdConstants.TYPE}\":\"${LdConstants.SCHEMA_INT}\",\"${LdConstants.VALUE}\":\"${it}\"}],")
        }
        sb.append("\"${LdConstants.PROFILE}\":[{\"${LdConstants.ID}\":\"${profile.id}\"}],")
        sb.append("\"${LdConstants.PREF_LABEL}\":[{\"${LdConstants.VALUE}\":\"${pipelineView.prefLabel}\"}]")
        sb.append("}")
    }

    fun parseComponent(sb: StringBuilder, component: Component) {
        sb.append("{")
        sb.append("\"${LdConstants.ID}\":\"${component.id}\",")
        sb.append("\"${LdConstants.TYPE}\":[\"${LdConstants.TYPE_COMPONENT}\"],")
        sb.append("\"${LdConstants.CONFIGURATION_GRAPH}\":[{\"${LdConstants.ID}\":\"${component.configurationId}\"}],")
        sb.append("\"${LdConstants.TEMPLATE}\":[{\"${LdConstants.ID}\":\"${component.templateId}\"}],")
        sb.append("\"${LdConstants.X}\":[{\"${LdConstants.TYPE}\":\"${LdConstants.SCHEMA_INTEGER}\",\"${LdConstants.VALUE}\":\"${component.x}\"}],")
        sb.append("\"${LdConstants.Y}\":[{\"${LdConstants.TYPE}\":\"${LdConstants.SCHEMA_INTEGER}\",\"${LdConstants.VALUE}\":\"${component.y}\"}],")
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
        sb.append("{")
        sb.append("\"${LdConstants.ID}\":\"${profile.id}\",")
        sb.append("\"${LdConstants.TYPE}\":[\"${LdConstants.TYPE_EXECUTION_PROFILE}\"],")
        sb.append("\"${LdConstants.REPO_POLICY}\":[{\"${LdConstants.ID}\":\"${profile.repoPolicyId}\"}],")
        sb.append("\"${LdConstants.REPO_TYPE}\":[{\"${LdConstants.ID}\":\"${profile.repoTypeId}\"}]")
        sb.append("}")
    }

    fun firstPart(sb: StringBuilder) {
        sb.append("{\"${LdConstants.GRAPH}\":[")
        parsePipelineView(sb)
        sb.append(",")
        components.forEach {
            parseComponent(sb, it)
            sb.append(",")
        }
        connections.forEach {
            parseConnection(sb, it)
            sb.append(",")
        }
        parseProfile(sb)
        sb.append("],")
        sb.append("\"${LdConstants.ID}\":\"${pipelineView.id}\"")
        sb.append("}")
    }

    fun secondPart(sb: StringBuilder) {
        configurations.forEach {
            sb.append(",{\"${LdConstants.GRAPH}\":[{")
            sb.append("\"${LdConstants.ID}\":\"${it.id}\",")
            sb.append("\"${LdConstants.TYPE}\":[\"${it.type}\"],")
            val settings = Gson().toJson(it.settings) ?: "{"
            sb.append(settings.drop(1))
            sb.append("],")
            sb.append("\"${LdConstants.ID}\":\"${it.id}\"")
            sb.append("}")
        }
    }

    val sb = StringBuilder()
    sb.append("[")
    firstPart(sb)
    secondPart(sb)
    sb.append("]")
    return sb.toString()
}