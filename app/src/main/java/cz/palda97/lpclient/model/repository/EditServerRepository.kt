package cz.palda97.lpclient.model.repository

import cz.palda97.lpclient.model.entities.server.ServerInstance

/**
 * Repository for storing [ServerInstance] that is currently being edited.
 */
class EditServerRepository {
    var serverToEdit = ServerInstance()
    var tmpServer = ServerInstance()
}