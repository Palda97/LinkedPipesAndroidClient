package cz.palda97.lpclient.view.editcomponent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.BindingButtonBinding
import cz.palda97.lpclient.model.entities.pipeline.Binding
import cz.palda97.lpclient.view.AdapterWithList

/**
 * Adapter for list of binding buttons.
 * @property addConnection Function for creating a connection connected to this binding.
 */
class BindingAdapter(
    private val addConnection: (Binding) -> Unit
) : RecyclerView.Adapter<BindingAdapter.BindingViewHolder>(),
    AdapterWithList<Binding> {

    private var bindingList: List<Binding> = emptyList()

    init {
        //setHasStableIds(true)
    }

    /**
     * Update the content of this adapter.
     */
    fun updateBindingList(newBindingList: List<Binding>) {
        if (bindingList.isEmpty()) {
            bindingList = newBindingList
            l("before notifyItemRangeInserted")
            notifyItemRangeInserted(0, newBindingList.size)
            l("after notifyItemRangeInserted")
        } else {
            l("updateConfigInputList start")
            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return bindingList.size
                }

                override fun getNewListSize(): Int {
                    return newBindingList.size
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    val oldItem = bindingList[oldItemPosition]
                    val newItem = newBindingList[newItemPosition]
                    //return oldItem == newItem
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    val oldItem = bindingList[oldItemPosition]
                    val newItem = newBindingList[newItemPosition]
                    return oldItem.prefLabel == newItem.prefLabel
                }
            })
            bindingList = newBindingList
            result.dispatchUpdatesTo(this)
            l("updateConfigInputList ends")
        }
    }

    class BindingViewHolder(val binding: BindingButtonBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
        val binding = DataBindingUtil.inflate<BindingButtonBinding>(
            LayoutInflater.from(parent.context), R.layout.binding_button, parent, false
        )
        return BindingViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return bindingList.size
    }

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        val binding = bindingList[position]
        holder.binding.binding = binding
        holder.binding.button.setOnClickListener {
            addConnection(bindingList[holder.adapterPosition])
        }
        holder.binding.executePendingBindings()
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
    }

    //AdapterWithList overrides
    override fun getList(): List<Binding>? = bindingList
    override val adapter: BindingAdapter = this
}