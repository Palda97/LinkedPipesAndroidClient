package cz.palda97.lpclient.view.pipelines

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.ListItemServersBinding
import cz.palda97.lpclient.databinding.ListItemTwoLineMetaBinding
import cz.palda97.lpclient.model.ServerInstance
import cz.palda97.lpclient.viewmodel.pipelines.PipelineView

class PipelineRecyclerAdapter(
    private val editPipeline: (PipelineView) -> Unit,
    private val launchPipeline: (PipelineView) -> Unit
) : RecyclerView.Adapter<PipelineRecyclerAdapter.PipelineViewHolder>() {
    private var pipelineList: List<PipelineView>? = null
    fun getPipelineList() = pipelineList

    init {
        //setHasStableIds(true)
    }

    fun updatePipelineList(newPipelineList: List<PipelineView>) {
        if (pipelineList == null) {
            pipelineList = newPipelineList
            notifyItemRangeInserted(0, newPipelineList.size)
        } else {
            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return pipelineList!!.size
                }

                override fun getNewListSize(): Int {
                    return newPipelineList.size
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    //return serverList!![oldItemPosition].url == newServerList[newItemPosition].url
                    val oldItem = pipelineList!![oldItemPosition]
                    val newItem = newPipelineList[newItemPosition]
                    return oldItem == newItem
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    /*val newItem = newPipelineList[newItemPosition]
                    val oldItem = pipelineList!![oldItemPosition]*/
                    return areItemsTheSame(oldItemPosition, newItemPosition)
                }
            })
            pipelineList = newPipelineList
            result.dispatchUpdatesTo(this)
        }
    }

    class PipelineViewHolder(val binding: ListItemTwoLineMetaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PipelineViewHolder {
        val binding = DataBindingUtil
            .inflate<ListItemTwoLineMetaBinding>(
                LayoutInflater.from(parent.context), R.layout.list_item_two_line_meta,
                parent, false
            )
        return PipelineViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return pipelineList?.size ?: 0
    }

    override fun onBindViewHolder(holder: PipelineViewHolder, position: Int) {
        val pipelineView = pipelineList!![position]

        holder.binding.pipeline = pipelineView
        holder.binding.executePendingBindings()

        holder.itemView.setOnClickListener {
            editPipeline(pipelineView)
        }

        holder.itemView.setOnLongClickListener {
            launchPipeline(pipelineView)
            true
        }
    }

    /*override fun getItemId(position: Int): Long {
        return serverList?.get(position)?.id ?: -1
    }*/
}