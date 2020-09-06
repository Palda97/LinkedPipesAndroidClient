package cz.palda97.lpclient.viewmodel.pipelines

import com.google.gson.Gson
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.ServerInstance
import cz.palda97.lpclient.model.travelobjects.CommonFunctions.giveMeThatId
import cz.palda97.lpclient.model.travelobjects.CommonFunctions.giveMeThatString
import cz.palda97.lpclient.model.travelobjects.CommonFunctions.prepareSemiRootElement
import cz.palda97.lpclient.model.travelobjects.LdConstants.PREF_LABEL
import cz.palda97.lpclient.model.travelobjects.LdConstants.VALUE

class PipelineViewFactory(val pipelineList: List<PipelineView>?) {

    constructor(server: ServerInstance, string: String?) : this(fromJson(server, string))

    companion object {
        private fun fromJson(server: ServerInstance, string: String?): List<PipelineView>? {
            if (string == null)
                return null
            //val res = pipelineViewsFromJsonLd(JsonUtils.fromString(string), server)
            val res = pipelineViewsFromJsonLd(Gson().fromJson(string, Any::class.java), server)
            return when (res) {
                is Either.Left -> null
                is Either.Right -> res.value
            }
        }

        private fun pipelineViewsFromJsonLd(
            jsonObject: Any?,
            server: ServerInstance
        ): Either<String, List<PipelineView>> {
            if (jsonObject == null)
                return Either.Left("null pointer")
            return when (jsonObject) {
                is ArrayList<*> -> {
                    if (jsonObject.size == 0)
                        return Either.Left("size of the root arraylist is zero")
                    val list = mutableListOf<PipelineView>()
                    jsonObject.forEach {
                        val res = parsePipelineView(it, server)
                        if (res is Either.Right)
                            list.add(res.value)
                        else
                            return Either.Left(
                                "pipelineView is null"
                            )
                    }
                    return Either.Right(list)
                }
                else -> Either.Left("root element not arraylist")
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
                makePipelineView(pipelineRootMap, server) ?: return Either.Left(
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
            return PipelineView(prefLabel, id, server)
        }
    }
}