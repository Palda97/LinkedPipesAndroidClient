package cz.palda97.lpclient.model.entities.pipelineview

import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.travelobjects.CommonFunctions
import cz.palda97.lpclient.model.travelobjects.CommonFunctions.giveMeThatId
import cz.palda97.lpclient.model.travelobjects.CommonFunctions.giveMeThatString
import cz.palda97.lpclient.model.travelobjects.CommonFunctions.prepareSemiRootElement
import cz.palda97.lpclient.model.travelobjects.LdConstants
import cz.palda97.lpclient.model.travelobjects.LdConstants.PREF_LABEL
import cz.palda97.lpclient.model.travelobjects.LdConstants.VALUE

/**
 * Factory for transforming jsonLd to list of [PipelineView].
 * @property serverWithPipelineViews [MailPackage] with server and parsed pipelineViews.
 */
class PipelineViewFactory(val serverWithPipelineViews: MailPackage<ServerWithPipelineViews>) {

    /**
     * @param server Server that belongs to the pipelineViews.
     * @param string JsonLd with pipelineViews.
     */
    constructor(server: ServerInstance, string: String?) : this(
        fromJson(
            server,
            string
        )
    )

    companion object {
        private val l = Injector.generateLogFunction(this)

        private fun fromJson(
            server: ServerInstance,
            string: String?
        ): MailPackage<ServerWithPipelineViews> {
            return when (val res = CommonFunctions.getRootArrayList(string)) {
                is Either.Left -> MailPackage.brokenPackage(
                    res.value
                )
                is Either.Right -> {
                    val list = mutableListOf<PipelineView>()
                    res.value.forEach {
                        val resPipe =
                            parsePipelineView(
                                it,
                                server
                            )
                        if (resPipe is Either.Right)
                            list.add(resPipe.value)
                        else
                            return MailPackage.brokenPackage(
                                "some pipelineView is null"
                            )
                    }
                    MailPackage(
                        ServerWithPipelineViews(
                            server,
                            list
                        )
                    )
                }
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

        /**
         * Creates [PipelineView].
         * @param map Map containing the fields of PipelineView.
         * @param server [ServerInstance] corresponding to the PipelineView.
         * @return PipelineView or null on error.
         */
        fun makePipelineView(
            map: Map<*, *>,
            server: ServerInstance
        ): PipelineView? {
            val id = giveMeThatId(map) ?: return null
            val prefLabel = giveMeThatString(map, PREF_LABEL, VALUE) ?: return null
            return PipelineView(
                prefLabel,
                id,
                server.id
            ).apply {
                version = giveMeThatString(map, LdConstants.VERSION, VALUE)?.toIntOrNull()
            }
        }
    }
}