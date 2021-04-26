package cz.palda97.lpclient.view.editcomponent

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import cz.palda97.lpclient.R

private val TAB_TITLES = arrayOf(
    R.string.tab_text_0,
    R.string.tab_text_1,
    R.string.tab_text_2,
    R.string.tab_text_3
)

/**
 * Adapter for the tab navigation in [EditComponentActivity][cz.palda97.lpclient.view.EditComponentActivity].
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment = when(position) {
        0 -> ConfigurationFragment.getInstance()
        2 -> InheritanceFragment.getInstance()
        3 -> BindingFragment.getInstance()
        else -> GeneralFragment.getInstance()
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return 4
    }
}