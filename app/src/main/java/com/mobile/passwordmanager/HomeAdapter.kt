package com.mobile.passwordmanager

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.iconics.IconicsDrawable
import com.mobile.passwordmanager.databinding.ItemEntryBinding
import com.mobile.passwordmanager.databinding.ItemGroupBinding
import com.mobile.passwordmanager.databinding.ItemSectionHeaderBinding

/**
 * 主页多类型适配器:分组列表在上,密码列表在下。
 * 分组项有独立的卡片背景和配色,与密码项视觉上区分开。
 */
sealed class HomeItem {
    data class GroupHeader(val text: String) : HomeItem()
    data class GroupRow(val group: Group, val count: Int) : HomeItem()
    data class EntryHeader(val text: String) : HomeItem()
    data class EntryRow(val entry: Entry) : HomeItem()
    data class EmptyHint(val text: String) : HomeItem()
}

class HomeAdapter(
    private val onGroupClick: (Group) -> Unit,
    private val onGroupDelete: (Group) -> Unit,
    private val onEntryClick: (Entry) -> Unit,
    private val onEntryCopy: (Entry) -> Unit,
    private val onEntryDelete: (Entry) -> Unit
) : ListAdapter<HomeItem, RecyclerView.ViewHolder>(DIFF) {

    companion object {
        private const val TYPE_GROUP_HEADER = 1
        private const val TYPE_GROUP = 2
        private const val TYPE_ENTRY_HEADER = 3
        private const val TYPE_ENTRY = 4
        private const val TYPE_EMPTY = 5

        private val DIFF = object : DiffUtil.ItemCallback<HomeItem>() {
            override fun areItemsTheSame(a: HomeItem, b: HomeItem): Boolean = when {
                a is HomeItem.GroupRow && b is HomeItem.GroupRow -> a.group.id == b.group.id
                a is HomeItem.EntryRow && b is HomeItem.EntryRow -> a.entry.id == b.entry.id
                a is HomeItem.GroupHeader && b is HomeItem.GroupHeader -> true
                a is HomeItem.EntryHeader && b is HomeItem.EntryHeader -> true
                a is HomeItem.EmptyHint && b is HomeItem.EmptyHint -> a.text == b.text
                else -> false
            }

            override fun areContentsTheSame(a: HomeItem, b: HomeItem): Boolean = a == b
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is HomeItem.GroupHeader -> TYPE_GROUP_HEADER
        is HomeItem.GroupRow -> TYPE_GROUP
        is HomeItem.EntryHeader -> TYPE_ENTRY_HEADER
        is HomeItem.EntryRow -> TYPE_ENTRY
        is HomeItem.EmptyHint -> TYPE_EMPTY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_GROUP_HEADER -> HeaderVH(
                ItemSectionHeaderBinding.inflate(inflater, parent, false)
            )
            TYPE_GROUP -> GroupVH(
                ItemGroupBinding.inflate(inflater, parent, false)
            )
            TYPE_ENTRY_HEADER -> HeaderVH(
                ItemSectionHeaderBinding.inflate(inflater, parent, false)
            )
            TYPE_ENTRY -> EntryVH(
                ItemEntryBinding.inflate(inflater, parent, false)
            )
            TYPE_EMPTY -> HeaderVH(
                ItemSectionHeaderBinding.inflate(inflater, parent, false)
            )
            else -> throw IllegalArgumentException("未知 viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is HomeItem.GroupHeader -> {
                (holder as HeaderVH).binding.tvHeader.text = item.text
            }
            is HomeItem.GroupRow -> {
                val h = holder as GroupVH
                val ctx = h.binding.root.context
                with(h.binding) {
                    tvGroupName.text = item.group.name.ifBlank { ctx.getString(R.string.untitled) }
                    tvGroupCount.text = ctx.getString(R.string.group_entry_count, item.count)
                    val iconEntry = IconCatalog.find(item.group.iconKey)
                    if (iconEntry != null) {
                        ivGroupIconBrand.setIcon(IconicsDrawable(ctx, iconEntry.icon))
                        ivGroupIconBrand.visibility = android.view.View.VISIBLE
                        ivGroupIconDefault.visibility = android.view.View.GONE
                    } else {
                        ivGroupIconBrand.visibility = android.view.View.GONE
                        ivGroupIconDefault.visibility = android.view.View.VISIBLE
                    }
                    root.setOnClickListener { onGroupClick(item.group) }
                    btnDeleteGroup.setOnClickListener { onGroupDelete(item.group) }
                }
            }
            is HomeItem.EntryHeader -> {
                (holder as HeaderVH).binding.tvHeader.text = item.text
            }
            is HomeItem.EntryRow -> {
                val h = holder as EntryVH
                val ctx = h.binding.root.context
                with(h.binding) {
                    tvTitle.text = item.entry.title.ifBlank { ctx.getString(R.string.untitled) }
                    tvUsername.text = item.entry.username.ifBlank { ctx.getString(R.string.no_username) }
                    val iconEntry = IconCatalog.find(item.entry.iconKey)
                    if (iconEntry != null) {
                        ivEntryIconBrand.setIcon(IconicsDrawable(ctx, iconEntry.icon))
                        ivEntryIconBrand.visibility = android.view.View.VISIBLE
                        ivEntryIconDefault.visibility = android.view.View.GONE
                    } else {
                        ivEntryIconBrand.visibility = android.view.View.GONE
                        ivEntryIconDefault.visibility = android.view.View.VISIBLE
                    }
                    root.setOnClickListener { onEntryClick(item.entry) }
                    btnCopy.setOnClickListener { onEntryCopy(item.entry) }
                    btnDelete.setOnClickListener { onEntryDelete(item.entry) }
                }
            }
            is HomeItem.EmptyHint -> {
                (holder as HeaderVH).binding.tvHeader.text = item.text
            }
        }
    }

    // --- ViewHolders ---

    class HeaderVH(val binding: ItemSectionHeaderBinding) : RecyclerView.ViewHolder(binding.root)
    class GroupVH(val binding: ItemGroupBinding) : RecyclerView.ViewHolder(binding.root)
    class EntryVH(val binding: ItemEntryBinding) : RecyclerView.ViewHolder(binding.root)
}
