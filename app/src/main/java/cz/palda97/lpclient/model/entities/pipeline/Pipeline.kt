package cz.palda97.lpclient.model.entities.pipeline

import cz.palda97.lpclient.model.entities.pipelineview.PipelineView

/**
 * The whole pipeline.
 */
data class Pipeline(
    val pipelineView: PipelineView,
    val profile: Profile?,
    val components: List<Component>,
    val connections: List<Connection>,
    val configurations: List<Configuration>,
    val vertexes: List<Vertex>,
    val templates: List<Template>,
    val mapping: List<SameAs>,
    val tags: List<Tag>
)