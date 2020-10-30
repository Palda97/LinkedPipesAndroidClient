package cz.palda97.lpclient.model.entities.pipeline

import cz.palda97.lpclient.model.entities.pipelineview.PipelineView

data class Pipeline(
    val pipelineView: PipelineView,
    val profile: Profile,
    val components: List<Component>,
    val connections: List<Connection>,
    val configurations: List<Configuration>
)