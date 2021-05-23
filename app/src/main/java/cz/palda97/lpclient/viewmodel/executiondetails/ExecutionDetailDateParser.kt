package cz.palda97.lpclient.viewmodel.executiondetails

import java.text.SimpleDateFormat
import java.util.*

/**
 * Class that converts Date to display String for UI.
 */
object ExecutionDetailDateParser {
    private const val VIEW_DATE_FORMAT = "HH:mm:ss dd.MM.yyyy"
    private val viewDateFormat = SimpleDateFormat(VIEW_DATE_FORMAT)

    /**
     * Prepares Date for UI.
     */
    fun toViewFormat(date: Date?): String? = date?.let { viewDateFormat.format(it) }

    fun duration(start: Date?, end: Date?): String? {
        if (start == null || end == null) {
            return null
        }
        val ms = end.time - start.time
        val time = ms / 1000
        val s = time % 60
        val m = (time / 60) % 60
        val h = (time / 3600)
        return "${h.twoDigitsAtLeast}:${m.twoDigitsAtLeast}:${s.twoDigitsAtLeast}"
    }

    private val Long.twoDigitsAtLeast: String
        get() = if (this < 10) "0$this" else "$this"
}