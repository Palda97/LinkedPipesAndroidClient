package cz.palda97.lpclient.model.entities.pipeline

import cz.palda97.lpclient.Injector
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class ConfigInputFactory(private val html: String, private val componentId: String) {

    fun parse(): List<ConfigInput>? {
        val doc = Jsoup.parse(html) ?: return null
        doc.getElementsByTag(MD_TABS).remove()
        val switches = parseSwitches(doc)
        val nonSwitches = parseContainers(doc)
        if (switches == null || nonSwitches == null) {
            return null
        }
        return nonSwitches + switches
    }

    private fun parseOptions(select: Element): MutableList<Pair<String, String>> {
        //println("parseOptions")
        val options = select.getElementsByTag(MD_OPTION)
        return options.map {
            val key = it.attr(VALUE)
            val value = it.text()
            key to value
        }.toMutableList()
    }

    private fun parseContainers(doc: Document): List<ConfigInput>? {
        //println("parseContainers")
        val containers = doc.getElementsByTag(MD_INPUT_CONTAINER)!!
        return containers.map {
            val labels = it.getElementsByTag(LABEL)
            /*if (labels.size != 1) {
                //println("labels.size != 1")
                return null
            }
            val label = labels[0].text()*/
            val label: String = when (labels.size) {
                0 -> it.ownText()
                1 -> labels[0].ownText()
                else -> return null
            }
            val inputs = it.getElementsByAttribute(NG_MODEL)
            if (inputs.size != 1) {
                //println("inputs.size != 1")
                return null
            }
            val input = inputs[0]
            val id = input.attr(NG_MODEL).removeSurrounding(ID_PREFIX, ID_SUFFIX)
            when(val tag = input.tag().name) {
                INPUT -> ConfigInput(label, ConfigInput.Type.EDIT_TEXT, id, componentId)
                MD_SELECT -> ConfigInput(label, ConfigInput.Type.DROPDOWN, id, componentId, parseOptions(input))
                LP_YASQE -> ConfigInput(label, ConfigInput.Type.TEXT_AREA, id, componentId)
                TEXTAREA -> ConfigInput(label, ConfigInput.Type.TEXT_AREA, id, componentId)
                else -> return null//.also { println("else: $tag") }
            }
        }
    }

    private fun parseSwitches(doc: Document): List<ConfigInput>? {
        val switches = doc.getElementsByTag(MD_SWITCH)
        return switches.map {
            val id = it.attr(NG_MODEL).removeSurrounding(ID_PREFIX, ID_SUFFIX)
            val label = it.ownText()
            ConfigInput(label, ConfigInput.Type.SWITCH, id, componentId)
        }
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
        private const val MD_SWITCH = "md-switch"
        private const val NG_MODEL = "ng-model"
        private const val ID_PREFIX = "dialog."
        private const val ID_SUFFIX = ".value"
        private const val MD_INPUT_CONTAINER = "md-input-container"
        private const val MD_SELECT = "md-select"
        private const val MD_OPTION = "md-option"
        private const val INPUT = "input"
        private const val LABEL = "label"
        private const val VALUE = "value"
        private const val LP_YASQE = "lp-yasqe"
        private const val TEXTAREA = "textarea"
        private const val MD_TABS = "md-tabs"
    }
}