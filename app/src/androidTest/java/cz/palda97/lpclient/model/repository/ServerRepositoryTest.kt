package cz.palda97.lpclient.model.repository

import androidx.lifecycle.LiveData
import org.junit.Assert.*
import cz.palda97.lpclient.*
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao
import cz.palda97.lpclient.model.entities.server.ServerInstance
import io.mockk.*
import kotlinx.coroutines.*
import org.junit.*

class ServerRepositoryTest
    : TestWithDb() {

    private lateinit var serverDao: ServerInstanceDao
    private lateinit var serverRepo: ServerRepository

    private val serverFullList = listOf(
        ServerInstance("1", "url1", true, "", false).apply { id = 1 },
        ServerInstance("2", "url2", true, "", false).apply { id = 2 },
        ServerInstance("3", "url3", true, "", false).apply { id = 3 },
        ServerInstance("4", "url4", false, "", false).apply { id = 4 },
        ServerInstance("5", "url5", false, "", false).apply { id = 5 }
    )

    @Before
    fun defineDaos() {
        serverDao = db.serverDao()
        serverRepo = ServerRepository(serverDao)
    }

    private lateinit var routines: RepositoryRoutines
    private lateinit var mockDao: ServerInstanceDao
    private lateinit var mockRepo: ServerRepository
    @Before
    fun mock() {
        routines = mockk()
        coEvery { routines.update(any()) } returns mockk()
        mockkObject(Injector)
        every { Injector.repositoryRoutines } returns routines

        mockDao = mockk(relaxed = true)
        mockRepo = ServerRepository(mockDao)
    }

    @Test
    fun insertServerAndLiveData() {
        fun <T> LiveData<MailPackage<List<T>>>.assertSize(size: Int): List<T> {
            val list = await()!!.mailContent
            assertEquals(size, list!!.size)
            return list
        }
        runBlocking {
            serverFullList.forEach {
                serverRepo.insertServer(it)
            }
        }
        val defaultActiveSize = serverFullList.filter { it.active }.size
        serverRepo.liveServers.assertSize(serverFullList.size)
        serverRepo.activeLiveServers.assertSize(defaultActiveSize)
        val active = runBlocking { serverRepo.activeServers() }
        assertEquals(defaultActiveSize, active.size)
        //coVerify(exactly = defaultActiveSize) { routines.update(any()) }
    }

    private fun List<ServerInstance>.insert() = runBlocking {
        forEach {
            serverDao.insertServer(it)
        }
    }

    @Test
    fun delete() {
        val serverToDelete = serverFullList.first()
        runBlocking {
            mockRepo.deleteServer(serverToDelete)
            mockRepo.deleteAll()
        }
        coVerify {
            mockDao.deleteServer(serverToDelete)
            mockDao.deleteAll()
        }
    }

    @Test
    fun matchExcept() {
        serverFullList.insert()
        val noMatchServer = ServerInstance("6", "url6")
        val last = serverFullList.last()
        runBlocking { serverDao.insertServer(noMatchServer) }
        val nameMatchServer = ServerInstance(last.name, noMatchServer.url)
        val urlMatchServer = ServerInstance(noMatchServer.name, last.url)
        val bothMatch = ServerInstance(last.name, last.url)
        val no = runBlocking { serverRepo.matchUrlOrNameExcept(noMatchServer, noMatchServer) }
        val name = runBlocking { serverRepo.matchUrlOrNameExcept(nameMatchServer, noMatchServer) }
        val url = runBlocking { serverRepo.matchUrlOrNameExcept(urlMatchServer, noMatchServer) }
        val both = runBlocking { serverRepo.matchUrlOrNameExcept(bothMatch, noMatchServer) }
        assertEquals(ServerRepository.MatchCases.NO_MATCH, no)
        assertEquals(ServerRepository.MatchCases.NAME, name)
        assertEquals(ServerRepository.MatchCases.URL, url)
        assertEquals(ServerRepository.MatchCases.URL, both)
    }
}