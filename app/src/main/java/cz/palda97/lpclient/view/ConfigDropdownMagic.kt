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
            adapter.items = it
        }
        setAdapter(adapter)
        setOnItemClickListener { _, _, position, _ ->
            adapter.lastSelectedPosition = position
            onItemClick(position, adapter.lastSelectedItemId)
        }
        return adapter
    }

    val MaterialAutoCompleteTextView.smartAdapter: SmartArrayAdapter<*>?
        get() {
            val adapter = adapter ?: return null
            return adapter as? SmartArrayAdapter<*>
        }

    inline fun <reified T: Any> MaterialAutoCompleteTextView.getLastSelected(): T? {
        val smartArrayAdapter = smartAdapter ?: return null
        val id = smartArrayAdapter.lastSelectedItemId as? T
        return id
    }

    /**
     * Sets the smart adapter position.
     * @return false if there are not that many items in adapter
     */
    fun MaterialAutoCompleteTextView.setPosition(position: Int): Boolean {
        require(position >= 0)
        val adapter = smartAdapter ?: return false
        if (position >= adapter.count) {
            return false
        }
        adapter.lastSelectedPosition = position
        setText(adapter.getItem(SmartArrayAdapter.LAST_SELECTED).toString())
        return true
    }

    /**
     * Finds the item in smart adapter and sets it's position to it.
     * @return false if item is not found
     */
    fun MaterialAutoCompleteTextView.setItem(item: Any?): Boolean {
        val adapter = smartAdapter ?: return false
        val index = adapter.indexOf(item)
        if (index < 0) {
            return false
        }
        return setPosition(index)
    }

    private val l = Injector.generateLogFunction("ConfigDropdownMagic")
}