package cz.palda97.lpclient.model.repository

import androidx.lifecycle.MutableLiveData
import cz.palda97.lpclient.model.ServerInstance

class EditServerRepository {
    var tmpServerInstance = ServerInstance()
    fun forgetTmpServer() {
        tmpServerInstance = ServerInstance()
    }

    var rewrite = true

    val doneButtonEnable = MutableLiveData(true)
}