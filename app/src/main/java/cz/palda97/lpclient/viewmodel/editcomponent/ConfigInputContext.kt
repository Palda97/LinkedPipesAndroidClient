package cz.palda97.lpclient.viewmodel.editcomponent

import cz.palda97.lpclient.model.entities.pipeline.ConfigInput
import cz.palda97.lpclient.model.entities.pipeline.Configuration
import cz.palda97.lpclient.model.entities.pipeline.DialogJs
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