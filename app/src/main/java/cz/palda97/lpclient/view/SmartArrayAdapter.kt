package cz.palda97.lpclient.view

import android.content.Context
import android.widget.ArrayAdapter

class SmartArrayAdapter<T>(context: Context, resource: Int) :
    ArrayAdapter<SmartArrayAdapter.PairWrapper<T>>(context, resource) {

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

    companion object {
        const val LAST_SELECTED = -1
    }
}