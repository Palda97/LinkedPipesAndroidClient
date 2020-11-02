package cz.palda97.lpclient.model.repository

import android.content.SharedPreferences
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.entities.pipeline.Pipeline
import cz.palda97.lpclient.model.entities.pipeline.PipelineFactory
import cz.palda97.lpclient.model.entities.pipeline.jsonLd
import cz.palda97.lpclient.model.entities.pipelineview.PipelineView
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.network.PipelineRetrofit

class PipelineRepository(private val serverDao: ServerInstanceDao, private val sharedPreferences: SharedPreferences) {

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

    private fun savePipeline(pipeline: Pipeline) {
        sharedPreferences.edit().putString(PIPELINE_JSONLD, pipeline.jsonLd()).apply()
        sharedPreferences.edit().putLong(PIPELINE_SERVER_ID, pipeline.pipelineView.serverId).apply()
    }

    private fun loadPipeline(): Pipeline? {
        val serverId = sharedPreferences.getLong(PIPELINE_SERVER_ID, 0L)
        val server = serverDao.findById(serverId) ?: return null
        val pipelineJsonLd = sharedPreferences.getString(PIPELINE_JSONLD, null)
        val mail = PipelineFactory(server, pipelineJsonLd).pipeline
        return mail.mailContent
    }

    var pipelineMail: MailPackage<Pipeline> = MailPackage.loadingPackage()

    suspend fun cachePipeline(pipelineView: PipelineView) {
        val mail = downloadPipeline(pipelineView)
        if (mail.isOk) {
            mail.mailContent!!
            savePipeline(mail.mailContent)
        }
        pipelineMail = mail
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
        private const val PIPELINE_JSONLD = "PIPELINE_JSONLD"
        private const val PIPELINE_SERVER_ID = "PIPELINE_SERVER_ID"
    }
}