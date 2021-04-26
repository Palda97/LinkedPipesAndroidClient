package cz.palda97.lpclient.view.editcomponent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.ListItemTwoLineBinding
import cz.palda97.lpclient.model.entities.pipeline.Connection
import cz.palda97.lpclient.view.AdapterWithList
import cz.palda97.lpclient.viewmodel.editcomponent.ConnectionV

/**
 * Adapter for list of connection.
 */
class ConnectionAdapter : RecyclerView.Adapter<ConnectionAdapter.ConnectionViewHolder>(),
    AdapterWithList<Pair<Connection, ConnectionV.ConnectionItem>> {

    private var connectionList: List<Pair<Connection, ConnectionV.ConnectionItem>> = emptyList()

    init {
        //setHasStableIds(true)
    }

    /**
     * Update the content of this adapter.
     */
    fun updateConnectionList(newConnectionList: List<Pair<Connection, ConnectionV.ConnectionItem>>) {
        if (connectionList.isEmpty()) {
            connectionList = newConnectionList
            l("before notifyItemRangeInserted")
            notifyItemRangeInserted(0, newConnectionList.size)
            l("after notifyItemRangeInserted")
        } else {
            l("updateConfigInputList start")
            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return connectionList.size
                }

                override fun getNewListSize(): Int {
                    return newConnectionList.size
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    val oldItem = connectionList[oldItemPosition]
                    val newItem = newConnectionList[newItemPosition]
                    return oldItem.first.id == newItem.first.id
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    val oldItem = connectionList[oldItemPosition]
                    val newItem = newConnectionList[newItemPosition]
                    return oldItem.second == newItem.second
                }
            })
            connectionList = newConnectionList
            result.dispatchUpdatesTo(this)
            l("updateConfigInputList ends")
        }
    }

    class ConnectionViewHolder(val binding: ListItemTwoLineBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConnectionViewHolder {
        val binding = DataBindingUtil.inflate<ListItemTwoLineBinding>(
            LayoutInflater.from(parent.context), R.layout.list_item_two_line, parent, false
        )
        return ConnectionViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return connectionList.size
    }

    override fun onBindViewHolder(holder: ConnectionViewHolder, position: Int) {
        val item = connectionList[position].second
        holder.binding.upperText = item.component
        holder.binding.bottomText = "${item.source} -> ${item.target}"
        holder.binding.executePendingBindings()
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
    }

    //AdapterWithList overrides
    override fun getList(): List<Pair<Connection, ConnectionV.ConnectionItem>>? = connectionList
    override val adapter: ConnectionAdapter = this
}