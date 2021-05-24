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

    private const val DATE_FORMAT_NO_TIMEZONE = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    private val noTimezoneFormat = SimpleDateFormat(DATE_FORMAT_NO_TIMEZONE)

    private fun dateFromString(string: String?, format: SimpleDateFormat): Date? =
        try {
            if (string != null)
                format.parse(string)
            else
                null
        } catch (e: ParseException) {
            null
        }

    /**
     * Converts String to Date.
     * @param string String representing dateTime, including timezone.
     * @return Date or null on parse error.
     * @see <a href="http://www.w3.org/2001/XMLSchema#dateTime">http://www.w3.org/2001/XMLSchema#dateTime</a>
     */
    fun toDate(string: String?) = dateFromString(string, simpleDateFormat)

    /**
     * Converts Date to String.
     * @return String representing dateTime, including timezone.
     * @see <a href="http://www.w3.org/2001/XMLSchema#dateTime">http://www.w3.org/2001/XMLSchema#dateTime</a>
     */
    fun fromDate(date: Date): String = simpleDateFormat.format(date)

    /**
     * Converts String to Date.
     * @param string String representing dateTime, without timezone.
     * @return Date or null on parse error.
     * @see <a href="http://www.w3.org/2001/XMLSchema#dateTime">http://www.w3.org/2001/XMLSchema#dateTime</a>
     */
    fun toDateWithoutTimezone(string: String?) = dateFromString(string, noTimezoneFormat)
}