package cz.palda97.lpclient.model.entities.pipeline

data class Connection(
    val sourceBinding: String,
    val sourceComponentId: String,
    val targetBinding: String,
    val targetComponentId: String,
    val vertexIds: List<String>,
    val id: String
)