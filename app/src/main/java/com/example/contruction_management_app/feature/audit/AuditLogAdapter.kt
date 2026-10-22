package com.example.contruction_management_app.feature.audit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.ItemAuditLogBinding
import com.example.contruction_management_app.domain.model.AuditAction
import com.example.contruction_management_app.domain.model.AuditLogItem

class AuditLogAdapter : ListAdapter<AuditLogItem, AuditLogAdapter.AuditViewHolder>(DiffCallback) {

    inner class AuditViewHolder(private val binding: ItemAuditLogBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AuditLogItem) {
            binding.tvAuditUser.text = item.userName
            binding.tvAuditTimestamp.text = "${item.createdAt} • IP: ${item.ipAddress ?: "127.0.0.1"}"
            binding.tvAuditActionBadge.text = item.action.displayName
            binding.tvAuditDetails.text = item.details ?: "No details logged."

            val initials = item.userName
                .split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .map { it.first().uppercaseChar() }
                .joinToString("")
            binding.tvUserInitials.text = if (initials.isNotEmpty()) initials else "U"

            val context = binding.root.context
            val badgeColor = when (item.action) {
                AuditAction.APPROVE -> ContextCompat.getColor(context, R.color.cms_success)
                AuditAction.ISSUE_STOCK -> ContextCompat.getColor(context, R.color.cms_accent)
                AuditAction.PAY_BILL -> ContextCompat.getColor(context, R.color.cms_info)
                AuditAction.REJECT -> ContextCompat.getColor(context, R.color.cms_error)
                else -> ContextCompat.getColor(context, R.color.cms_accent)
            }
            binding.tvAuditActionBadge.setTextColor(badgeColor)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuditViewHolder {
        val binding = ItemAuditLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AuditViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AuditViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<AuditLogItem>() {
        override fun areItemsTheSame(oldItem: AuditLogItem, newItem: AuditLogItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: AuditLogItem, newItem: AuditLogItem): Boolean = oldItem == newItem
    }
}
