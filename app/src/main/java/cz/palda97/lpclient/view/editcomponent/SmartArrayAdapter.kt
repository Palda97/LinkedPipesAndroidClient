package cz.palda97.lpclient.view.editcomponent

import android.content.Context
import android.widget.ArrayAdapter

class SmartArrayAdapter(context: Context, resource: Int) :
    ArrayAdapter<SmartArrayAdapter.PairWrapper>(context, resource) {

    class PairWrapper(val pair: Pair<String, String>) {
        override fun toString() = pair.second
    }

    var lastSelectedPosition: Int? = null

    fun setItems(list: List<Pair<String, String>>) {
        clear()
        addAll(
            list.map {
                PairWrapper(it)
            }
        )
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): PairWrapper? {
        if (position != LAST_SELECTED)
            return super.getItem(position)
        return lastSelectedPosition?.let {
            getItem(it)
        }
    }

    val lastSelectedItemId: String?
        get() = getItem(LAST_SELECTED)?.pair?.first

    companion object {
        const val LAST_SELECTED = -1
    }
}