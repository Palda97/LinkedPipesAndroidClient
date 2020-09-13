package cz.palda97.lpclient.model

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateParser {
    private const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
    private val simpleDateFormat = SimpleDateFormat(DATE_FORMAT)

    fun toDate(string: String?): Date? =
        try {
            if (string != null)
                simpleDateFormat.parse(string)
            else
                null
        } catch (e: ParseException) {
            null
        }

    fun fromDate(date: Date): String = simpleDateFormat.format(date)
}