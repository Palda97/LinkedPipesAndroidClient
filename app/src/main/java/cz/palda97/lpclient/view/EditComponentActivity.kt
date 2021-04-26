package cz.palda97.lpclient.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.ActivityEditComponentBinding
import cz.palda97.lpclient.view.editcomponent.SectionsPagerAdapter
import cz.palda97.lpclient.viewmodel.editcomponent.EditComponentViewModel

/**
 * Activity for component editing.
 */
class EditComponentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditComponentBinding
    private lateinit var viewModel: EditComponentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_component)
        //setContentView(R.layout.activity_edit_component)
        viewModel = EditComponentViewModel.getInstance(this)
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

        setUpComponents()
    }

    private fun setUpComponents() {

        fun setUpToolbar() {
            setSupportActionBar(binding.toolbar)
            title = viewModel.currentComponent?.prefLabel
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        setUpToolbar()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_editcomponent, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
        R.id.delete_this_component_item -> {
            viewModel.deleteCurrentComponent()
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    companion object {
        private val l = Injector.generateLogFunction(this)

        /**
         * Function for starting this activity.
         */
        fun start(act: Context) {
            val intent = Intent(act, this::class.java.declaringClass)
            act.startActivity(intent)
        }
    }
}