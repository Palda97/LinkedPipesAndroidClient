package cz.palda97.lpclient.model

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData

/**
 * Class for getting the [SharedPreferences][android.content.SharedPreferences].
 */
object SharedPreferencesFactory {

    private const val SP_NAME = "preferences.xml"

    /**
     * Gets the [SharedPreferences][android.content.SharedPreferences] object.
     */
    fun sharedPreferences(context: Context) = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)!!

    fun <T> SharedPreferences.liveData(
        key: String,
        defaultValue: T,
        getValueFromPreferences: SharedPreferences.(key: String, defaultValue: T) -> T
    ) : LiveData<T> = SPLiveData(this, key, defaultValue, getValueFromPreferences)

    private class SPLiveData<T>(
        private val sharedPreferences: SharedPreferences,
        private val key: String,
        private val defaultValue: T,
        private val getValueFromPreferences: SharedPreferences.(key: String, defaultValue: T) -> T
    ) : LiveData<T>() {

        private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener {_, key ->
            if (key == this.key)
                value = sharedPreferences.getValueFromPreferences(key, defaultValue)
        }

        override fun onActive() {
            super.onActive()
            value = sharedPreferences.getValueFromPreferences(key, defaultValue)
            sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
        }

        override fun onInactive() {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
            super.onInactive()
        }
    }
}