package cz.palda97.lpclient.viewmodel.executions

import java.text.SimpleDateFormat
import java.util.*

object ExecutionDateParser {
    private const val VIEW_DATE_FORMAT = "dd.MM.yyyy"
    private val viewDateFormat = SimpleDateFormat(VIEW_DATE_FORMAT)
    fun toViewFormat(date: Date): String = viewDateFormat.format(date)
}