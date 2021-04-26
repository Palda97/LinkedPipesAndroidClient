package cz.palda97.lpclient.view

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.android.material.floatingactionbutton.FloatingActionButton
import cz.palda97.lpclient.model.MailPackage

/**
 * Floating action button cosmetics.
 */
object FABCosmetics {

    /**
     * The button will hide with animation if the [MailPackage] has
     * [LOADING][MailPackage.Status.LOADING] state and reappear
     * also with animation otherwise.
     */
    fun FloatingActionButton.hideOrShow(mailPackage: MailPackage<*>?) {
        if (mailPackage == null)
            return
        if (mailPackage.isLoading)
            hide()
        else
            show()
    }

    /**
     * Subscribe the floating action button to a LiveData. The button will hide with animation
     * when the [MailPackage] has [LOADING][MailPackage.Status.LOADING] state and reappear
     * also with animation otherwise.
     */
    fun <dummy> FloatingActionButton.hideOrShowSub(live: LiveData<MailPackage<dummy>>, lifecycleOwner: LifecycleOwner) {
        live.observe(lifecycleOwner, Observer {
            hideOrShow(it)
        })
    }
}