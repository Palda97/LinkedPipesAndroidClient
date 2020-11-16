package cz.palda97.lpclient.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.ActivityEditComponentBinding
import cz.palda97.lpclient.view.editcomponent.SectionsPagerAdapter
import cz.palda97.lpclient.viewmodel.editcomponent.EditComponentViewModel

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

        fun setUpTitle() {
            viewModel.liveComponent.observe(this, Observer {
                val component = it ?: return@Observer
                binding.title = component.prefLabel
                binding.executePendingBindings()
            })
        }

        setUpTitle()
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
        fun start(act: Context) {
            val intent = Intent(act, this::class.java.declaringClass)
            act.startActivity(intent)
        }
    }
}