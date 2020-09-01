package cz.palda97.lpclient.model

import android.content.Context

object SharedPreferencesFactory {

    private const val SP_NAME = "preferences.xml"

    fun sharedPreferences(context: Context) = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)!!
}