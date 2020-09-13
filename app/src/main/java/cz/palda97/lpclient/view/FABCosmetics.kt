package cz.palda97.lpclient.view

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
}