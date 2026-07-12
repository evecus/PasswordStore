package com.mobile.passwordmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.iconics.IconicsDrawable

/**
 * 图标选择弹窗的网格适配器。[selectedKey] 为空表示"不使用品牌图标,保留默认图标"。
 */
class IconPickerAdapter(
    private val onSelect: (IconCatalog.IconEntry?) -> Unit
) : RecyclerView.Adapter<IconPickerAdapter.VH>() {

    private var items: List<IconCatalog.IconEntry> = IconCatalog.all
    private var selectedKey: String? = null

    fun submit(newItems: List<IconCatalog.IconEntry>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun setSelected(key: String?) {
        selectedKey = key
        notifyDataSetChanged()
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: com.mikepenz.iconics.view.IconicsImageView = view.findViewById(R.id.ivIcon)
        val tvLabel: android.widget.TextView = view.findViewById(R.id.tvIconLabel)
        val selectedRing: View = view.findViewById(R.id.selectedRing)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_icon_option, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val entry = items[position]
        holder.ivIcon.setIcon(IconicsDrawable(holder.itemView.context, entry.icon))
        holder.tvLabel.text = entry.label
        holder.selectedRing.visibility = if (entry.key == selectedKey) View.VISIBLE else View.GONE
        holder.itemView.setOnClickListener { onSelect(entry) }
    }

    override fun getItemCount(): Int = items.size
}
