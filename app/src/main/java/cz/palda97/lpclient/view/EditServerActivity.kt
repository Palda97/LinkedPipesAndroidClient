package cz.palda97.lpclient.view

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cz.palda97.lpclient.R
import cz.palda97.lpclient.view.editserver.EditServerFragment

class EditServerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_server)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.container,
                    EditServerFragment.newInstance()
                )
                .commitNow()
        }
    }

    companion object {
        fun start(act: Context){
            val intent = Intent(act, this::class.java.declaringClass)
            act.startActivity(intent)
        }
    }
}