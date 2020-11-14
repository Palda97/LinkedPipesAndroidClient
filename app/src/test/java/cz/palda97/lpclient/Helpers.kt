package cz.palda97.lpclient

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun <T: Any?>T.println(): T {
    println(this)
    return this
}

fun sleep(timeMillis: Long) = runBlocking {
    delay(timeMillis)
}