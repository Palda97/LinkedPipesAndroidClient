package cz.palda97.lpclient.viewmodel.editcomponent

/**
 * Data for displaying inheritance.
 */
data class InheritanceV(
    val id: String,
    val label: String,
    val inherit: Boolean
)

/**
 * Wrapper for list of [InheritanceV] and a
 * [configType][cz.palda97.lpclient.model.entities.pipeline.DialogJs.configType].
 */
data class InheritanceVWrapper(
    val inheritances: List<InheritanceV>,
    val configType: String
)