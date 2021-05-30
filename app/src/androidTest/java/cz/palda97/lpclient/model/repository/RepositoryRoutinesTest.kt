package cz.palda97.lpclient.model.repository

import org.junit.Test
import org.junit.Assert.*
import cz.palda97.lpclient.*
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.entities.server.ServerInstance
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before

class RepositoryRoutinesTest
    : AndroidTest() {


    @RelaxedMockK private lateinit var mServerRepo: ServerRepository
    @RelaxedMockK private lateinit var mPipelineRepo: PipelineViewRepository
    @RelaxedMockK private lateinit var mExecutionRepo: ExecutionRepository
    @RelaxedMockK private lateinit var mComponentRepo: PossibleComponentRepository
    @RelaxedMockK private lateinit var mNoveltyRepository: ExecutionNoveltyRepository
    private lateinit var routines: RepositoryRoutines

    @Before
    fun mock() {
        MockKAnnotations.init(this)
        with(Injector) {
            mockkObject(this)
            every { serverRepository } returns mServerRepo
            every { pipelineViewRepository } returns mPipelineRepo
            every { executionRepository } returns mExecutionRepo
            every { possibleComponentRepository } returns mComponentRepo
            every { executionNoveltyRepository } returns mNoveltyRepository
        }
        routines = RepositoryRoutines()
    }

    @Test
    fun update() {
        val server = mockk<ServerInstance>()
        runBlocking { routines.update(server) }
        coVerify(exactly = 1) {
            mPipelineRepo.update(server)
            mExecutionRepo.update(server)
            mComponentRepo.cachePossibleComponents(server)
        }
    }

    @Test
    fun refresh() {
        val serverList = listOf(
            ServerInstance("server666", "https://example.com").apply { id = 666L },
            ServerInstance("server777", "8.8.8.8").apply { id = 777L }
        )
        coEvery { mServerRepo.activeServers() } returns serverList
        runBlocking { routines.refresh() }
        coVerify(exactly = 1) {
            mPipelineRepo.refreshPipelineViews(Either.Right(serverList))
            mExecutionRepo.cacheExecutions(Either.Right<ServerInstance, List<ServerInstance>?>(serverList), false)
            mComponentRepo.cachePossibleComponents(serverList)
        }
    }

    @Test
    fun cleanDb() {
        runBlocking { routines.cleanDb() }
        coVerify(exactly = 1) {
            mPipelineRepo.cleanDb()
            mExecutionRepo.cleanDb()
            mNoveltyRepository.cleanDb()
        }
    }

    @Test
    fun onServerChange() {
        routines.onServerToFilterChange()
        verify(exactly = 1) {
            mPipelineRepo.onServerToFilterChange()
            mExecutionRepo.onServerToFilterChange()
        }
    }
}