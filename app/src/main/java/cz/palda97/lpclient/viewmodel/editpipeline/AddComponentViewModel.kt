package cz.palda97.lpclient.viewmodel.editpipeline

import android.app.Application
import androidx.lifecycle.*
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.entities.pipeline.Component
import cz.palda97.lpclient.model.entities.possiblecomponent.PossibleComponent
import cz.palda97.lpclient.model.repository.PossibleComponentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class AddComponentViewModel(application: Application) : AndroidViewModel(application) {

    private val possibleRepository: PossibleComponentRepository = Injector.possibleComponentRepository

    private val retrofitScope
        get() = CoroutineScope(Dispatchers.IO)

    fun addComponent(possibleComponent: PossibleComponent) {
        TODO("Add component and configuration to db")
    }

    val liveComponents
        get() = possibleRepository.liveComponents

    var lastSelectedComponentPosition: Int?
        get() = possibleRepository.lastSelectedComponentPosition
        set(value) {
            possibleRepository.lastSelectedComponentPosition = value
        }

    companion object {
        private val l = Injector.generateLogFunction(this)

        fun getInstance(owner: ViewModelStoreOwner) =
            ViewModelProvider(owner).get(AddComponentViewModel::class.java)
    }
}