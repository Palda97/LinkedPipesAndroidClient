package cz.palda97.lpclient.view.editcomponent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.ListItemSwitchBinding
import cz.palda97.lpclient.view.AdapterWithList
import cz.palda97.lpclient.viewmodel.editcomponent.InheritanceV
import cz.palda97.lpclient.viewmodel.editcomponent.InheritanceVWrapper

class InheritanceAdapter(
    private val setInheritance: (fullName: String, value: Boolean, configType: String) -> Unit?
) : RecyclerView.Adapter<InheritanceAdapter.InheritanceViewHolder>(),
    AdapterWithList<InheritanceV> {

    private var inheritanceVList: List<InheritanceV> = emptyList()
    private var inheritanceVWrapper: InheritanceVWrapper? = null

    init {
        //setHasStableIds(true)
    }

    fun updateInheritanceVWrapper(newInheritanceVWrapper: InheritanceVWrapper) {
        inheritanceVWrapper = newInheritanceVWrapper
        val newInheritanceVList = newInheritanceVWrapper.inheritances

        if (inheritanceVList.isEmpty()) {
            inheritanceVList = newInheritanceVList
            l("before notifyItemRangeInserted")
            notifyItemRangeInserted(0, newInheritanceVList.size)
            l("after notifyItemRangeInserted")
        } else {
            l("updateInheritanceVWrapper start")
            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return inheritanceVList.size
                }

                override fun getNewListSize(): Int {
                    return newInheritanceVList.size
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    val oldItem = inheritanceVList[oldItemPosition]
                    val newItem = newInheritanceVList[newItemPosition]
                    return oldItem == newItem
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    return areItemsTheSame(oldItemPosition, newItemPosition)
                }
            })
            inheritanceVList = newInheritanceVList
            result.dispatchUpdatesTo(this)
            l("updateInheritanceVWrapper ends")
        }
    }

    class InheritanceViewHolder(val binding: ListItemSwitchBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InheritanceViewHolder {
        val binding = DataBindingUtil.inflate<ListItemSwitchBinding>(
            LayoutInflater.from(parent.context), R.layout.list_item_switch, parent, false
        )
        return InheritanceViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return inheritanceVList.size
    }

    override fun onBindViewHolder(holder: InheritanceViewHolder, position: Int) {
        val inheritanceV = inheritanceVList[position]
        holder.binding.text = inheritanceV.label
        holder.binding.checked = inheritanceV.inherit
        holder.binding.mSwitch.setOnCheckedChangeListener { _, isChecked ->
            setInheritance(inheritanceV.id, isChecked, inheritanceVWrapper!!.configType)
        }
        holder.binding.executePendingBindings()
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
    }

    //AdapterWithList overrides
    override fun getList(): List<InheritanceV>? = inheritanceVList
    override val adapter: InheritanceAdapter = this
}