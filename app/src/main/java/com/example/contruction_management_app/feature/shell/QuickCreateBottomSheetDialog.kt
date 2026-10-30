package com.example.contruction_management_app.feature.shell

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.contruction_management_app.core.auth.SessionDataStore
import com.example.contruction_management_app.databinding.BottomSheetQuickCreateBinding
import com.example.contruction_management_app.domain.model.Role
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class QuickCreateBottomSheetDialog(
    private val onNewProjectSelected: () -> Unit,
    private val onTasksSelected: () -> Unit,
    private val onToolsSelected: () -> Unit,
    private val onDsrSelected: (() -> Unit)? = null,
    private val onExpenseSelected: (() -> Unit)? = null
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetQuickCreateBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetQuickCreateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val session = SessionDataStore(requireContext().applicationContext)

        lifecycleScope.launch {
            val user = session.currentUserFlow.firstOrNull()
            val permissions = session.effectivePermissionsFlow.firstOrNull() ?: emptySet()
            val role = user?.role

            val canCreateProject = role == Role.SUPER_ADMIN || role == Role.COMPANY_ADMIN ||
                    role == Role.PROJECT_MANAGER || permissions.contains("projects:create")

            val canManageTasks = role == Role.SUPER_ADMIN || role == Role.COMPANY_ADMIN ||
                    role == Role.PROJECT_MANAGER || role == Role.SITE_SUPERVISOR ||
                    permissions.contains("tasks:update") || permissions.contains("tasks:manage")

            val canUseCalculators = role != Role.ACCOUNTANT && role != Role.STOREKEEPER

            val canSubmitDsr = role == Role.SITE_SUPERVISOR || role == Role.PROJECT_MANAGER ||
                    role == Role.CIVIL_ENGINEER || permissions.contains("reports:daily_site")

            val canSubmitExpense = role == Role.ACCOUNTANT || role == Role.SITE_SUPERVISOR ||
                    role == Role.PROJECT_MANAGER || permissions.contains("expenses:submit")

            binding.actionQuickNewProject.visibility = if (canCreateProject) View.VISIBLE else View.GONE
            binding.actionQuickTasks.visibility = if (canManageTasks) View.VISIBLE else View.GONE
            binding.actionQuickTools.visibility = if (canUseCalculators) View.VISIBLE else View.GONE
            binding.actionQuickDsr.visibility = if (canSubmitDsr) View.VISIBLE else View.GONE
            binding.actionQuickExpense.visibility = if (canSubmitExpense) View.VISIBLE else View.GONE
        }

        binding.actionQuickNewProject.setOnClickListener {
            dismiss()
            onNewProjectSelected()
        }

        binding.actionQuickTasks.setOnClickListener {
            dismiss()
            onTasksSelected()
        }

        binding.actionQuickTools.setOnClickListener {
            dismiss()
            onToolsSelected()
        }

        binding.actionQuickDsr.setOnClickListener {
            dismiss()
            onDsrSelected?.invoke() ?: onTasksSelected()
        }

        binding.actionQuickExpense.setOnClickListener {
            dismiss()
            onExpenseSelected?.invoke() ?: onTasksSelected()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "QuickCreateBottomSheetDialog"
    }
}
