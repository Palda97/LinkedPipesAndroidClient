package cz.palda97.lpclient.model.pipeline

import com.google.gson.Gson
import cz.palda97.lpclient.*
import cz.palda97.lpclient.model.db.Converters
import cz.palda97.lpclient.model.entities.pipeline.Config
import org.junit.Test

import org.junit.Assert.*

class ConvertersTest {

    fun parseConfigList(): List<Config>? {
        val string = CONFIG_LIST
        val converters = Converters()
        val list = converters.toListConfig(string)
        return list
    }

    @Test
    fun configFromString() {
        val list = parseConfigList()
        assertNotNull("config is null", list)
        assertEquals(1, list!!.size)
    }

    @Test
    fun editConfig() {
        val list = parseConfigList() ?: return
        if (list.size != 1)
            return
        val config = list[0]
        val original = parseConfigList()!![0]
        assertEquals(false.toString(), config.getString(CONFIG_LIST_KEY))
        config.setString(CONFIG_LIST_KEY, true.toString())
        assertEquals(true.toString(), config.getString(CONFIG_LIST_KEY))
        val configSplit = Gson().toJson(config.settings).split("\"")
        val originalSplit = Gson().toJson(original.settings).split("\"")
        assertEquals(configSplit.size, originalSplit.size)
        assertEquals(CONFIG_LIST_SIZE, configSplit.size)
        assertEquals(originalSplit.filterIndexed { index, _ ->
            index != CONFIG_LIST_CHANGED
        }, configSplit.filterIndexed { index, _ ->
            index != CONFIG_LIST_CHANGED
        })
        assertNotEquals(originalSplit[CONFIG_LIST_CHANGED], configSplit[CONFIG_LIST_CHANGED])
    }

    @Test
    fun pairFromString() {
        val string = PAIR_LIST
        val converters = Converters()
        val configInput = converters.toListOfPairs(string)
        assertNotNull("configInput is null", configInput)
    }

    companion object {
        private const val CONFIG_LIST_SIZE = 85
        private const val CONFIG_LIST_CHANGED = 9
        private const val CONFIG_LIST_KEY =
            "http://plugins.linkedpipes.com/ontology/e-httpGetFile#encodeUrl"
        private const val CONFIG_LIST =
            "[{\"id\":\"http://localhost:8080/resources/pipelines/1604082676059/component/a0db-a8d9/configuration\",\"settings\":{\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#encodeUrl\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#boolean\",\"@value\":\"false\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#encodeUrlControl\":[{\"@id\":\"http://plugins.linkedpipes.com/resource/configuration/None\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileName\":[{\"@value\":\"abc.txt\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileNameControl\":[{\"@id\":\"http://plugins.linkedpipes.com/resource/configuration/None\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileUri\":[{\"@value\":\"http://localhost:1234/abc.txt\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileUriControl\":[{\"@id\":\"http://plugins.linkedpipes.com/resource/configuration/None\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#hardRedirect\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#boolean\",\"@value\":\"false\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#hardRedirectControl\":[{\"@id\":\"http://plugins.linkedpipes.com/resource/configuration/None\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#userAgent\":[{\"@value\":\"\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#userAgentControl\":[{\"@id\":\"http://plugins.linkedpipes.com/resource/configuration/None\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#utf8Redirect\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#boolean\",\"@value\":\"false\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#utf8RedirectControl\":[{\"@id\":\"http://plugins.linkedpipes.com/resource/configuration/None\"}]},\"type\":\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#Configuration\"}]"
        private const val PAIR_LIST =
            "[{\"first\":\"CSV\",\"second\":\"CSV\"},{\"first\":\"DBF\",\"second\":\"DBF\"},{\"first\":\"XLS\",\"second\":\"XLS\"}]"
    }
}