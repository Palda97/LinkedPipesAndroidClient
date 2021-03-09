package cz.palda97.lpclient.view

import android.content.Context
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R

object ConfigDropdownMagic {
    fun <T> MaterialAutoCompleteTextView.fillWithOptions(
        context: Context,
        options: List<Pair<T, String>>? = null,
        onItemClick: (position: Int, item: T?) -> Unit = {_, _ -> }
    ): SmartArrayAdapter<T> {
        val adapter = SmartArrayAdapter<T>(
            context,
            R.layout.dropdown_item_text_view
        )
        options?.let {
            adapter.setItems(it)
        }
        setAdapter(adapter)
        setOnItemClickListener { _, _, position, _ ->
            adapter.lastSelectedPosition = position
            onItemClick(position, adapter.lastSelectedItemId)
        }
        return adapter
    }

    inline fun <reified T: Any> MaterialAutoCompleteTextView.getLastSelected(): T? {
        val adapter = adapter ?: return null
        val smartArrayAdapter = adapter as? SmartArrayAdapter<*>
            ?: return null
        val id = smartArrayAdapter.lastSelectedItemId as? T
        return id
    }

    private val l = Injector.generateLogFunction("ConfigDropdownMagic")
}