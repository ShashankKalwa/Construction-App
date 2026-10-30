package com.example.contruction_management_app.feature.inventory

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.ItemMaterialRequestBinding
import com.example.contruction_management_app.domain.model.MaterialRequest
import com.example.contruction_management_app.domain.model.MaterialRequestStatus

class MaterialRequestAdapter(
    private val onApproveClick: (MaterialRequest) -> Unit,
    private val onRejectClick: (MaterialRequest) -> Unit,
    private val onIssueClick: (MaterialRequest) -> Unit
) : ListAdapter<MaterialRequest, MaterialRequestAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMaterialRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onApproveClick, onRejectClick, onIssueClick)
    }

    class ViewHolder(private val binding: ItemMaterialRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            request: MaterialRequest,
            onApproveClick: (MaterialRequest) -> Unit,
            onRejectClick: (MaterialRequest) -> Unit,
            onIssueClick: (MaterialRequest) -> Unit
        ) {
            val context = binding.root.context
            binding.tvRequestCode.text = request.id.uppercase()
            binding.tvRequestDate.text = request.createdAt?.take(10) ?: ""
            binding.tvMaterialName.text = request.materialName
            binding.tvQuantity.text = "${request.quantity} ${request.materialUnit}"
            binding.tvRequiredDate.text = request.requiredByDate ?: "Not specified"
            binding.tvRequestedBy.text = "Requested by: ${request.requestedBy}"

            if (!request.phaseOrTaskDesc.isNullOrBlank()) {
                binding.tvTaskDesc.visibility = View.VISIBLE
                binding.tvTaskDesc.text = "Phase/Task: ${request.phaseOrTaskDesc}"
            } else {
                binding.tvTaskDesc.visibility = View.GONE
            }

            // Audit details
            val auditParts = mutableListOf<String>()
            if (!request.approvedBy.isNullOrBlank()) {
                auditParts.add("Approved by ${request.approvedBy}")
            }
            if (!request.issuedBy.isNullOrBlank()) {
                auditParts.add("Issued by ${request.issuedBy}")
            }
            if (auditParts.isNotEmpty()) {
                binding.tvAuditDetails.visibility = View.VISIBLE
                binding.tvAuditDetails.text = auditParts.joinToString(" • ")
            } else {
                binding.tvAuditDetails.visibility = View.GONE
            }

            // Status Badge & Action Buttons
            when (request.status) {
                MaterialRequestStatus.PENDING -> {
                    binding.tvStatusBadge.text = "PENDING APPROVAL"
                    binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.cms_warning))
                    binding.tvStatusBadge.backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.cms_warning_tint))

                    binding.layoutActionButtons.visibility = View.VISIBLE
                    binding.btnApprove.visibility = View.VISIBLE
                    binding.btnReject.visibility = View.VISIBLE
                    binding.btnIssueStock.visibility = View.GONE
                    binding.containerRejectionReason.visibility = View.GONE

                    binding.btnApprove.setOnClickListener { onApproveClick(request) }
                    binding.btnReject.setOnClickListener { onRejectClick(request) }
                }
                MaterialRequestStatus.APPROVED -> {
                    binding.tvStatusBadge.text = "APPROVED (READY TO ISSUE)"
                    binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.cms_info))
                    binding.tvStatusBadge.backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.cms_info_tint))

                    binding.layoutActionButtons.visibility = View.VISIBLE
                    binding.btnApprove.visibility = View.GONE
                    binding.btnReject.visibility = View.GONE
                    binding.btnIssueStock.visibility = View.VISIBLE
                    binding.containerRejectionReason.visibility = View.GONE

                    binding.btnIssueStock.setOnClickListener { onIssueClick(request) }
                }
                MaterialRequestStatus.ISSUED, MaterialRequestStatus.FULFILLED -> {
                    binding.tvStatusBadge.text = if (request.status == MaterialRequestStatus.ISSUED) "ISSUED TO SITE" else "FULFILLED"
                    binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.cms_success))
                    binding.tvStatusBadge.backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.cms_success_tint))

                    binding.layoutActionButtons.visibility = View.GONE
                    binding.containerRejectionReason.visibility = View.GONE
                }
                MaterialRequestStatus.REJECTED -> {
                    binding.tvStatusBadge.text = "REJECTED"
                    binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.cms_error))
                    binding.tvStatusBadge.backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.cms_error_tint))

                    binding.layoutActionButtons.visibility = View.GONE
                    if (!request.rejectionReason.isNullOrBlank()) {
                        binding.containerRejectionReason.visibility = View.VISIBLE
                        binding.tvRejectionReason.text = request.rejectionReason
                    } else {
                        binding.containerRejectionReason.visibility = View.GONE
                    }
                }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<MaterialRequest>() {
        override fun areItemsTheSame(oldItem: MaterialRequest, newItem: MaterialRequest): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MaterialRequest, newItem: MaterialRequest): Boolean {
            return oldItem == newItem
        }
    }
}
