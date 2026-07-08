package com.mobile.passwordmanager

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mobile.passwordmanager.databinding.ItemEntryBinding

class EntriesAdapter(
    private val onClick: (Entry) -> Unit,
    private val onCopyPassword: (Entry) -> Unit,
    private val onDelete: (Entry) -> Unit
) : ListAdapter<Entry, EntriesAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemEntryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemEntryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            tvTitle.text = item.title.ifBlank { root.context.getString(R.string.untitled) }
            tvUsername.text = item.username.ifBlank { root.context.getString(R.string.no_username) }
            root.setOnClickListener { onClick(item) }
            btnCopy.setOnClickListener { onCopyPassword(item) }
            btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Entry>() {
            override fun areItemsTheSame(a: Entry, b: Entry) = a.id == b.id
            override fun areContentsTheSame(a: Entry, b: Entry) = a == b
        }
    }
}
