package cz.palda97.lpclient.viewmodel.editpipeline

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.repository.PipelineRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class EditPipelineViewModel(application: Application) : AndroidViewModel(application) {

    private val pipelineRepository: PipelineRepository = Injector.pipelineRepository

    private val retrofitScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)
    private val dbScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)

    companion object {
        private val l = Injector.generateLogFunction(this)
    }
}