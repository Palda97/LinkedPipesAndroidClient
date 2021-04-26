package cz.palda97.lpclient.viewmodel.editcomponent

import cz.palda97.lpclient.model.entities.pipeline.*
import cz.palda97.lpclient.model.repository.ComponentRepository

/**
 * A safe way for wrapping [status][ComponentRepository.StatusCode] and other things.
 */
sealed class ConfigInputContext {
    abstract val status: ComponentRepository.StatusCode
}

/**
 * Wrapper just for [status][ComponentRepository.StatusCode].
 */
data class OnlyStatus(override val status: ComponentRepository.StatusCode): ConfigInputContext()

/**
 * Wrapper for [status][ComponentRepository.StatusCode], [DialogJs] and [ConfigInput]s.
 */
data class ConfigInputComplete(
    override val status: ComponentRepository.StatusCode,
    val dialogJs: DialogJs,
    val configInputs: List<ConfigInput>
): ConfigInputContext()

/**
 * Wrapper for [status][ComponentRepository.StatusCode], and content for the
 * [BindingFragment][cz.palda97.lpclient.view.editcomponent.BindingFragment]
 * for displaying connections.
 */
data class BindingComplete(
    override val status: ComponentRepository.StatusCode,
    val bindings: List<Binding>,
    val connections: List<Connection>,
    val components: List<Component>,
    val templates: List<Template>,
    val otherBindings: List<StatusWithBinding>
): ConfigInputContext()

/**
 * Wrapper for [status][ComponentRepository.StatusCode], and content for the
 * [BindingFragment][cz.palda97.lpclient.view.editcomponent.BindingFragment]
 * for displaying connections.
 */
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