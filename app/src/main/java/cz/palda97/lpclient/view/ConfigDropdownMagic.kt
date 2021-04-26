package cz.palda97.lpclient.view

import android.content.Context
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R

/**
 * Class for making dropdown's adapter remember the items inside, not only String representation
 * and working with it.
 */
object ConfigDropdownMagic {

    /**
     * Setup the dropdown's adapter with items.
     * @param options List of items paired with their text representation.
     * @param shouldFilter If the dropdown should filter suggestions while typing in.
     * @param onItemClick Function that will be called when an item is clicked.
     * @return [SmartArrayAdapter] belonging to this dropdown.
     */
    fun <T> MaterialAutoCompleteTextView.fillWithOptions(
        context: Context,
        options: List<Pair<T, String>>? = null,
        shouldFilter: Boolean = true,
        onItemClick: (position: Int, item: T?) -> Unit = {_, _ -> }
    ): SmartArrayAdapter<T> {
        val adapter = SmartArrayAdapter<T>(
            context,
            R.layout.dropdown_item_text_view,
            shouldFilter
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

    /**
     * Gets a [SmartArrayAdapter] of this dropdown or null if this dropdown is not setup with it.
     */
    val MaterialAutoCompleteTextView.smartAdapter: SmartArrayAdapter<*>?
        get() {
            val adapter = adapter ?: return null
            return adapter as? SmartArrayAdapter<*>
        }

    /**
     * Gets the last selected item.
     * @return Item that was selected last or null if no item has been selected.
     */
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