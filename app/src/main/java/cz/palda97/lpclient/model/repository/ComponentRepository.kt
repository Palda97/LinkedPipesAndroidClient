package cz.palda97.lpclient.model.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cz.palda97.lpclient.model.entities.pipeline.Component

class ComponentRepository {

    private val _liveComponent: MutableLiveData<Component> = MutableLiveData()
    val liveComponent: LiveData<Component>
        get() = _liveComponent

    var currentComponent: Component? = null
        set(value) {
            _liveComponent.value = value
            field = value
        }
}