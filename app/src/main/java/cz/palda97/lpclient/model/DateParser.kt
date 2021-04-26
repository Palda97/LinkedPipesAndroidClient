package cz.palda97.lpclient.model

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Converter between string representation of time and Date object.
 */
object DateParser {
    private const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
    private val simpleDateFormat = SimpleDateFormat(DATE_FORMAT)

    /**
     * Converts String to Date.
     * @return Date or null on parse error.
     */
    fun toDate(string: String?): Date? =
        try {
            if (string != null)
                simpleDateFormat.parse(string)
            else
                null
        } catch (e: ParseException) {
            null
        }

    /**
     * Converts Date to String.
     */
    fun fromDate(date: Date): String = simpleDateFormat.format(date)
}