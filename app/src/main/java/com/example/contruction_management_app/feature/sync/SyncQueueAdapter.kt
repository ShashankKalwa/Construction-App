package com.example.contruction_management_app.feature.sync

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.ItemSyncQueueBinding
import com.example.contruction_management_app.domain.model.SyncQueueItem
import com.example.contruction_management_app.domain.model.SyncStatus

class SyncQueueAdapter : ListAdapter<SyncQueueItem, SyncQueueAdapter.SyncViewHolder>(DiffCallback) {

    inner class SyncViewHolder(private val binding: ItemSyncQueueBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SyncQueueItem) {
            binding.tvSyncTitle.text = item.title
            binding.tvSyncSubtitle.text = item.subtitle
            binding.tvSyncStatusBadge.text = item.status.displayName

            val context = binding.root.context
            val color = when (item.status) {
                SyncStatus.CONFIRMED -> ContextCompat.getColor(context, R.color.cms_success)
                SyncStatus.PENDING_SYNC -> ContextCompat.getColor(context, R.color.cms_warning)
                SyncStatus.FAILED -> ContextCompat.getColor(context, R.color.cms_error)
                SyncStatus.SYNCING -> ContextCompat.getColor(context, R.color.cms_accent)
            }
            binding.tvSyncStatusBadge.setTextColor(color)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SyncViewHolder {
        val binding = ItemSyncQueueBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SyncViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SyncViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SyncQueueItem>() {
        override fun areItemsTheSame(oldItem: SyncQueueItem, newItem: SyncQueueItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: SyncQueueItem, newItem: SyncQueueItem): Boolean = oldItem == newItem
    }
}
