package com.example.contruction_management_app.feature.project

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.ItemProjectCardBinding
import com.example.contruction_management_app.domain.model.Project
import java.math.BigDecimal
import java.math.RoundingMode

class ProjectAdapter(
    private val onProjectClick: (Project) -> Unit
) : ListAdapter<Project, ProjectAdapter.ProjectViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val binding = ItemProjectCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProjectViewHolder(
        private val binding: ItemProjectCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(project: Project) {
            val context = itemView.context

            binding.tvProjectName.text = project.name
            binding.tvProjectCode.text = project.code
            binding.tvProjectLocation.text = project.location
            binding.tvProjectStatus.text = project.status.replace("_", " ")

            val statusColorRes = when (project.status.uppercase()) {
                "COMPLETED" -> R.color.cms_success
                "IN_PROGRESS" -> R.color.cms_accent
                "PLANNING" -> R.color.cms_info
                "ON_HOLD" -> R.color.cms_warning
                else -> R.color.cms_text_secondary
            }
            val statusColor = ContextCompat.getColor(context, statusColorRes)
            binding.tvProjectStatus.setTextColor(statusColor)

            // Format Budget in INR (Crores / Lakhs)
            val budgetFormatted = formatBudget(project.budgetInr)
            binding.tvProjectBudget.text = "Budget: $budgetFormatted"

            val progressInt = project.progressPct.toInt().coerceIn(0, 100)
            binding.progressProject.progress = progressInt
            binding.tvProjectProgressPct.text = "$progressInt%"

            binding.cardProject.setOnClickListener {
                onProjectClick(project)
            }
        }

        private fun formatBudget(budget: BigDecimal): String {
            val cr = BigDecimal("10000000")
            val lakh = BigDecimal("100000")
            return when {
                budget >= cr -> "₹" + budget.divide(cr, 2, RoundingMode.HALF_UP).toPlainString() + " Cr"
                budget >= lakh -> "₹" + budget.divide(lakh, 2, RoundingMode.HALF_UP).toPlainString() + " L"
                else -> "₹" + budget.toPlainString()
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Project>() {
        override fun areItemsTheSame(oldItem: Project, newItem: Project): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Project, newItem: Project): Boolean =
            oldItem == newItem
    }
}
