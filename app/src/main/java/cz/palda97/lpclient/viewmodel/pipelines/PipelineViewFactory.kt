package cz.palda97.lpclient.viewmodel.pipelines

class PipelineViewFactory(val pipelineList: List<PipelineView>?) {

    constructor(serverName: String, string: String?): this(fromJson(serverName, string))

    companion object {
        fun fromJson(serverName: String, string: String?): List<PipelineView>? {
            if (string == null)
                return null
            return listOf(PipelineView("pipeline xd", serverName, "10.10.2020"))
        }
    }
}