package cz.palda97.lpclient.view

import android.content.Context
import android.widget.ArrayAdapter

class SmartArrayAdapter<T>(context: Context, resource: Int) :
    ArrayAdapter<SmartArrayAdapter.PairWrapper<T>>(context, resource) {

    class PairWrapper<T>(val pair: Pair<T, String>) {
        override fun toString() = pair.second
    }

    var lastSelectedPosition: Int? = null

    fun setItems(list: List<Pair<T, String>>) {
        lastSelectedPosition = null
        clear()
        addAll(
            list.map {
                PairWrapper(it)
            }
        )
        notifyDataSetChanged()
    }

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