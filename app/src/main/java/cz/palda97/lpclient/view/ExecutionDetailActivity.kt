package cz.palda97.lpclient.view

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cz.palda97.lpclient.R
import cz.palda97.lpclient.view.executiondetails.ExecutionDetailsFragment

/**
 * Activity for displaying execution details.
 */
class ExecutionDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_execution_detail)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.container,
                    ExecutionDetailsFragment.newInstance()
                )
                .commitNow()
        }
    }

    companion object {

        /**
         * Function for starting this activity.
         */
        fun start(act: Context){
            val intent = Intent(act, this::class.java.declaringClass)
            act.startActivity(intent)
        }
    }
}