package cz.palda97.lpclient

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun String.println() {
    println(this)
}

fun sleep(timeMillis: Long) = runBlocking {
    delay(timeMillis)
}