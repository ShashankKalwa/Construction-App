package com.example.contruction_management_app.feature.shell

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.contruction_management_app.databinding.BottomSheetQuickCreateBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class QuickCreateBottomSheetDialog(
    private val onNewProjectSelected: () -> Unit,
    private val onTasksSelected: () -> Unit,
    private val onToolsSelected: () -> Unit
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "QuickCreateBottomSheetDialog"
    }
}
