package cz.palda97.lpclient.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import cz.palda97.lpclient.model.services.ExecutionMonitorPeriodic
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel for automatic execution refresh.
 */
class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    init {
        automaticRefresh()
    }

    private fun automaticRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(AUTOMATIC_REFRESH_RATE)
                ExecutionMonitorPeriodic.enqueueOneShot(getApplication())
            }
        }
    }

    companion object {

        /**
         * Gets an instance of [MainActivityViewModel] tied to the owner's lifecycle.
         */
        fun getInstance(owner: ViewModelStoreOwner) = ViewModelProvider(owner).get(MainActivityViewModel::class.java)

        private const val AUTOMATIC_REFRESH_RATE = 5000L
    }
}