package cz.palda97.lpclient.viewmodel.settings

import androidx.appcompat.app.AppCompatDelegate

/**
 * Enum representing a night mode settings.
 */
enum class NightModeEnum(val nightMode: Int) {
    NIGHT(AppCompatDelegate.MODE_NIGHT_YES),
    DAY(AppCompatDelegate.MODE_NIGHT_NO),
    SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

    companion object {

        /**
         * Converts AppCompatDelegate's night mode into [NightModeEnum].
         */
        fun fromNightMode(nightMode: Int): NightModeEnum = when (nightMode) {
            AppCompatDelegate.MODE_NIGHT_YES -> NIGHT
            AppCompatDelegate.MODE_NIGHT_NO -> DAY
            else -> SYSTEM
        }
    }
}

/**
 * Wrapper for [NightModeEnum] and it's correct language translation.
 */
class NightModeInstance(val string: String, val enum: NightModeEnum) {
    override fun toString(): String = string
}

/**
 * Factory for working with [NightModeInstance] lists.
 */
object NightModeFactory {

    /**
     * Create a list of [NightModeInstance]s.
     * @param day Correct translation for day mode.
     * @param night Correct translation for night mode.
     * @param system Correct translation for system settings driven night mode.
     */
    fun getList(day: String, night: String, system: String): List<NightModeInstance> = listOf(
        NightModeInstance(
            day,
            NightModeEnum.DAY
        ),
        NightModeInstance(
            night,
            NightModeEnum.NIGHT
        ),
        NightModeInstance(
            system,
            NightModeEnum.SYSTEM
        )
    )

    /**
     * Find a translation of the night mode in the list of [NightModeInstances][NightModeInstance].
     */
    fun enumAndListToString(enum: NightModeEnum, list: List<NightModeInstance>): String =
        list.find {
            it.enum == enum
        }?.string ?: ""
}