package cz.palda97.lpclient.model.travelobjects

object LdConstants {
    const val GRAPH = "@graph"
    const val TYPE = "@type"
    const val ID = "@id"
    const val VALUE = "@value"

    const val VERSION = "http://etl.linkedpipes.com/ontology/version"
    const val PROFILE = "http://linkedpipes.com/ontology/profile"
    const val PREF_LABEL = "http://www.w3.org/2004/02/skos/core#prefLabel"
    const val CONFIGURATION_GRAPH = "http://linkedpipes.com/ontology/configurationGraph"
    const val TEMPLATE = "http://linkedpipes.com/ontology/template"
    const val X = "http://linkedpipes.com/ontology/x"
    const val Y = "http://linkedpipes.com/ontology/y"
    const val SOURCE_BINDING = "http://linkedpipes.com/ontology/sourceBinding"
    const val SOURCE_COMPONENT = "http://linkedpipes.com/ontology/sourceComponent"
    const val TARGET_BINDING = "http://linkedpipes.com/ontology/targetBinding"
    const val TARGET_COMPONENT = "http://linkedpipes.com/ontology/targetComponent"
    const val REPO_POLICY = "http://linkedpipes.com/ontology/rdfRepositoryPolicy"
    const val REPO_TYPE = "http://linkedpipes.com/ontology/rdfRepositoryType"

    const val TYPE_PIPELINE = "http://linkedpipes.com/ontology/Pipeline"
    const val TYPE_COMPONENT = "http://linkedpipes.com/ontology/Component"
    const val TYPE_CONNECTION = "http://linkedpipes.com/ontology/Connection"
    const val TYPE_EXECUTION_PROFILE = "http://linkedpipes.com/ontology/ExecutionProfile"
    const val TYPE_EXECUTION = "http://etl.linkedpipes.com/ontology/Execution"
    const val TYPE_TOMBSTONE = "http://linkedpipes.com/ontology/Tombstone"

    const val COMPONENT_EXECUTED = "http://etl.linkedpipes.com/ontology/execution/componentExecuted"
    const val COMPONENT_FINISHED = "http://etl.linkedpipes.com/ontology/execution/componentFinished"
    const val COMPONENT_MAPPED = "http://etl.linkedpipes.com/ontology/execution/componentMapped"
    const val COMPONENT_TO_EXECUTE = "http://etl.linkedpipes.com/ontology/execution/componentToExecute"
    const val COMPONENT_TO_MAP = "http://etl.linkedpipes.com/ontology/execution/componentToMap"
    const val EXECUTION_END = "http://etl.linkedpipes.com/ontology/execution/end"
    const val EXECUTION_SIZE = "http://etl.linkedpipes.com/ontology/execution/size"
    const val EXECUTION_START = "http://etl.linkedpipes.com/ontology/execution/start"
    const val EXECUTION_PIPELINE = "http://etl.linkedpipes.com/ontology/pipeline"
    const val EXECUTION_STATUS = "http://etl.linkedpipes.com/ontology/status"

    const val EXECUTION_STATUS_FINISHED = "http://etl.linkedpipes.com/resources/status/finished"
    const val EXECUTION_STATUS_FAILED = "http://etl.linkedpipes.com/resources/status/failed"
    const val EXECUTION_STATUS_RUNNING = "http://etl.linkedpipes.com/resources/status/running"
}