package cz.palda97.lpclient.view.executions

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.ListItemTwoLineMetaBinding
import cz.palda97.lpclient.model.DateParser
import cz.palda97.lpclient.model.Execution
import cz.palda97.lpclient.view.AdapterWithList
import cz.palda97.lpclient.viewmodel.executions.ExecutionV

class ExecutionRecycleAdapter(
    private val viewExecution: (ExecutionV) -> Unit,
    private val launchExecution: (ExecutionV) -> Unit
) : RecyclerView.Adapter<ExecutionRecycleAdapter.ExecutionViewHolder>(),
    AdapterWithList<ExecutionV> {
    private var executionList: List<ExecutionV>? = null

    init {
        //setHasStableIds(true)
    }

    fun updateExecutionList(newExecutionList: List<ExecutionV>) {
        if (executionList == null) {
            executionList = newExecutionList
            notifyItemRangeInserted(0, newExecutionList.size)
        } else {
            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return executionList!!.size
                }

                override fun getNewListSize(): Int {
                    return newExecutionList.size
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    //return serverList!![oldItemPosition].url == newServerList[newItemPosition].url
                    val oldItem = executionList!![oldItemPosition]
                    val newItem = newExecutionList[newItemPosition]
                    return oldItem == newItem
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    val newItem = newExecutionList[newItemPosition]
                    val oldItem = executionList!![oldItemPosition]
                    return newItem.start == oldItem.start &&
                            newItem.pipelineName == oldItem.pipelineName
                            && newItem.serverName == oldItem.serverName
                            && newItem.status == oldItem.status
                }
            })
            executionList = newExecutionList
            result.dispatchUpdatesTo(this)
        }
    }

    class ExecutionViewHolder(val binding: ListItemTwoLineMetaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExecutionViewHolder {
        val binding = DataBindingUtil
            .inflate<ListItemTwoLineMetaBinding>(
                LayoutInflater.from(parent.context), R.layout.list_item_two_line_meta,
                parent, false
            )
        return ExecutionViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return executionList?.size ?: 0
    }

    override fun onBindViewHolder(holder: ExecutionViewHolder, position: Int) {
        val execution = executionList!![position]

        holder.binding.upperText = execution.pipelineName
        holder.binding.bottomText = execution.serverName
        holder.binding.metaText = execution.start
        holder.binding.executePendingBindings()

        holder.itemView.setOnClickListener {
            viewExecution(executionList!![holder.adapterPosition])
        }

        holder.itemView.setOnLongClickListener {
            launchExecution(executionList!![holder.adapterPosition])
            true
        }
    }

    /*override fun getItemId(position: Int): Long {
        return serverList?.get(position)?.id ?: -1
    }*/

    companion object {
        private val TAG = Injector.tag(this)
        private fun l(msg: String) = Log.d(TAG, msg)
    }

    override fun getList(): List<ExecutionV>? = executionList
    override val adapter = this
}