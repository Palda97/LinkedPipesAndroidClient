package cz.palda97.lpclient.model

import com.google.gson.Gson
import cz.palda97.lpclient.model.travelobjects.CommonFunctions.giveMeThatId
import cz.palda97.lpclient.model.travelobjects.CommonFunctions.giveMeThatString
import cz.palda97.lpclient.model.travelobjects.CommonFunctions.prepareSemiRootElement
import cz.palda97.lpclient.model.travelobjects.LdConstants.PREF_LABEL
import cz.palda97.lpclient.model.travelobjects.LdConstants.VALUE

class PipelineViewFactory(val serverWithPipelineViews: MailPackage<ServerWithPipelineViews>) {

    constructor(server: ServerInstance, string: String?) : this(
        fromJson(
            server,
            string
        )
    )

    companion object {
        private fun fromJson(
            server: ServerInstance,
            string: String?
        ): MailPackage<ServerWithPipelineViews> {
            if (string == null)
                return MailPackage.brokenPackage("string is null")
            //val res = pipelineViewsFromJsonLd(JsonUtils.fromString(string), server)
            return pipelineViewsFromJsonLd(
                Gson().fromJson(string, Any::class.java),
                server
            )
        }

        private fun pipelineViewsFromJsonLd(
            jsonObject: Any?,
            server: ServerInstance
        ): MailPackage<ServerWithPipelineViews> {
            if (jsonObject == null)
                return MailPackage.brokenPackage("null pointer")
            return when (jsonObject) {
                is ArrayList<*> -> {
                    if (jsonObject.size == 0)
                        return MailPackage.brokenPackage("size of the root arraylist is zero")
                    val list = mutableListOf<PipelineView>()
                    jsonObject.forEach {
                        val res =
                            parsePipelineView(
                                it,
                                server
                            )
                        if (res is Either.Right)
                            list.add(res.value)
                        else
                            return MailPackage.brokenPackage(
                                "some pipelineView is null"
                            )
                    }
                    return MailPackage(ServerWithPipelineViews(server, list))
                }
                else -> MailPackage.brokenPackage("root element not arraylist")
            }
        }

        private fun parsePipelineView(
            jsonObject: Any?,
            server: ServerInstance
        ): Either<String, PipelineView> {
            val pipelineRoot = prepareSemiRootElement(jsonObject)
                ?: return Either.Left("pipelineRoot is weird")
            if (pipelineRoot.size != 1)
                return Either.Left("pipelineRoot.size != 1")
            val pipelineRootMap =
                pipelineRoot[0] as? Map<*, *> ?: return Either.Left(
                    "pipelineRoot is not Map"
                )
            val pipelineView =
                makePipelineView(
                    pipelineRootMap,
                    server
                ) ?: return Either.Left(
                    "pipelineView is null"
                )
            return Either.Right(pipelineView)
        }

        private fun makePipelineView(
            map: Map<*, *>,
            server: ServerInstance
        ): PipelineView? {
            val id = giveMeThatId(map) ?: return null
            val prefLabel = giveMeThatString(map, PREF_LABEL, VALUE) ?: return null
            return PipelineView(prefLabel, id, server.id)
        }
    }
}