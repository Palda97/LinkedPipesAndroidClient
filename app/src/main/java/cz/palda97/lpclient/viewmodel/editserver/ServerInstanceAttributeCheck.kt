package cz.palda97.lpclient.viewmodel.editserver

import cz.palda97.lpclient.model.entities.server.ServerInstance

/**
 * Class for checking if this server's name and url are valid.
 */
class ServerInstanceAttributeCheck(serverInstance: ServerInstance) {

    /**
     * Check for name and url validity.
     * Returns SaveStatus.WORKING when ok.
     */
    val status: EditServerViewModel.SaveStatus = with(serverInstance) {
        when {
            name.isEmpty() -> EditServerViewModel.SaveStatus.EMPTY_NAME
            url.isEmpty() -> EditServerViewModel.SaveStatus.EMPTY_URL
            else -> EditServerViewModel.SaveStatus.WORKING
        }
    }
}