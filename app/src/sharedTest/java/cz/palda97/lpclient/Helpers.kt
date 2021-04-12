package cz.palda97.lpclient

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import cz.palda97.lpclient.model.entities.server.ServerInstance
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers
import org.junit.Assert
import retrofit2.Retrofit
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import cz.palda97.lpclient.model.network.RetrofitHelper
import io.mockk.*
import okhttp3.ResponseBody
import retrofit2.Call

fun <T : Any?> T.println(cosmetics: (String) -> String = { it }): T {
    println(cosmetics(this.toString()))
    return this
}

fun <T : Any?> T.log(cosmetics: (String) -> String = { it }): T {
    Log.d("androidTest", cosmetics(this.toString()))
    return this
}

fun String.newLineInsteadOfComma() = replace(", ", ",\n")

fun sleep(timeMillis: Long) = runBlocking {
    delay(timeMillis)
}

fun <T : Any> T.stringFromFile(filename: String): String {
    val classLoader = javaClass.classLoader
    val path = classLoader!!.getResource(filename)?.file
    assert(path != null) { "stringFromFile: Wrong filename!" }
    val file = File(path!!)
    return file.readText()
}

inline fun <reified T> assertListContentMatch(expected: List<T>, actual: List<T>) =
    assertListContentMatch("", expected, actual)

inline fun <reified T> assertListContentMatch(msg: String, expected: List<T>, actual: List<T>) =
    Assert.assertThat(msg, actual, Matchers.containsInAnyOrder(*(expected.toTypedArray())))

fun <T> LiveData<T>.await(count: Int = 1): T? {
    require(count > 0) { "count has to be bigger than 0" }
    //"waitForValue - start".log()
    var value: T? = null
    val latch = CountDownLatch(count)
    val observer = Observer<T> {
        value = it ?: return@Observer
        //"waitForValue - observer".log()
        latch.countDown()
    }
    observeForever(observer)
    latch.await(2, TimeUnit.SECONDS)
    removeObserver(observer)
    //"waitForValue - end".log()
    return value
}

/**
 * You need to mock it yourself via
 *
 * every { retrofit.method(any()) } returns call
 *
 * or by calling finishMock
 */
inline fun <reified T : Any> mockRetrofit(
    stringFromCall: String? = "",
    server: ServerInstance? = null,
    noinline extensionBuildFun: MockKMatcherScope.(builder: Retrofit.Builder) -> T
): Pair<T, Call<ResponseBody>> {
    mockkObject(RetrofitHelper)
    return mockRetrofitAddServer(stringFromCall, server, extensionBuildFun)
}

inline fun <reified T : Any> mockRetrofitAddServer(
    stringFromCall: String? = "",
    server: ServerInstance? = null,
    noinline extensionBuildFun: MockKMatcherScope.(builder: Retrofit.Builder) -> T
): Pair<T, Call<ResponseBody>> {
    val mCall: Call<ResponseBody> = mockk()
    val mRetrofit: T = mockk()
    val mBuilder: Retrofit.Builder = mockk()
    every { extensionBuildFun(mBuilder) } returns mRetrofit
    every {
        RetrofitHelper.getBuilder(
            server ?: any(),
            server?.frontendUrl ?: any()
        )
    } returns mBuilder
    coEvery { RetrofitHelper.getStringFromCall(mCall) } returns stringFromCall
    return mRetrofit to mCall
}

fun <T> Pair<T, Call<ResponseBody>>.finishMock(f: MockKMatcherScope.(retrofit: T) -> Call<ResponseBody>): T {
    val (mRetro, mCall) = this
    every { f(mRetro) } returns mCall
    return mRetro
}

fun assertFail(msg: String = ""): Nothing {
    throw AssertionError(msg)
}
