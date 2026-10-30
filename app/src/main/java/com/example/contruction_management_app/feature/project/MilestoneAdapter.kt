package com.example.contruction_management_app.feature.project

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.ItemMilestoneBinding
import com.example.contruction_management_app.domain.model.Milestone

class MilestoneAdapter(
    private val onMilestoneClick: (Milestone) -> Unit
) : ListAdapter<Milestone, MilestoneAdapter.MilestoneViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MilestoneViewHolder {
        val binding = ItemMilestoneBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MilestoneViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MilestoneViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MilestoneViewHolder(
        private val binding: ItemMilestoneBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(milestone: Milestone) {
            val context = itemView.context
            binding.tvMilestoneTitle.text = milestone.title
            binding.tvMilestoneTargetPct.text = "Target: ${milestone.targetCompletionPct.toInt()}% Progress"
            binding.tvMilestoneDueDate.text = if (!milestone.dueDate.isNullOrBlank()) {
                "Due: ${milestone.dueDate}"
            } else {
                "No due date set"
            }

            val statusColorRes = when (milestone.status.uppercase()) {
                "REACHED", "COMPLETED" -> R.color.cms_success
                "DELAYED" -> R.color.cms_error
                else -> R.color.cms_info
            }
            val statusColor = ContextCompat.getColor(context, statusColorRes)
            binding.tvMilestoneStatus.text = milestone.status.uppercase()
            binding.tvMilestoneStatus.setTextColor(statusColor)

            binding.cardMilestone.setOnClickListener {
                onMilestoneClick(milestone)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Milestone>() {
        override fun areItemsTheSame(oldItem: Milestone, newItem: Milestone): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Milestone, newItem: Milestone): Boolean =
            oldItem == newItem
    }
}
