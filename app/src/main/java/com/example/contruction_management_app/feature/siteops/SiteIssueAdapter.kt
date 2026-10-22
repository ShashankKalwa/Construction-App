package com.example.contruction_management_app.feature.siteops

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.ItemSiteIssueBinding
import com.example.contruction_management_app.domain.model.IssueSeverity
import com.example.contruction_management_app.domain.model.IssueStatus
import com.example.contruction_management_app.domain.model.SiteIssue

class SiteIssueAdapter(
    private val onUpdateStatusClicked: (SiteIssue) -> Unit
) : ListAdapter<SiteIssue, SiteIssueAdapter.IssueViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IssueViewHolder {
        val binding = ItemSiteIssueBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IssueViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IssueViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class IssueViewHolder(private val binding: ItemSiteIssueBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(issue: SiteIssue) {
            val context = binding.root.context
            binding.tvIssueTitle.text = issue.title
            binding.tvIssueDescription.text = issue.description ?: "No description provided."
            binding.tvIssueSeverity.text = issue.severity.name
            binding.tvIssueStatus.text = issue.status.name.replace("_", " ")

            val sevColorRes = when (issue.severity) {
                IssueSeverity.CRITICAL -> R.color.cms_error
                IssueSeverity.HIGH -> R.color.cms_warning
                IssueSeverity.MEDIUM -> R.color.cms_info
                IssueSeverity.LOW -> R.color.cms_text_secondary
            }
            binding.tvIssueSeverity.setTextColor(ContextCompat.getColor(context, sevColorRes))

            val statusColorRes = when (issue.status) {
                IssueStatus.OPEN -> R.color.cms_warning
                IssueStatus.IN_PROGRESS -> R.color.cms_info
                IssueStatus.RESOLVED -> R.color.cms_success
                IssueStatus.CLOSED -> R.color.cms_text_tertiary
            }
            binding.tvIssueStatus.setTextColor(ContextCompat.getColor(context, statusColorRes))

            binding.tvIssueAssignedTo.text = issue.assignedTo?.let { "Assigned: $it" } ?: "Assigned: Unassigned"

            binding.btnUpdateIssueStatus.setOnClickListener {
                onUpdateStatusClicked(issue)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SiteIssue>() {
        override fun areItemsTheSame(oldItem: SiteIssue, newItem: SiteIssue): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: SiteIssue, newItem: SiteIssue): Boolean =
            oldItem == newItem
    }
}
