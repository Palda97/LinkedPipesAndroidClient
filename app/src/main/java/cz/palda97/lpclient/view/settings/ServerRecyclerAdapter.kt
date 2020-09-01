package cz.palda97.lpclient.view.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.ListItemServersBinding
import cz.palda97.lpclient.model.ServerInstance

class ServerRecyclerAdapter(private val editServer: (ServerInstance) -> Unit, private val deleteServer: (ServerInstance) -> Unit): RecyclerView.Adapter<ServerRecyclerAdapter.ServerViewHolder>() {
    var serverList: List<ServerInstance>? = null
    init {
        //setHasStableIds(true)
    }
    fun updateServerList(newServerList: List<ServerInstance>){
        if (serverList == null) {
            serverList = newServerList
            notifyItemRangeInserted(0, newServerList.size)
        } else {
            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return serverList!!.size
                }

                override fun getNewListSize(): Int {
                    return newServerList.size
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    //return serverList!![oldItemPosition].url == newServerList[newItemPosition].url
                    val oldItem = serverList!![oldItemPosition]
                    val newItem = newServerList[newItemPosition]
                    return oldItem == newItem
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    val newItem = newServerList[newItemPosition]
                    val oldItem = serverList!![oldItemPosition]
                    return newItem.url == oldItem.url && newItem.name == oldItem.name
                }
            })
            serverList = newServerList
            result.dispatchUpdatesTo(this)
        }
    }

    class ServerViewHolder(val binding: ListItemServersBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerViewHolder {
        val binding = DataBindingUtil
            .inflate<ListItemServersBinding>(
                LayoutInflater.from(parent.context), R.layout.list_item_servers,
                parent, false)
        return ServerViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return serverList?.size ?: 0
    }

    override fun onBindViewHolder(holder: ServerViewHolder, position: Int) {
        val serverInstance = serverList!![position]

        holder.binding.server = serverInstance
        holder.binding.executePendingBindings()

        holder.itemView.setOnClickListener {
            editServer(serverInstance)
        }
    }

    /*override fun getItemId(position: Int): Long {
        return serverList?.get(position)?.id ?: -1
    }*/
}