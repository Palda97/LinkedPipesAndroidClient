package cz.palda97.lpclient.model.entities.pipeline

//data class Configuration(val settings: Map<*, *>, val type: String, val id: String)
data class Configuration(val settings: List<Config>, val id: String)
data class Config(val settings: Map<*, *>, val type: String, val id: String)