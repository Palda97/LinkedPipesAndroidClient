package cz.palda97.lpclient.model.entities.pipeline

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing template's binding.
 */
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

    /**
     * Check if these two bindings are at the same side.
     * Configuration is the same side as input.
     */
    infix fun isSameSideAs(binding: Binding) = type.side == binding.type.side
}