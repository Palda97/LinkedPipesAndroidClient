package cz.palda97.lpclient.viewmodel.editcomponent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.entities.pipeline.Component
import cz.palda97.lpclient.model.repository.ComponentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditComponentViewModel(application: Application) : AndroidViewModel(application) {

    private val componentRepository: ComponentRepository = Injector.componentRepository

    private val retrofitScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)

    val liveComponent: LiveData<Component>
        get() = componentRepository.liveComponent

    init {
        if (first) {
            first = false
            retrofitScope.launch {
                componentRepository.currentComponent?.let {
                    /*val configInputs = when(val res = componentRepository.downloadDialog(it)) {
                        is Either.Left -> return@let
                        is Either.Right -> res.value
                    }*/
                    val configInputs = componentRepository.downloadDialog(it)
                    //l(configInputs)
                }
            }
        }
    }

    companion object {
        private val l = Injector.generateLogFunction(this)

        fun getInstance(owner: ViewModelStoreOwner) =
            ViewModelProvider(owner).get(EditComponentViewModel::class.java)

        private var first = true
    }
}