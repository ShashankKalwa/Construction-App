package com.example.contruction_management_app.feature.siteops

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.ItemWorkProgressBinding
import com.example.contruction_management_app.domain.model.WorkProgress
import com.example.contruction_management_app.domain.model.WorkProgressStatus

class WorkProgressAdapter(
    private val onVerifyClicked: ((WorkProgress) -> Unit)? = null
) : ListAdapter<WorkProgress, WorkProgressAdapter.ProgressViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgressViewHolder {
        val binding = ItemWorkProgressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProgressViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProgressViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProgressViewHolder(private val binding: ItemWorkProgressBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(progress: WorkProgress) {
            binding.tvProgressItemName.text = progress.itemName
            binding.tvPlannedQty.text = "${progress.plannedQuantity.stripTrailingZeros().toPlainString()} ${progress.unit}"
            binding.tvReportedQty.text = "${progress.reportedQuantity.stripTrailingZeros().toPlainString()} ${progress.unit}"
            binding.tvVerifiedQty.text = "${progress.verifiedQuantity.stripTrailingZeros().toPlainString()} ${progress.unit}"

            // Status Badge
            binding.tvProgressStatusBadge.text = progress.status.name
            when (progress.status) {
                WorkProgressStatus.VERIFIED -> {
                    binding.tvProgressStatusBadge.setTextColor(ContextCompat.getColor(binding.root.context, R.color.cms_success))
                    binding.btnVerifyProgress.visibility = View.GONE
                }
                WorkProgressStatus.REPORTED -> {
                    binding.tvProgressStatusBadge.setTextColor(ContextCompat.getColor(binding.root.context, R.color.cms_accent))
                    binding.btnVerifyProgress.visibility = View.VISIBLE
                }
                WorkProgressStatus.REJECTED -> {
                    binding.tvProgressStatusBadge.setTextColor(ContextCompat.getColor(binding.root.context, R.color.cms_error))
                    binding.btnVerifyProgress.visibility = View.GONE
                }
            }

            if (!progress.verificationNotes.isNullOrBlank() || !progress.verifiedBy.isNullOrBlank()) {
                binding.layoutVerificationNotes.visibility = View.VISIBLE
                val verifier = progress.verifiedBy ?: "Verifier"
                val notes = progress.verificationNotes ?: "Verified"
                binding.tvVerificationNotes.text = "Verified by $verifier: $notes"
            } else {
                binding.layoutVerificationNotes.visibility = View.GONE
            }

            binding.tvReportedMeta.text = "Reported by ${progress.reportedBy}"

            binding.btnVerifyProgress.setOnClickListener {
                onVerifyClicked?.invoke(progress)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<WorkProgress>() {
        override fun areItemsTheSame(oldItem: WorkProgress, newItem: WorkProgress): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: WorkProgress, newItem: WorkProgress): Boolean =
            oldItem == newItem
    }
}
