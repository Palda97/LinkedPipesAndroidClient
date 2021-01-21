package cz.palda97.lpclient.viewmodel.editcomponent

import cz.palda97.lpclient.model.entities.pipeline.*
import cz.palda97.lpclient.model.repository.ComponentRepository

sealed class ConfigInputContext {
    abstract val status: ComponentRepository.StatusCode
}

data class OnlyStatus(override val status: ComponentRepository.StatusCode): ConfigInputContext()

data class ConfigInputComplete(
    override val status: ComponentRepository.StatusCode,
    //val configuration: Configuration,
    val dialogJs: DialogJs,
    val configInputs: List<ConfigInput>
): ConfigInputContext()

data class BindingComplete(
    override val status: ComponentRepository.StatusCode,
    val bindings: List<Binding>,
    val connections: List<Connection>,
    val components: List<Component>,
    val templates: List<Template>,
    val otherBindings: List<StatusWithBinding>
): ConfigInputContext()

data class ConnectionV(
    override val status: ComponentRepository.StatusCode,
    val connections: List<Pair<Connection, ConnectionItem>>
): ConfigInputContext() {
    data class ConnectionItem(
        val component: String,
        val source: String,
        val target: String
    )
}