package cz.palda97.lpclient.viewmodel.executiondetails

import android.app.Application
import androidx.lifecycle.*
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.repository.ExecutionRepository
import cz.palda97.lpclient.model.repository.ServerRepository
import kotlinx.coroutines.*

/**
 * ViewModel for the [ExecutionDetailsFragment][cz.palda97.lpclient.view.executiondetails.ExecutionDetailsFragment].
 */
class ExecutionDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val executionRepository: ExecutionRepository = Injector.executionRepository
    private val serverRepository: ServerRepository = Injector.serverRepository

    private val retrofitScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)
    private val dbScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)

    //

    companion object {
        private val l = Injector.generateLogFunction(this)
        /**
         * Gets an instance of [ExecutionDetailsViewModel] tied to the owner's lifecycle.
         */
        fun getInstance(owner: ViewModelStoreOwner) = ViewModelProvider(owner).get(ExecutionDetailsViewModel::class.java)
    }
}