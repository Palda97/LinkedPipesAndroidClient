package cz.palda97.lpclient.view

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter

/**
 * Adapter that contains items of certain type and their text representation.
 * @param T The type of items stored in this adapter.
 * @param resource Dropdown item resource id.
 * @param shouldFilter If the dropdown should filter suggestions while typing in.
 */
class SmartArrayAdapter<T>(
    context: Context,
    resource: Int,
    shouldFilter: Boolean = true
) : ArrayAdapter<SmartArrayAdapter.PairWrapper<T>>(context, resource) {

    /**
     * Wrapper for T and it's text representation. Method toString() returns the text representation.
     */
    class PairWrapper<T>(val pair: Pair<T, String>) {
        override fun toString() = pair.second
    }

    /**
     * Position of item that was selected last. Can be null if no item has been selected.
     * Has a modified setter, that ignores values smaller than zero.
     */
    var lastSelectedPosition: Int? = null
        set(value) {
            if (value != null && value < 0)
                return
            field = value
        }

    /**
     * Items stored in this adapter.
     */
    var items: List<Pair<T, String>> = emptyList()
        set(value) {
            field = value
            lastSelectedPosition = null
            clear()
            addAll(
                value.map {
                    PairWrapper(it)
                }
            )
            notifyDataSetChanged()
        }

    /**
     * Get index of this item.
     * @return Item index or -1 if not found.
     */
    fun indexOf(item: Any?) = items.map { it.first }.indexOf(item)

    /**
     * Get item stored on this position.
     * @param position Position of wanted item or [LAST_SELECTED] if you want item that was selected last.
     * @return [PairWrapper] of the item or null if adapter doesn't contain this item.
     */
    override fun getItem(position: Int): PairWrapper<T>? {
        if (position != LAST_SELECTED)
            return super.getItem(position)
        return lastSelectedPosition?.let {
            getItem(it)
        }
    }

    /**
     * Last selected item or null if no item has been selected.
     */
    val lastSelectedItemId: T?
        get() = getItem(LAST_SELECTED)?.pair?.first

    private fun filterOff() = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults =
            FilterResults().apply {
                values = items
                count = items.size
            }
        override fun publishResults(
            constraint: CharSequence?,
            results: FilterResults?
        ) {
            notifyDataSetChanged()
        }
    }
    private fun filterOn() = super.getFilter()
    private val correctFilterFun: () -> Filter = if (shouldFilter) ::filterOn else ::filterOff
    override fun getFilter(): Filter = correctFilterFun()

    companion object {

        /**
         * Position of last selected item. Useful in [getItem].
         */
        const val LAST_SELECTED = -1
    }
}