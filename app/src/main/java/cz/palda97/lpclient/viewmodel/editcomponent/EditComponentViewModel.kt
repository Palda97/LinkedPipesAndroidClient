package cz.palda97.lpclient.viewmodel.editcomponent

import android.app.Application
import androidx.lifecycle.*
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.entities.pipeline.Component
import cz.palda97.lpclient.model.entities.pipeline.ConfigInput
import cz.palda97.lpclient.model.entities.pipeline.Configuration
import cz.palda97.lpclient.model.repository.ComponentRepository
import cz.palda97.lpclient.model.repository.PipelineRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class EditComponentViewModel(application: Application) : AndroidViewModel(application) {

    private val componentRepository: ComponentRepository = Injector.componentRepository
    private val pipelineRepository: PipelineRepository = Injector.pipelineRepository

    private val retrofitScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)

    val liveConfigInput: LiveData<MailPackage<Either<ComponentRepository.StatusCode, List<ConfigInput>>>>
        get() = componentRepository.liveConfigInput

    private val componentId: String?
        get() = componentRepository.currentComponentId

    val currentComponent: Component?
        get() = pipelineRepository.currentPipeline.value?.mailContent?.components?.find {
            it.id == componentId
        }

    private val configurationId: String?
        get() = currentComponent?.configurationId

    val currentConfiguration: Configuration? = pipelineRepository.currentPipeline.value?.mailContent?.configurations?.find {
        configurationId == it.id
    }

    companion object {
        private val l = Injector.generateLogFunction(this)

        fun getInstance(owner: ViewModelStoreOwner) =
            ViewModelProvider(owner).get(EditComponentViewModel::class.java)
    }
}