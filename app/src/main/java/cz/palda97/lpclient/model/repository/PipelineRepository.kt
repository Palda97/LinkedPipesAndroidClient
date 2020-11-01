package cz.palda97.lpclient.model.repository

import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.entities.pipeline.Pipeline
import cz.palda97.lpclient.model.entities.pipeline.PipelineFactory
import cz.palda97.lpclient.model.entities.pipelineview.PipelineView
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.network.PipelineRetrofit

class PipelineRepository(private val serverDao: ServerInstanceDao) {

    private suspend fun getPipelineRetrofit(server: ServerInstance): Either<PipelineViewRepository.StatusCode, PipelineRetrofit> =
        Injector.pipelineViewRepository.getPipelineRetrofit(server)

    private suspend fun getPipelineRetrofit(pipelineView: PipelineView): Either<PipelineViewRepository.StatusCode, PipelineRetrofit> =
        Injector.pipelineViewRepository.getPipelineRetrofit(pipelineView)

    private suspend fun downloadPipeline(pipelineView: PipelineView): MailPackage<Pipeline> {
        val server = serverDao.findById(pipelineView.serverId) ?: return MailPackage.brokenPackage("server not found")
        val pipelineString = when(val res = Injector.pipelineViewRepository.downloadPipelineString(pipelineView)) {
            is Either.Left -> return MailPackage.brokenPackage(res.value.name)
            is Either.Right -> res.value
        }
        return PipelineFactory(server, pipelineString).pipeline
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
    }
}