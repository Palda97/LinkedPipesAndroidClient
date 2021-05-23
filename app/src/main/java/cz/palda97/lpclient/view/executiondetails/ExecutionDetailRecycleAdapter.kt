package cz.palda97.lpclient.view.executiondetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.ListItemOneLineMetaBinding
import cz.palda97.lpclient.view.AdapterWithList
import cz.palda97.lpclient.viewmodel.executiondetails.ExecutionDetailComponentV

/**
 * Adapter for list of execution components.
 */
class ExecutionDetailRecycleAdapter(
) : RecyclerView.Adapter<ExecutionDetailRecycleAdapter.ExecutionDetailViewHolder>(),
    AdapterWithList<ExecutionDetailComponentV> {
    private var componentList: List<ExecutionDetailComponentV>? = null

    init {
        //setHasStableIds(true)
    }

    /**
     * Update the content of this adapter.
     */
    fun updateComponentList(newComponentList: List<ExecutionDetailComponentV>) {
        if (componentList == null) {
            componentList = newComponentList
            notifyItemRangeInserted(0, newComponentList.size)
        } else {
            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return componentList!!.size
                }

                override fun getNewListSize(): Int {
                    return newComponentList.size
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    //return serverList!![oldItemPosition].url == newServerList[newItemPosition].url
                    val oldItem = componentList!![oldItemPosition]
                    val newItem = newComponentList[newItemPosition]
                    return oldItem == newItem
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    val newItem = newComponentList[newItemPosition]
                    val oldItem = componentList!![oldItemPosition]
                    return newItem.status == oldItem.status
                            && newItem.label == oldItem.label
                }
            })
            componentList = newComponentList
            result.dispatchUpdatesTo(this)
        }
    }

    class ExecutionDetailViewHolder(val binding: ListItemOneLineMetaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExecutionDetailViewHolder {
        val binding = DataBindingUtil
            .inflate<ListItemOneLineMetaBinding>(
                LayoutInflater.from(parent.context), R.layout.list_item_one_line_meta,
                parent, false
            )
        return ExecutionDetailViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return componentList?.size ?: 0
    }

    override fun onBindViewHolder(holder: ExecutionDetailViewHolder, position: Int) {
        val component = componentList!![position]

        holder.binding.icon = component.status
        holder.binding.mainText = component.label
        holder.binding.executePendingBindings()
    }

    /*override fun getItemId(position: Int): Long {
        return serverList?.get(position)?.id ?: -1
    }*/

    companion object {
        private val l = Injector.generateLogFunction(this)
    }

    override fun getList(): List<ExecutionDetailComponentV>? = componentList
    override val adapter = this
}