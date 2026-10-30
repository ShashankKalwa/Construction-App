package com.example.contruction_management_app.feature.inventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.contruction_management_app.databinding.DialogApproveMaterialRequestBinding
import com.example.contruction_management_app.domain.model.MaterialRequest
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ApproveMaterialRequestDialog(
    private val request: MaterialRequest,
    private val onApprove: (MaterialRequest) -> Unit,
    private val onReject: (MaterialRequest, String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogApproveMaterialRequestBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogApproveMaterialRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvRequestDetails.text = "${request.quantity} ${request.materialUnit} of ${request.materialName}"
        binding.tvRequestMeta.text = "Requested by: ${request.requestedBy} • Required: ${request.requiredByDate ?: "N/A"}"
        binding.tvRequestPhase.text = "Task: ${request.phaseOrTaskDesc ?: "Unspecified"}"

        binding.btnApprove.setOnClickListener {
            onApprove(request)
            dismiss()
        }

        binding.btnReject.setOnClickListener {
            val reason = binding.etReason.text?.toString()?.trim()
            if (reason.isNullOrBlank()) {
                binding.tilReason.error = "Reason is required when rejecting a request"
                return@setOnClickListener
            }
            onReject(request, reason)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ApproveMaterialRequestDialog"
    }
}

