package cz.palda97.lpclient.model.repository

import android.content.SharedPreferences
import org.junit.Test
import org.junit.Assert.*
import cz.palda97.lpclient.*
import cz.palda97.lpclient.model.db.dao.PipelineDao
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.entities.server.ServerInstance
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before

class PipelineRepositoryTest
    : TestWithDb() {

    private lateinit var serverDao: ServerInstanceDao
    private lateinit var pipelineDao: PipelineDao
    private lateinit var sharedPreferences: SharedPreferences
    private var spPipelineServerId = 0L
    private fun sharedPreferencesServer(server: ServerInstance) {
        spPipelineServerId = server.id
    }
    private var spPipelineId: String? = null
    private var spCacheStatus: String? = null
    private lateinit var repo: PipelineRepository

    @Before
    fun setupDb() {
        serverDao = db.serverDao()
        runBlocking { SERVERS.forEach { serverDao.insertServer(it) } }
        pipelineDao = db.pipelineDao()
        mockSharedPreferences()
        sharedPreferencesServer(SERVERS.first())
        repo = PipelineRepository(serverDao, pipelineDao, sharedPreferences)
    }

    @Suppress("LABEL_NAME_CLASH")
    private fun mockSharedPreferences() {
        spPipelineServerId = 0L
        spPipelineId = null
        spCacheStatus = null
        sharedPreferences = mockk() {
            every { getLong(PipelineRepository.PIPELINE_SERVER_ID, 0) } answers { spPipelineServerId }
            every { getString(PipelineRepository.PIPELINE_ID, null) } answers { spPipelineId }
            every { getString(PipelineRepository.CACHE_STATUS, null) } answers { spCacheStatus }
            every { edit() } returns mockk() {
                every { putLong(PipelineRepository.PIPELINE_SERVER_ID, any()) } answers {
                    spPipelineServerId = secondArg()
                    this@mockk
                }
                every { putString(PipelineRepository.PIPELINE_ID, any()) } answers {
                    spPipelineId = secondArg()
                    this@mockk
                }
                every { putString(PipelineRepository.CACHE_STATUS, any()) } answers {
                    spCacheStatus = secondArg()
                    this@mockk
                }
                every { apply() } returns Unit
                every { remove(PipelineRepository.CACHE_STATUS) } answers {
                    spCacheStatus = null
                    this@mockk
                }
            }
        }
    }

    @Test
    fun cachePipelineInit() {
        TODO()
    }

    @Test
    fun cannotSavePipelineForUpload() {
        TODO()
    }

    @Test
    fun createPipelineInit() {
        TODO()
    }

    @Test
    fun insertCurrentPipelineView() {
        TODO()
    }

    @Test
    fun liveNewPipeline() {
        TODO()
    }

    @Test
    fun livePipeline() {
        TODO()
    }

    @Test
    fun liveUploadStatus() {
        TODO()
    }

    @Test
    fun resetLiveNewPipeline() {
        TODO()
    }

    @Test
    fun resetUploadStatus() {
        TODO()
    }

    @Test
    fun retryCachePipeline() {
        TODO()
    }

    @Test
    fun savePipeline() {
        TODO()
    }

    @Test
    fun uploadPipeline() {
        TODO()
    }

    companion object {

        private val SERVERS = listOf(
            ServerInstance("server666", "https://example.com").apply { id = 666L },
            ServerInstance("server777", "8.8.8.8").apply { id = 777L }
        )
    }
}