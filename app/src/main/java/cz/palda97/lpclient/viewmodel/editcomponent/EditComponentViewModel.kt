package cz.palda97.lpclient.viewmodel.editcomponent

import android.app.Application
import androidx.lifecycle.*
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.entities.pipeline.*
import cz.palda97.lpclient.model.repository.ComponentRepository
import cz.palda97.lpclient.model.repository.PipelineRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditComponentViewModel(application: Application) : AndroidViewModel(application) {

    private val componentRepository: ComponentRepository = Injector.componentRepository
    //private val pipelineRepository: PipelineRepository = Injector.pipelineRepository

    private val retrofitScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)

    val liveConfigInput
        get() = componentRepository.liveConfigInput()
    val liveDialogJs
        get() = componentRepository.liveDialogJs()
    val liveBinding
        get() = componentRepository.liveBinding()

    val liveComponent
        get() = componentRepository.liveComponent

    /*val currentPipeline
        get() = componentRepository.currentPipeline*/

    suspend fun prepareConfiguration() = componentRepository.prepareConfiguration()
    fun configGetString(key: String) = componentRepository.configuration?.getString(key)
    fun configSetString(key: String, value: String) = componentRepository.configuration?.setString(key, value)
    fun persistConfiguration() = retrofitScope.launch {
        componentRepository.persistConfiguration()
    }

    companion object {
        private val l = Injector.generateLogFunction(this)

        fun getInstance(owner: ViewModelStoreOwner) =
            ViewModelProvider(owner).get(EditComponentViewModel::class.java)
    }
}