package com.example.contruction_management_app.feature.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.BottomSheetAddMemberBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddMemberBottomSheetDialog(
    private val projectId: String,
    private val projectViewModel: ProjectViewModel,
    private val onAssigned: () -> Unit = {}
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddMemberBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddMemberBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAssignMember.setOnClickListener {
            val userId = binding.etUserId.text?.toString()?.trim()
            if (userId.isNullOrEmpty()) {
                binding.tilUserId.error = "User identifier is required"
                return@setOnClickListener
            }
            binding.tilUserId.error = null

            val assignedRole = when (binding.chipGroupProjectRole.checkedChipId) {
                R.id.chipRoleSiteSupervisor -> "SITE_SUPERVISOR"
                R.id.chipRoleStorekeeper -> "STOREKEEPER"
                R.id.chipRoleAccountant -> "ACCOUNTANT"
                R.id.chipRoleContractor -> "CONTRACTOR"
                else -> "CIVIL_ENGINEER"
            }

            binding.btnAssignMember.isEnabled = false
            binding.progressAssignMember.visibility = View.VISIBLE

            projectViewModel.assignMember(
                projectId = projectId,
                userId = userId,
                assignedRole = assignedRole,
                onSuccess = {
                    binding.progressAssignMember.visibility = View.GONE
                    Toast.makeText(requireContext(), "Team member assigned", Toast.LENGTH_SHORT).show()
                    onAssigned()
                    dismiss()
                },
                onError = { err ->
                    binding.progressAssignMember.visibility = View.GONE
                    binding.btnAssignMember.isEnabled = true
                    Toast.makeText(requireContext(), err, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AddMemberBottomSheetDialog"
    }
}
