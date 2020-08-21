package cz.palda97.lpclient.model.repository

import cz.palda97.lpclient.model.ServerInstance

class EditServerRepository {
    var tmpServerInstance = ServerInstance()
    fun forgetTmpServer() {
        tmpServerInstance = ServerInstance()
    }

    var rewrite = true
}