package cz.palda97.lpclient.view

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import cz.palda97.lpclient.R
import cz.palda97.lpclient.viewmodel.MainActivityViewModel

/**
 * The main activity containing fragments for displaying executions, pipelineViews and settings with servers.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainActivityViewModel
    private lateinit var navView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        /*val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_executions,
            R.id.navigation_pipelines,
            R.id.navigation_settings
        ))*/
        //setupActionBarWithNavController(navController, appBarConfiguration)

        navView.setupWithNavController(navController)

        viewModel = MainActivityViewModel.getInstance(this)
    }

    private fun startWithTheRightFragment() {
        switchToFragment?.let {
            navView.selectedItemId = it
        }
        switchToFragment = null
    }

    override fun onResume() {
        super.onResume()
        startWithTheRightFragment()
    }

    companion object {
        var switchToFragment: Int? = null
    }
}