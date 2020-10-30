package cz.palda97.lpclient.view.pipelines

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.ListItemTwoLineBinding
import cz.palda97.lpclient.model.entities.pipelineview.PipelineView
import cz.palda97.lpclient.view.AdapterWithList

class PipelineRecyclerAdapter(
    private val editPipeline: (PipelineView) -> Unit,
    private val launchPipeline: (PipelineView) -> Unit
) : RecyclerView.Adapter<PipelineRecyclerAdapter.PipelineViewHolder>(),
    AdapterWithList<PipelineView> {
    private var pipelineList: List<PipelineView>? = null

    init {
        //setHasStableIds(true)
    }

    fun updatePipelineList(newPipelineList: List<PipelineView>) {
        if (pipelineList == null) {
            pipelineList = newPipelineList
            l("before notifyItemRangeInserted")
            notifyItemRangeInserted(0, newPipelineList.size)
            l("after notifyItemRangeInserted")
        } else {
            l("updatePipelineList start")
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
            l("updatePipelineList ends")
        }
    }

    class PipelineViewHolder(val binding: ListItemTwoLineBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PipelineViewHolder {
        val binding = DataBindingUtil
            .inflate<ListItemTwoLineBinding>(
                LayoutInflater.from(parent.context), R.layout.list_item_two_line,
                parent, false
            )
        return PipelineViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return pipelineList?.size ?: 0
    }

    override fun onBindViewHolder(holder: PipelineViewHolder, position: Int) {
        val pipelineView = pipelineList!![position]

        holder.binding.upperText = pipelineView.prefLabel
        holder.binding.bottomText = pipelineView.serverName
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

    companion object {
        private val TAG = Injector.tag(this)
        private fun l(msg: String) = Log.d(TAG, msg)
    }

    override fun getList(): List<PipelineView>? = pipelineList
    override val adapter: PipelineRecyclerAdapter = this
}