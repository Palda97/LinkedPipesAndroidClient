package cz.palda97.lpclient.view

import androidx.recyclerview.widget.RecyclerView

/**
 * Interface for adapter and it's content.
 * It is useful in [RecyclerViewCosmetics] for swipe to delete function.
 */
interface AdapterWithList<item> {
    fun getList(): List<item>?
    val adapter: RecyclerView.Adapter<*>
}