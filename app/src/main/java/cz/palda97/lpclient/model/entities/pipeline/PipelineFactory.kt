package cz.palda97.lpclient.model.entities.pipeline

import android.util.Log
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.entities.server.ServerInstance

class PipelineFactory(val pipeline: MailPackage<Pipeline>) {

    constructor(server: ServerInstance, string: String?) : this(fromJson(server, string))

    companion object {
        private val TAG = Injector.tag(this)
        private fun l(msg: Any?) = Log.d(TAG, msg.toString())

        private fun fromJson(server: ServerInstance, string: String?): MailPackage<Pipeline> {
            TODO()
        }
    }
}