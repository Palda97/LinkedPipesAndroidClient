package cz.palda97.lpclient.viewmodel.settings

import androidx.appcompat.app.AppCompatDelegate

enum class NightModeEnum(val nightMode: Int) {
    NIGHT(AppCompatDelegate.MODE_NIGHT_YES),
    DAY(AppCompatDelegate.MODE_NIGHT_NO),
    SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

    companion object {
        fun fromNightMode(nightMode: Int): NightModeEnum = when (nightMode) {
            AppCompatDelegate.MODE_NIGHT_YES -> NIGHT
            AppCompatDelegate.MODE_NIGHT_NO -> DAY
            else -> SYSTEM
        }
    }
}

class NightModeInstance(val string: String, val enum: NightModeEnum) {
    override fun toString(): String = string
}

object NightModeFactory {

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

    /*
    fun enumToPosition(enum: NightModeEnum): Int = when(enum) {
        NightModeEnum.NIGHT -> 1
        NightModeEnum.DAY -> 0
        NightModeEnum.SYSTEM -> 2
    }
    */

    fun enumAndListToString(enum: NightModeEnum, list: List<NightModeInstance>): String =
        list.find {
            it.enum == enum
        }?.string ?: ""
}