package cz.palda97.lpclient.view

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter

class SmartArrayAdapter<T>(
    context: Context,
    resource: Int,
    shouldFilter: Boolean = true
) : ArrayAdapter<SmartArrayAdapter.PairWrapper<T>>(context, resource) {

    class PairWrapper<T>(val pair: Pair<T, String>) {
        override fun toString() = pair.second
    }

    var lastSelectedPosition: Int? = null
        set(value) {
            if (value != null && value < 0)
                return
            field = value
        }

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

    fun indexOf(item: Any?) = items.map { it.first }.indexOf(item)

    override fun getItem(position: Int): PairWrapper<T>? {
        if (position != LAST_SELECTED)
            return super.getItem(position)
        return lastSelectedPosition?.let {
            getItem(it)
        }
    }

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
        const val LAST_SELECTED = -1
    }
}