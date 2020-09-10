package cz.palda97.lpclient.view

import androidx.recyclerview.widget.RecyclerView

interface AdapterWithList<item> {
    fun getList(): List<item>?
    val adapter: RecyclerView.Adapter<*>
}