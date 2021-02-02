package cz.palda97.lpclient

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File

fun <T: Any?>T.println(): T {
    println(this)
    return this
}

fun sleep(timeMillis: Long) = runBlocking {
    delay(timeMillis)
}

fun <T: Any> T.stringFromFile(filename: String): String {
    val classLoader = javaClass.classLoader
    val path = classLoader!!.getResource(filename)?.file
    assert(path != null) { "stringFromFile: Wrong filename!" }
    val file = File(path!!)
    return file.readText()
}