package cz.palda97.lpclient.model.network

import java.net.URLEncoder

/**
 * Class for generating URLs for the web frontend.
 */
object WebUrlGenerator {

    private val String.encode
        get() = URLEncoder.encode(this, "utf-8")!!

    private const val CANVAS = "/#/pipelines/edit/canvas"

    /**
     * Generate web link for an execution.
     * @return Web frontend URL for the execution.
     */
    fun execution(frontendUrl: String, executionId: String, pipelineId: String): String {
        val canvas = "$frontendUrl$CANVAS"
        val pipeline = pipelineId.encode
        val execution = executionId.encode
        return "$canvas?pipeline=$pipeline&execution=$execution"
    }

    /**
     * Generate web link for a pipeline.
     * @return Web frontend URL for the pipeline.
     */
    fun pipeline(frontendUrl: String, pipelineId: String): String {
        val canvas = "$frontendUrl$CANVAS"
        val pipeline = pipelineId.encode
        return "$canvas?pipeline=$pipeline"
    }
}