package cz.palda97.lpclient.viewmodel.executiondetails

import android.app.Application
import androidx.lifecycle.*
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.repository.ExecutionDetailRepository
import kotlinx.coroutines.*

/**
 * ViewModel for the [ExecutionDetailsFragment][cz.palda97.lpclient.view.executiondetails.ExecutionDetailsFragment].
 */
class ExecutionDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val detailRepository = Injector.executionDetailRepository

    val liveDetail: LiveData<ExecutionDetailViewStructure>
        get() = detailRepository.liveDetail.map {
            ExecutionDetailViewStructure(it)
        }

    val liveUpdateError: LiveData<ExecutionDetailRepository.ExecutionDetailRepositoryStatus>
        get() = detailRepository.liveUpdateError

    val updateErrorNeutralValue = ExecutionDetailRepository.ExecutionDetailRepositoryStatus.OK

    fun resetUpdateError() {
        detailRepository.liveUpdateError.value = updateErrorNeutralValue
    }

    val pipelineName
        get() = detailRepository.currentPipelineName

    /** @see [ExecutionDetailRepository.executionLink] **/
    suspend fun executionLink() = withContext(Dispatchers.IO) {
        detailRepository.executionLink()
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
        /**
         * Gets an instance of [ExecutionDetailsViewModel] tied to the owner's lifecycle.
         */
        fun getInstance(owner: ViewModelStoreOwner) = ViewModelProvider(owner).get(ExecutionDetailsViewModel::class.java)
    }
}