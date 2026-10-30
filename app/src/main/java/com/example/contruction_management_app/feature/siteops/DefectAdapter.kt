package com.example.contruction_management_app.feature.siteops

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.ItemDefectBinding
import com.example.contruction_management_app.domain.model.DefectStatus
import com.example.contruction_management_app.domain.model.IssueSeverity
import com.example.contruction_management_app.domain.model.SiteDefect

class DefectAdapter(
    private val onResolveClicked: ((SiteDefect) -> Unit)? = null,
    private val onReinspectClicked: ((SiteDefect) -> Unit)? = null
) : ListAdapter<SiteDefect, DefectAdapter.DefectViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefectViewHolder {
        val binding = ItemDefectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DefectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DefectViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DefectViewHolder(private val binding: ItemDefectBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(defect: SiteDefect) {
            binding.tvDefectTitle.text = defect.title
            binding.tvDefectDescription.text = defect.description

            // Severity Badge
            binding.tvDefectSeverityBadge.text = defect.severity.name
            when (defect.severity) {
                IssueSeverity.CRITICAL -> {
                    binding.tvDefectSeverityBadge.setTextColor(ContextCompat.getColor(binding.root.context, R.color.cms_error))
                }
                IssueSeverity.HIGH -> {
                    binding.tvDefectSeverityBadge.setTextColor(ContextCompat.getColor(binding.root.context, R.color.cms_accent))
                }
                IssueSeverity.MEDIUM -> {
                    binding.tvDefectSeverityBadge.setTextColor(ContextCompat.getColor(binding.root.context, R.color.cms_info))
                }
                IssueSeverity.LOW -> {
                    binding.tvDefectSeverityBadge.setTextColor(ContextCompat.getColor(binding.root.context, R.color.cms_text_tertiary))
                }
            }

            // Status Badge & Action Button
            binding.tvDefectStatusBadge.text = defect.status.name
            when (defect.status) {
                DefectStatus.OPEN -> {
                    binding.tvDefectStatusBadge.setTextColor(ContextCompat.getColor(binding.root.context, R.color.cms_error))
                    binding.btnDefectAction.visibility = View.VISIBLE
                    binding.btnDefectAction.text = "Mark Resolved"
                    binding.btnDefectAction.setOnClickListener { onResolveClicked?.invoke(defect) }
                }
                DefectStatus.RESOLVED -> {
                    binding.tvDefectStatusBadge.setTextColor(ContextCompat.getColor(binding.root.context, R.color.cms_info))
                    binding.btnDefectAction.visibility = View.VISIBLE
                    binding.btnDefectAction.text = "QC Re-Inspect"
                    binding.btnDefectAction.setOnClickListener { onReinspectClicked?.invoke(defect) }
                }
                DefectStatus.CLOSED -> {
                    binding.tvDefectStatusBadge.setTextColor(ContextCompat.getColor(binding.root.context, R.color.cms_success))
                    binding.btnDefectAction.visibility = View.GONE
                }
            }

            val loc = defect.location?.let { "📍 $it" } ?: "📍 Site"
            val ddl = defect.deadline?.let { " • ⏰ Due: $it" } ?: ""
            binding.tvDefectLocationDeadline.text = "$loc$ddl"

            if (!defect.reInspectionNotes.isNullOrBlank() || !defect.reInspectedBy.isNullOrBlank()) {
                binding.layoutReinspectionNotes.visibility = View.VISIBLE
                val inspector = defect.reInspectedBy ?: "QC Inspector"
                val notes = defect.reInspectionNotes ?: "Passed inspection"
                binding.tvReinspectionNotes.text = "QC Re-inspection by $inspector: $notes"
            } else {
                binding.layoutReinspectionNotes.visibility = View.GONE
            }

            val assignee = defect.assignedTo?.let { "Assigned: $it" } ?: "Unassigned"
            binding.tvDefectAssigneeMeta.text = "$assignee | Logged by ${defect.createdBy}"
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SiteDefect>() {
        override fun areItemsTheSame(oldItem: SiteDefect, newItem: SiteDefect): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: SiteDefect, newItem: SiteDefect): Boolean =
            oldItem == newItem
    }
}
