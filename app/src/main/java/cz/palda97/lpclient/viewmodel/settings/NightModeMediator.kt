package cz.palda97.lpclient.viewmodel.settings

import androidx.appcompat.app.AppCompatDelegate

class NightModeMediator(id: Int) {

    enum class NightModeEnum(id: Int) {
        NIGHT(AppCompatDelegate.MODE_NIGHT_YES),
        DAY(AppCompatDelegate.MODE_NIGHT_NO),
        SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    val nightModeEnum: NightModeEnum = when(id) {
        AppCompatDelegate.MODE_NIGHT_YES -> NightModeEnum.NIGHT
        AppCompatDelegate.MODE_NIGHT_NO -> NightModeEnum.DAY
        else -> NightModeEnum.SYSTEM
    }
}