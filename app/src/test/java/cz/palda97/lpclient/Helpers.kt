package cz.palda97.lpclient

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers
import org.junit.Assert
import java.io.File

fun <T: Any?>T.println(cosmetics: (String) -> String = {it}): T {
    println(cosmetics(this.toString()))
    return this
}

fun String.newLineInsteadOfComma() = replace(", ", ",\n")

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

inline fun <reified T> assertListContentMatch(expected: List<T>, actual: List<T>) =
    assertListContentMatch("", expected, actual)
inline fun <reified T> assertListContentMatch(msg: String, expected: List<T>, actual: List<T>) =
    Assert.assertThat(msg, actual, Matchers.containsInAnyOrder(*(expected.toTypedArray())))