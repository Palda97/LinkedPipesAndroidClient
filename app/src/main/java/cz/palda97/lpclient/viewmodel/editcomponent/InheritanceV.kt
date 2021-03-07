package cz.palda97.lpclient.viewmodel.editcomponent

data class InheritanceV(
    val id: String,
    val label: String,
    val inherit: Boolean
)

data class InheritanceVWrapper(
    val inheritances: List<InheritanceV>,
    val configType: String
)