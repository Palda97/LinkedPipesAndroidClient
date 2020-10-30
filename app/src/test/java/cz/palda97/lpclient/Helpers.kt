package cz.palda97.lpclient

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun Any?.println() {
    println(this)
}

fun sleep(timeMillis: Long) = runBlocking {
    delay(timeMillis)
}