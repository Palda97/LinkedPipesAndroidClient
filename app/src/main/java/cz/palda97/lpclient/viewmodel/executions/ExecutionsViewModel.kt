package cz.palda97.lpclient.viewmodel.executions

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import cz.palda97.lpclient.Injector

class ExecutionsViewModel(application: Application): AndroidViewModel(application) {

    //repo

    fun refreshExecutionsButton() {
        TODO()
    }

    companion object {
        private val TAG = Injector.tag(this)
        private fun l(msg: String) = Log.d(TAG, msg)
    }
}