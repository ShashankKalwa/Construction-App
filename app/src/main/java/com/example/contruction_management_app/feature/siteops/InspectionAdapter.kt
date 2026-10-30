package com.example.contruction_management_app.feature.siteops

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.ItemInspectionBinding
import com.example.contruction_management_app.domain.model.InspectionStatus
import com.example.contruction_management_app.domain.model.InspectionType
import com.example.contruction_management_app.domain.model.SiteInspection

class InspectionAdapter(
    private val onInspectionClicked: ((SiteInspection) -> Unit)? = null
) : ListAdapter<SiteInspection, InspectionAdapter.InspectionViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InspectionViewHolder {
        val binding = ItemInspectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InspectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InspectionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class InspectionViewHolder(private val binding: ItemInspectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(inspection: SiteInspection) {
            binding.tvInspectionTitle.text = inspection.title
            binding.tvInspectionTypeBadge.text = inspection.inspectionType.name

            val loc = inspection.location?.let { " • $it" } ?: ""
            binding.tvInspectionLocationDate.text = "${inspection.inspectionDate}$loc"

            // Status Badge
            binding.tvInspectionStatusBadge.text = inspection.status.name
            when (inspection.status) {
                InspectionStatus.PASSED -> {
                    binding.tvInspectionStatusBadge.setTextColor(ContextCompat.getColor(binding.root.context, R.color.cms_success))
                }
                InspectionStatus.FAILED -> {
                    binding.tvInspectionStatusBadge.setTextColor(ContextCompat.getColor(binding.root.context, R.color.cms_error))
                }
                InspectionStatus.CONDITIONALLY_PASSED -> {
                    binding.tvInspectionStatusBadge.setTextColor(ContextCompat.getColor(binding.root.context, R.color.cms_accent))
                }
            }

            if (!inspection.notes.isNullOrBlank()) {
                binding.tvInspectionNotes.visibility = android.view.View.VISIBLE
                binding.tvInspectionNotes.text = inspection.notes
            } else {
                binding.tvInspectionNotes.visibility = android.view.View.GONE
            }

            // Checklist preview
            val totalCount = "\"item\":".toRegex().findAll(inspection.checklistData).count()
            val passCount = "\"passed\":true".toRegex().findAll(inspection.checklistData).count()
            if (totalCount > 0) {
                binding.tvChecklistSummary.visibility = android.view.View.VISIBLE
                binding.tvChecklistSummary.text = "$passCount of $totalCount checklist items passed"
            } else {
                binding.tvChecklistSummary.visibility = android.view.View.GONE
            }

            binding.tvInspectorMeta.text = "Inspector: ${inspection.inspectorId}"

            binding.root.setOnClickListener {
                onInspectionClicked?.invoke(inspection)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SiteInspection>() {
        override fun areItemsTheSame(oldItem: SiteInspection, newItem: SiteInspection): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: SiteInspection, newItem: SiteInspection): Boolean =
            oldItem == newItem
    }
}
