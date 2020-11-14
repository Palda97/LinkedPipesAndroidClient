package cz.palda97.lpclient.viewmodel.editcomponent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import cz.palda97.lpclient.Injector

class EditComponentViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private val l = Injector.generateLogFunction(this)

        fun getInstance(owner: ViewModelStoreOwner) =
            ViewModelProvider(owner).get(EditComponentViewModel::class.java)
    }
}