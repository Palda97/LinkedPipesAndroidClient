package cz.palda97.lpclient.view

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.android.material.floatingactionbutton.FloatingActionButton
import cz.palda97.lpclient.model.MailPackage

object FABCosmetics {
    fun FloatingActionButton.hideOrShow(mailPackage: MailPackage<*>?) {
        if (mailPackage == null)
            return
        if (mailPackage.isLoading)
            hide()
        else
            show()
    }

    fun <dummy> FloatingActionButton.hideOrShowSub(live: LiveData<MailPackage<dummy>>, lifecycleOwner: LifecycleOwner) {
        live.observe(lifecycleOwner, Observer {
            hideOrShow(it)
        })
    }
}