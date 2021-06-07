package cz.palda97.lpclient.model.network

import java.net.URLEncoder

object WebUrlGenerator {

    private val String.encode
        get() = URLEncoder.encode(this, "utf-8")!!

    fun execution(frontendUrl: String, executionId: String, pipelineId: String): String {
        val canvas = "$frontendUrl/#/pipelines/edit/canvas"
        val pipeline = pipelineId.encode
        val execution = executionId.encode
        return "$canvas?pipeline=$pipeline&execution=$execution"
    }
}