package cz.palda97.lpclient.viewmodel.executions

import java.text.SimpleDateFormat
import java.util.*

/**
 * Class that converts Date to display String for UI.
 */
object ExecutionDateParser {
    private const val VIEW_DATE_FORMAT = "dd.MM.yyyy"
    private val viewDateFormat
        get() = SimpleDateFormat(VIEW_DATE_FORMAT)

    /**
     * Prepares Date for UI.
     */
    fun toViewFormat(date: Date?): String? = date?.let { viewDateFormat.format(it) }
}