package com.example.contruction_management_app.feature.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.ItemNotificationBinding
import com.example.contruction_management_app.domain.model.NotificationItem
import com.example.contruction_management_app.domain.model.NotificationType

class NotificationAdapter(
    private val onItemClick: (NotificationItem) -> Unit
) : ListAdapter<NotificationItem, NotificationAdapter.NotificationViewHolder>(DiffCallback) {

    inner class NotificationViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NotificationItem) {
            binding.tvNotifTitle.text = item.title
            binding.tvNotifMessage.text = item.message
            binding.tvNotifTime.text = item.createdAt
            binding.viewUnreadDot.visibility = if (item.isRead) View.GONE else View.VISIBLE

            val context = binding.root.context
            val tintColor = when (item.type) {
                NotificationType.LOW_STOCK -> ContextCompat.getColor(context, R.color.cms_warning)
                NotificationType.SAFETY_ALERT -> ContextCompat.getColor(context, R.color.cms_error)
                NotificationType.DOCUMENT_UPLOADED -> ContextCompat.getColor(context, R.color.cms_info)
                else -> ContextCompat.getColor(context, R.color.cms_accent)
            }
            binding.ivNotifIcon.setColorFilter(tintColor)

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<NotificationItem>() {
        override fun areItemsTheSame(oldItem: NotificationItem, newItem: NotificationItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: NotificationItem, newItem: NotificationItem): Boolean = oldItem == newItem
    }
}
