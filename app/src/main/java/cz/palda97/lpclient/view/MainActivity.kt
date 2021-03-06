package cz.palda97.lpclient.view

import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        /*val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_executions,
            R.id.navigation_pipelines,
            R.id.navigation_settings
        ))*/
        //setupActionBarWithNavController(navController, appBarConfiguration)

        setupNavigation(navView, navController)
    }

    private fun setupNavigation(navView: BottomNavigationView, navController: NavController) {
        navView.setupWithNavController(navController)
        startWithTheRightFragment(navView)
    }

    private fun startWithTheRightFragment(navView: BottomNavigationView) {
        switchToFragment?.let {
            navView.selectedItemId = it
        }
        switchToFragment = null
    }

    companion object {
        var switchToFragment: Int? = null
    }
}