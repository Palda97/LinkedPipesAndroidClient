package cz.palda97.lpclient

import android.util.Log
import cz.palda97.lpclient.model.entities.server.ServerInstance
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoAnnotations
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
@PrepareForTest(
    Log::class
)
abstract class PowerMockTest {

    @Before
    fun mockLog() {
        MockitoAnnotations.initMocks(this)
        PowerMockito.mockStatic(Log::class.java)
        //PowerMockito.`when`(Log.d(any(), any())).thenReturn(0)
        PowerMockito.`when`(Log.d(any(), any())).thenAnswer {
            println("${it.arguments[0]}: ${it.arguments[1]}")
            0
        }
    }

    companion object {

        val SERVER = ServerInstance("Home Server", "http://localhost:8080/")
    }
}