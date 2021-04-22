package cz.palda97.lpclient

import android.util.Log
import cz.palda97.lpclient.*
import cz.palda97.lpclient.model.entities.server.ServerInstance
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.AfterClass
import org.junit.BeforeClass

abstract class MockkTest {

    companion object {

        val SERVER = ServerInstance("Home Server", "http://localhost:8080/")

        @JvmStatic
        @BeforeClass
        fun mockLog() {
            mockkStatic(Log::class)
            every { Log.d(any(), any()) } answers {
                val tag: String? = arg(0)
                val msg: String? = arg(1)
                println("$tag: $msg")
                0
            }
        }

        @JvmStatic
        @AfterClass
        fun unMockAll() {
            unmockkAll()
        }
    }
}