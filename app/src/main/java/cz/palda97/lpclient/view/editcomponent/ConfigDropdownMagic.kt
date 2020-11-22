package cz.palda97.lpclient.view.editcomponent

import android.content.Context
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import cz.palda97.lpclient.R

object ConfigDropdownMagic {
    fun MaterialAutoCompleteTextView.fillWithOptions(context: Context, options: List<Pair<String, String>>, onItemClick: (position: Int) -> Unit = {}) {
        val adapter = SmartArrayAdapter(context, R.layout.dropdown_item_text_view)
        adapter.setItems(options)
        setAdapter(adapter)
        setOnItemClickListener { _, _, position, _ ->
            adapter.lastSelectedPosition = position
            onItemClick(position)
        }
    }
}