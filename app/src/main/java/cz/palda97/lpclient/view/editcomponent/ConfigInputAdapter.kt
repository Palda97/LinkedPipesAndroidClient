package cz.palda97.lpclient.view.editcomponent

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.ConfigInputBinding
import cz.palda97.lpclient.model.entities.pipeline.ConfigInput
import cz.palda97.lpclient.model.entities.pipeline.Configuration
import cz.palda97.lpclient.model.entities.pipeline.DialogJs
import cz.palda97.lpclient.view.AdapterWithList
import cz.palda97.lpclient.view.editcomponent.ConfigDropdownMagic.fillWithOptions

class ConfigInputAdapter(
    private val context: Context,
    private val dialogJs: DialogJs,
    private val configuration: Configuration
) : RecyclerView.Adapter<ConfigInputAdapter.ConfigInputViewHolder>(),
    AdapterWithList<ConfigInput> {

    private var configInputList: List<ConfigInput> = emptyList()

    init {
        //setHasStableIds(true)
    }

    fun updateConfigInputList(newConfigInputList: List<ConfigInput>) {
        if (configInputList.isEmpty()) {
            configInputList = newConfigInputList
            l("before notifyItemRangeInserted")
            notifyItemRangeInserted(0, newConfigInputList.size)
            l("after notifyItemRangeInserted")
        } else {
            l("updateConfigInputList start")
            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return configInputList.size
                }

                override fun getNewListSize(): Int {
                    return newConfigInputList.size
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    val oldItem = configInputList[oldItemPosition]
                    val newItem = newConfigInputList[newItemPosition]
                    return oldItem == newItem
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    return areItemsTheSame(oldItemPosition, newItemPosition)
                }
            })
            configInputList = newConfigInputList
            result.dispatchUpdatesTo(this)
            l("updateConfigInputList ends")
        }
    }

    class ConfigInputViewHolder(val binding: ConfigInputBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConfigInputViewHolder {
        val binding = DataBindingUtil.inflate<ConfigInputBinding>(
            LayoutInflater.from(parent.context), R.layout.config_input, parent, false
        )
        return ConfigInputViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return configInputList.size
    }

    override fun onBindViewHolder(holder: ConfigInputViewHolder, position: Int) {
        val configInput = configInputList[position]
        holder.binding.configInput = configInput
        val translated = dialogJs.getFullPropertyName(configInput.id) ?: configInput.id
        val string = configuration.getString(translated) ?: ""
        when(configInput.type) {
            ConfigInput.Type.EDIT_TEXT -> holder.binding.editText.setText(string)
            ConfigInput.Type.SWITCH -> holder.binding.switchMaterial.isChecked = string.toBoolean()
            ConfigInput.Type.DROPDOWN -> {
                holder.binding.dropdown.fillWithOptions(context, configInput.options)
            }
            ConfigInput.Type.TEXT_AREA -> {
                holder.binding.textArea.setText(string)
            }
        }
        holder.binding.executePendingBindings()
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
    }

    //AdapterWithList overrides
    override fun getList(): List<ConfigInput>? = configInputList
    override val adapter: ConfigInputAdapter = this
}