package com.example.contruction_management_app.feature.task

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.ItemPhaseBinding
import com.example.contruction_management_app.databinding.ItemTaskBinding
import com.example.contruction_management_app.domain.model.Phase
import com.example.contruction_management_app.domain.model.Task
import com.example.contruction_management_app.domain.model.TaskStatus

sealed class TaskListItem {
    data class PhaseHeader(val phase: Phase) : TaskListItem()
    data class TaskRow(val task: Task) : TaskListItem()
}

class TaskAdapter(
    private val onTaskClick: (Task) -> Unit
) : ListAdapter<TaskListItem, RecyclerView.ViewHolder>(DiffCallback) {

    companion object {
        private const val TYPE_PHASE = 0
        private const val TYPE_TASK = 1

        private object DiffCallback : DiffUtil.ItemCallback<TaskListItem>() {
            override fun areItemsTheSame(oldItem: TaskListItem, newItem: TaskListItem): Boolean {
                return when {
                    oldItem is TaskListItem.PhaseHeader && newItem is TaskListItem.PhaseHeader ->
                        oldItem.phase.id == newItem.phase.id
                    oldItem is TaskListItem.TaskRow && newItem is TaskListItem.TaskRow ->
                        oldItem.task.id == newItem.task.id
                    else -> false
                }
            }

            override fun areContentsTheSame(oldItem: TaskListItem, newItem: TaskListItem): Boolean =
                oldItem == newItem
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TaskListItem.PhaseHeader -> TYPE_PHASE
            is TaskListItem.TaskRow -> TYPE_TASK
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_PHASE -> {
                val binding = ItemPhaseBinding.inflate(inflater, parent, false)
                PhaseViewHolder(binding)
            }
            else -> {
                val binding = ItemTaskBinding.inflate(inflater, parent, false)
                TaskViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is TaskListItem.PhaseHeader -> (holder as PhaseViewHolder).bind(item.phase)
            is TaskListItem.TaskRow -> (holder as TaskViewHolder).bind(item.task)
        }
    }

    inner class PhaseViewHolder(
        private val binding: ItemPhaseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(phase: Phase) {
            binding.tvPhaseOrder.text = phase.orderIndex.toString()
            binding.tvPhaseName.text = "Phase ${phase.orderIndex}: ${phase.name}"
            val progressInt = phase.progressPct.toInt().coerceIn(0, 100)
            binding.progressPhase.progress = progressInt
            binding.tvPhaseProgressPct.text = "$progressInt%"
        }
    }

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            val context = itemView.context
            val taskCode = "TSK-" + task.id.takeLast(4).uppercase()
            binding.tvTaskCode.text = taskCode
            binding.tvTaskName.text = task.title
            val assigneeName = task.assignee?.fullName ?: "Unassigned"
            binding.tvTaskSubtitle.text = "Assignee: $assigneeName • ${task.priority.displayName} Priority"

            val statusColorRes = when (task.status) {
                TaskStatus.COMPLETED, TaskStatus.APPROVED -> R.color.cms_success
                TaskStatus.IN_PROGRESS -> R.color.cms_accent
                TaskStatus.SUBMITTED_FOR_REVIEW -> R.color.cms_warning
                TaskStatus.CORRECTIONS_REQUIRED, TaskStatus.BLOCKED -> R.color.cms_error
                TaskStatus.PENDING -> R.color.cms_text_secondary
            }
            val statusColor = ContextCompat.getColor(context, statusColorRes)
            binding.tvTaskStatus.text = task.status.displayName.uppercase()
            binding.tvTaskStatus.setTextColor(statusColor)

            when {
                task.status == TaskStatus.SUBMITTED_FOR_REVIEW || task.reviewStatus == "SUBMITTED" -> {
                    binding.tvTaskReviewStatus.visibility = android.view.View.VISIBLE
                    binding.tvTaskReviewStatus.text = "IN REVIEW"
                    binding.tvTaskReviewStatus.setTextColor(ContextCompat.getColor(context, R.color.cms_warning))
                }
                task.status == TaskStatus.CORRECTIONS_REQUIRED || task.reviewStatus == "CORRECTIONS_REQUIRED" -> {
                    binding.tvTaskReviewStatus.visibility = android.view.View.VISIBLE
                    binding.tvTaskReviewStatus.text = "CHANGES NEEDED"
                    binding.tvTaskReviewStatus.setTextColor(ContextCompat.getColor(context, R.color.cms_error))
                }
                task.status == TaskStatus.APPROVED || task.reviewStatus == "APPROVED" -> {
                    binding.tvTaskReviewStatus.visibility = android.view.View.VISIBLE
                    binding.tvTaskReviewStatus.text = "APPROVED"
                    binding.tvTaskReviewStatus.setTextColor(ContextCompat.getColor(context, R.color.cms_success))
                }
                else -> {
                    binding.tvTaskReviewStatus.visibility = android.view.View.GONE
                }
            }

            val progressInt = task.progressPct.toInt().coerceIn(0, 100)
            binding.progressTask.progress = progressInt
            binding.tvTaskProgressPct.text = "$progressInt%"

            binding.cardTask.setOnClickListener {
                onTaskClick(task)
            }
        }
    }
}
