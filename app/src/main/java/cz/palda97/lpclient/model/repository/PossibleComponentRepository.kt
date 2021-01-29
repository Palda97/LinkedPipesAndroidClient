package cz.palda97.lpclient.model.repository

import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.db.dao.PipelineDao
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.entities.possiblecomponent.PossibleComponent
import cz.palda97.lpclient.model.entities.possiblecomponent.PossibleComponentFactory
import cz.palda97.lpclient.model.entities.possiblecomponent.PossibleStatus
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.network.PipelineRetrofit
import cz.palda97.lpclient.model.network.PipelineRetrofit.Companion.pipelineRetrofit
import cz.palda97.lpclient.model.network.RetrofitHelper
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class PossibleComponentRepository(
    //private val serverDao: ServerInstanceDao,
    private val pipelineDao: PipelineDao
) {

    enum class StatusCode {
        NO_CONNECT, INTERNAL_ERROR, DOWNLOADING_ERROR, PARSING_ERROR, OK, DOWNLOAD_IN_PROGRESS;
        //SERVER_NOT_FOUND

        companion object {
            val String?.toStatus
                get() = if (this == null) {
                    INTERNAL_ERROR
                } else {
                    try {
                        valueOf(this)
                    } catch (_: IllegalArgumentException) {
                        INTERNAL_ERROR
                    }
                }
        }
    }

    private suspend fun getPipelineRetrofit(server: ServerInstance): Either<StatusCode, PipelineRetrofit> =
        try {
            Either.Right(RetrofitHelper.getBuilder(server, server.frontendUrl).pipelineRetrofit)
        } catch (e: IllegalArgumentException) {
            l("getPipelineRetrofit $e")
            Either.Left(StatusCode.NO_CONNECT)
        }

    private suspend fun downloadPossibleComponents(server: ServerInstance): Either<StatusCode, List<PossibleComponent>> {
        val retrofit = when(val res = getPipelineRetrofit(server)) {
            is Either.Left -> return Either.Left(res.value)
            is Either.Right -> res.value
        }
        return downloadPossibleComponents(retrofit, server.id)
    }
    private suspend fun downloadPossibleComponents(retrofit: PipelineRetrofit, serverId: Long): Either<StatusCode, List<PossibleComponent>> {
        val call = retrofit.componentList()
        val text = RetrofitHelper.getStringFromCall(call) ?: return Either.Left(StatusCode.DOWNLOADING_ERROR)
        val factory = PossibleComponentFactory(text, serverId)
        val list = factory.parse() ?: return Either.Left(StatusCode.PARSING_ERROR)
        return Either.Right(list)
    }

    private suspend fun persistStatus(status: PossibleStatus) {
        pipelineDao.insertPossibleStatus(status)
    }

    private suspend fun persistPossibleComponents(list: List<PossibleComponent>) {
        pipelineDao.insertPossibleComponent(list)
    }

    suspend fun cachePossibleComponents(server: ServerInstance) {
        pipelineDao.prepareForPossibleComponentsDownload(server.id)
        //persistStatus(PossibleStatus(server.id, StatusCode.DOWNLOAD_IN_PROGRESS))
        val status = PossibleStatus(server.id, when(val res = downloadPossibleComponents(server)) {
            is Either.Left -> res.value
            is Either.Right -> {
                persistPossibleComponents(res.value)
                StatusCode.OK
            }
        })
        persistStatus(status)
    }

    private suspend fun downloadPossibleComponents(servers: List<ServerInstance>) = coroutineScope {
        val jobs = servers.map {
            async {
                it to downloadPossibleComponents(it)
            }
        }
        jobs.map {
            it.await()
        }
    }

    private suspend fun persistStatus(list: List<PossibleStatus>) {
        pipelineDao.insertPossibleStatus(list)
    }

    suspend fun cachePossibleComponents(servers: List<ServerInstance>) {
        pipelineDao.prepareForPossibleComponentsDownload(servers.map { it.id })
        val list = downloadPossibleComponents(servers)
        val statuses = list.map {
            val (server, either) = it
            PossibleStatus(server.id, when(either) {
                is Either.Left -> either.value
                is Either.Right -> {
                    persistPossibleComponents(either.value)
                    StatusCode.OK
                }
            })
        }
        persistStatus(statuses)
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
    }
}