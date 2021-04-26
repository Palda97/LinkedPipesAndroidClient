package cz.palda97.lpclient.model

import android.content.Context

/**
 * Class for getting the [SharedPreferences][android.content.SharedPreferences].
 */
object SharedPreferencesFactory {

    private const val SP_NAME = "preferences.xml"

    /**
     * Gets the [SharedPreferences][android.content.SharedPreferences] object.
     */
    fun sharedPreferences(context: Context) = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)!!
}