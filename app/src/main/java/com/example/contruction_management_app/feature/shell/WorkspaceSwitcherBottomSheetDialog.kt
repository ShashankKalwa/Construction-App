package com.example.contruction_management_app.feature.shell

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contruction_management_app.core.auth.SessionDataStore
import com.example.contruction_management_app.databinding.BottomSheetWorkspaceSwitcherBinding
import com.example.contruction_management_app.domain.model.Project
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class WorkspaceSwitcherBottomSheetDialog(
    private val projects: List<Project>,
    private val onProjectChosen: (Project) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetWorkspaceSwitcherBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetWorkspaceSwitcherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val session = SessionDataStore(requireContext().applicationContext)

        lifecycleScope.launch {
            val user = session.currentUserFlow.firstOrNull()
            val activeProjectId = session.activeProjectIdFlow.firstOrNull()

            binding.tvActiveCompanyName.text = user?.organizationName ?: "Apex Infra Ltd"
            binding.tvActiveCompanyRole.text = "Role: ${user?.role?.displayName ?: "Operator"}"

            if (projects.isEmpty()) {
                binding.tvEmptyProjects.visibility = View.VISIBLE
                binding.rvWorkspaceProjects.visibility = View.GONE
            } else {
                binding.tvEmptyProjects.visibility = View.GONE
                binding.rvWorkspaceProjects.visibility = View.VISIBLE

                val adapter = WorkspaceProjectAdapter(activeProjectId) { chosen ->
                    lifecycleScope.launch {
                        session.setActiveProjectId(chosen.id)
                        onProjectChosen(chosen)
                        dismiss()
                    }
                }
                binding.rvWorkspaceProjects.layoutManager = LinearLayoutManager(requireContext())
                binding.rvWorkspaceProjects.adapter = adapter
                adapter.submitList(projects)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "WorkspaceSwitcherBottomSheetDialog"

        fun newInstance(
            projects: List<Project>,
            onProjectChosen: (Project) -> Unit
        ): WorkspaceSwitcherBottomSheetDialog = WorkspaceSwitcherBottomSheetDialog(projects, onProjectChosen)
    }
}
