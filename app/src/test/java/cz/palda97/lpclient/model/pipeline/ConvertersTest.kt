package cz.palda97.lpclient.model.pipeline

import com.google.gson.Gson
import cz.palda97.lpclient.*
import cz.palda97.lpclient.model.db.Converters
import org.junit.Test

import org.junit.Assert.*

class ConvertersTest {

    @Test
    fun configFromString() {
        val string = CONFIG_LIST
        val converters = Converters()
        val config = converters.toListConfig(string)
        assertNotNull("config is null", config)
    }

    @Test
    fun pairFromString() {
        val string = PAIR_LIST
        val converters = Converters()
        val configInput = converters.toListOfPairs(string)
        assertNotNull("configInput is null", configInput)
    }

    companion object {
        private const val CONFIG_LIST = "[{\"id\":\"http://localhost:8080/resources/pipelines/1604082676059/component/a0db-a8d9/configuration\",\"settings\":{\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#encodeUrl\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#boolean\",\"@value\":\"false\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#encodeUrlControl\":[{\"@id\":\"http://plugins.linkedpipes.com/resource/configuration/None\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileName\":[{\"@value\":\"abc.txt\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileNameControl\":[{\"@id\":\"http://plugins.linkedpipes.com/resource/configuration/None\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileUri\":[{\"@value\":\"http://localhost:1234/abc.txt\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#fileUriControl\":[{\"@id\":\"http://plugins.linkedpipes.com/resource/configuration/None\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#hardRedirect\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#boolean\",\"@value\":\"false\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#hardRedirectControl\":[{\"@id\":\"http://plugins.linkedpipes.com/resource/configuration/None\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#userAgent\":[{\"@value\":\"\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#userAgentControl\":[{\"@id\":\"http://plugins.linkedpipes.com/resource/configuration/None\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#utf8Redirect\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#boolean\",\"@value\":\"false\"}],\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#utf8RedirectControl\":[{\"@id\":\"http://plugins.linkedpipes.com/resource/configuration/None\"}]},\"type\":\"http://plugins.linkedpipes.com/ontology/e-httpGetFile#Configuration\"}]"
        private const val PAIR_LIST = "[{\"first\":\"CSV\",\"second\":\"CSV\"},{\"first\":\"DBF\",\"second\":\"DBF\"},{\"first\":\"XLS\",\"second\":\"XLS\"}]"
    }
}