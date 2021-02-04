package cz.palda97.lpclient.view.editcomponent

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.databinding.ConfigInputBinding
import cz.palda97.lpclient.model.entities.pipeline.ConfigInput
import cz.palda97.lpclient.view.AdapterWithList
import cz.palda97.lpclient.view.ConfigDropdownMagic.fillWithOptions
import cz.palda97.lpclient.viewmodel.editcomponent.ConfigInputComplete

class ConfigInputAdapter(
    private val context: Context,
    private val configGetString: (String) -> String?,
    private val configSetString: (String, String) -> Unit
) : RecyclerView.Adapter<ConfigInputAdapter.ConfigInputViewHolder>(),
    AdapterWithList<ConfigInput> {

    private var configInputList: List<ConfigInput> = emptyList()
    private var configInputComplete: ConfigInputComplete? = null

    init {
        //setHasStableIds(true)
    }

    private fun positionToString(position: Int): String? {
        val dialogJs = configInputComplete?.dialogJs ?: return null
        val item = configInputList[position]
        val translated = dialogJs.getFullPropertyName(item.id) ?: item.id
        return configGetString(translated) ?: ""
    }

    fun updateConfigInputList(newConfigInputComplete: ConfigInputComplete) {
        configInputComplete = newConfigInputComplete
        val newConfigInputList = newConfigInputComplete.configInputs

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
                    //return oldItem == newItem
                    return oldItem.id == newItem.id && oldItem.componentId == newItem.componentId
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    //return areItemsTheSame(oldItemPosition, newItemPosition)
                    //return false
                    val oldString = positionToString(oldItemPosition) ?: return false
                    val newString = positionToString(newItemPosition) ?: return false
                    return oldString == newString
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
        val dialogJs = configInputComplete?.dialogJs ?: return
        holder.binding.configInput = configInput
        val translated = dialogJs.getFullPropertyName(configInput.id) ?: configInput.id
        val string = configGetString(translated) ?: ""
        //l("onBindViewHolder configGetString: $string")
        when(configInput.type) {
            ConfigInput.Type.EDIT_TEXT -> {
                holder.binding.editText.setText(string)
                holder.binding.editText.doOnTextChanged { text, _, _, _ ->
                    val newValue: String = text?.toString() ?: ""
                    configSetString(translated, newValue)
                }
            }
            ConfigInput.Type.SWITCH -> {
                holder.binding.switchMaterial.isChecked = string.toBoolean()
                holder.binding.switchMaterial.setOnCheckedChangeListener { _, isChecked ->
                    configSetString(translated, isChecked.toString())
                }
            }
            ConfigInput.Type.DROPDOWN -> {
                holder.binding.dropdown.fillWithOptions(context, configInput.options)
                //TODO()
            }
            ConfigInput.Type.TEXT_AREA -> {
                holder.binding.textArea.setText(string)
                holder.binding.textArea.doOnTextChanged { text, _, _, _ ->
                    val newValue: String = text?.toString() ?: ""
                    configSetString(translated, newValue)
                }
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