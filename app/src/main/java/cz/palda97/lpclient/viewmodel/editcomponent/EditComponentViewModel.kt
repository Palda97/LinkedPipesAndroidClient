package cz.palda97.lpclient.viewmodel.editcomponent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.entities.pipeline.Component
import cz.palda97.lpclient.model.repository.ComponentRepository

class EditComponentViewModel(application: Application) : AndroidViewModel(application) {

    private val componentRepository: ComponentRepository = Injector.componentRepository

    val liveComponent: LiveData<Component>
        get() = componentRepository.liveComponent

    companion object {
        private val l = Injector.generateLogFunction(this)

        fun getInstance(owner: ViewModelStoreOwner) =
            ViewModelProvider(owner).get(EditComponentViewModel::class.java)
    }
}