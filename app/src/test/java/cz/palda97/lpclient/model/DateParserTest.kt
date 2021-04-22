package cz.palda97.lpclient.model

import cz.palda97.lpclient.MockkTest
import org.junit.Assert
import org.junit.Test

class DateParserTest
    : MockkTest() {

    @Test
    fun dateParserBasic() {
        val input: String? = "2020-09-10T20:08:38.758+02:00"
        val date = DateParser.toDate(input)
        Assert.assertNotNull(date)
        date!!
        val output = DateParser.fromDate(date)
        Assert.assertEquals(input, output)
    }

    @Test
    fun dateParserNull() {
        val input: String? = "2020-09-10Terror20:08:38.758+02:00"
        val date = DateParser.toDate(input)
        Assert.assertNull(date)
    }

    @Test
    fun dateParserNull2() {
        val input: String? = null
        val date = DateParser.toDate(input)
        Assert.assertNull(date)
    }
}