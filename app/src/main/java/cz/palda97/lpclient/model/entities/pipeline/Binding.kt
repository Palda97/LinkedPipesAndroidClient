package cz.palda97.lpclient.model.entities.pipeline

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Binding(
    val templateId: String,
    val type: Type,
    val bindingValue: String,
    val prefLabel: String,
    @PrimaryKey(autoGenerate = false) val id: String
) {
    enum class Type(val side: Int) {
        CONFIGURATION(0), INPUT(0), OUTPUT(1)
    }

    infix fun isSameSideAs(binding: Binding) = type.side == binding.type.side
}