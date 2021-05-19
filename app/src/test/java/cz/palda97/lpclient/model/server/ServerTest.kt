package cz.palda97.lpclient.model.server

import cz.palda97.lpclient.*
import cz.palda97.lpclient.model.entities.server.ServerFactory
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.entities.server.ServerInstance.Companion.urlWithFixedProtocol
import org.junit.Test
import org.junit.Assert.*

class ServerTest
    : MockkTest() {

    @Test
    fun urlWithFixedProtocol() {
        assertEquals("https://www.example.com", "www.example.com".urlWithFixedProtocol)
        assertEquals("https://www.example.com/dir", "www.example.com/dir".urlWithFixedProtocol)
        assertEquals("https://www.example.com/dir0/dir1", "www.example.com/dir0/dir1".urlWithFixedProtocol)
        assertEquals("http://www.example.com", "http://www.example.com".urlWithFixedProtocol)
    }

    @Test
    fun json() {
        val json = "{\n" +
                "  \"name\":\"Home Wifi\",\n" +
                "  \"url\":\"http://192.168.1.50\",\n" +
                "  \"frontend\":8080,\n" +
                "  \"description\":\"\"\n" +
                "}"
        val server = ServerInstance("Home Wifi", "http://192.168.1.50").apply {
            frontend = 8080
        }
        val res = ServerFactory.fromJson(json)
        assertNotNull(res)
        res!!
        assertTrue(
            res.name == server.name
                    && res.url == server.url
                    && res.frontend == server.frontend
        )
    }

    @Test
    fun jsonNull() {
        val res = ServerFactory.fromJson("bad json")
        assertNull(res)
    }

    @Test
    fun frontendUrl() {
        val defaultPortUrl = "https://demo.etl.linkedpipes.com/"
        val serverDefaultPort = ServerInstance("serverDefaultPort", defaultPortUrl)
        val eightyEightyPortUrl = "http://192.168.1.50/"
        val server8080 = ServerInstance("serverDefaultPort", eightyEightyPortUrl).apply { frontend = 8080 }
        assertEquals(defaultPortUrl, serverDefaultPort.frontendUrl)
        assertEquals("http://192.168.1.50:8080", server8080.frontendUrl)
    }

    @Test
    fun credentials() {
        val credentials = "username" to "password"
        val serverAuthOn = ServerInstance("auth on", auth = true).apply {
            username = credentials.first
            password = credentials.second
        }
        val serverAuthOff = ServerInstance("auto off")
        assertEquals(credentials, serverAuthOn.credentials)
        assertNull(serverAuthOff.credentials)
    }

    @Test
    fun anyOverrides() {
        val servers = listOf(
            ServerInstance().apply { id = 42 },
            ServerInstance().apply { id = 42 },
            ServerInstance().apply { id = 777 }
        )
        assertEquals(servers[0], servers[1])
        assertEquals(servers[0].hashCode(), servers[1].hashCode())
        assertNotEquals(servers[0], servers[2])
        assertNotEquals(servers[0].hashCode(), servers[2].hashCode())
    }

    companion object {

        //
    }
}