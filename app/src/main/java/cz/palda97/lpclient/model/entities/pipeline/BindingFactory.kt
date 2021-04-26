package cz.palda97.lpclient.model.entities.pipeline

import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.travelobjects.CommonFunctions
import cz.palda97.lpclient.model.travelobjects.LdConstants

/**
 * @property json JsonLd containing bindings.
 */
class BindingFactory(private val json: String) {

    /**
     * Parse [json] to list of [bindings][Binding].
     * @return Bindings or null on error.
     */
    fun parse(): List<Binding>? {
        val rootArrayList = when (val res = CommonFunctions.getRootArrayList(json)) {
            is Either.Left -> return null
            is Either.Right -> res.value
        }
        if (rootArrayList.size != 1) {
            return null
        }
        val rootMap = rootArrayList[0]
        val templateId = CommonFunctions.giveMeThatId(rootMap) ?: return null
        val semiRootElement = CommonFunctions.prepareSemiRootElement(rootMap) ?: return null
        val bindingList: MutableList<Binding> = mutableListOf()
        semiRootElement.forEach {
            val map = it as? Map<*, *> ?: return null
            val types = CommonFunctions.giveMeThoseTypes(map) ?: return null
            if (types.contains(LdConstants.TYPE_PORT_INPUT) || types.contains(LdConstants.TYPE_PORT_OUTPUT) || types.contains(
                    LdConstants.TYPE_RUNTIME_CONFIGURATION
                )
            ) {
                val binding = parseBinding(map, templateId, types) ?: return null
                bindingList.add(binding)
            }
        }
        return bindingList
    }

    /*private fun typesToType(types: List<String>): Binding.Type? =
        if (types.contains(LdConstants.TYPE_PORT)) {
            if (types.contains(LdConstants.TYPE_PORT_INPUT)) {
                Binding.Type.INPUT
            } else if (types.contains(LdConstants.TYPE_PORT_OUTPUT)) {
                Binding.Type.OUTPUT
            } else {
                null
            }
        } else if (types.contains(LdConstants.TYPE_RUNTIME_CONFIGURATION)) {
            Binding.Type.CONFIGURATION
        } else {
            null
        }*/

    private fun typesToType(types: List<String>): Binding.Type? =
        if (types.contains(LdConstants.TYPE_PORT_INPUT)) {
            Binding.Type.INPUT
        } else if (types.contains(LdConstants.TYPE_PORT_OUTPUT)) {
            Binding.Type.OUTPUT
        } else if (types.contains(LdConstants.TYPE_RUNTIME_CONFIGURATION)) {
            Binding.Type.CONFIGURATION
        } else {
            null
        }

    private fun parseBinding(map: Map<*, *>, templateId: String, types: List<String>): Binding? {
        val type = typesToType(types) ?: return null
        val id = CommonFunctions.giveMeThatId(map) ?: return null
        val bindingValue =
            CommonFunctions.giveMeThatString(map, LdConstants.BINDING, LdConstants.VALUE)
                ?: return null
        val prefLabel =
            CommonFunctions.giveMeThatString(map, LdConstants.PREF_LABEL, LdConstants.VALUE)
                ?: return null
        return Binding(templateId, type, bindingValue, prefLabel, id)
    }
}