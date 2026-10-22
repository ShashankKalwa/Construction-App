package com.example.contruction_management_app.feature.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.BottomSheetTaskUpdateBinding
import com.example.contruction_management_app.domain.model.Task
import com.example.contruction_management_app.domain.model.TaskStatus
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TaskUpdateBottomSheetDialog(
    private val task: Task,
    private val taskViewModel: TaskViewModel,
    private val onTaskUpdated: () -> Unit = {}
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetTaskUpdateBinding? = null
    private val binding get() = _binding!!

    private var selectedStatus: TaskStatus = task.status
    private var progressValue: Double = task.progressPct

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetTaskUpdateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val taskCode = "TSK-" + task.id.takeLast(4).uppercase()
        binding.tvSheetTaskCode.text = taskCode
        binding.tvSheetTaskTitle.text = task.title

        // Initialize slider
        val initialProgress = task.progressPct.toFloat().coerceIn(0f, 100f)
        binding.sliderProgress.value = initialProgress
        binding.tvSliderValue.text = "${initialProgress.toInt()}%"

        binding.sliderProgress.addOnChangeListener { _, value, _ ->
            progressValue = value.toDouble()
            binding.tvSliderValue.text = "${value.toInt()}%"
        }

        // Initialize status chips
        when (task.status) {
            TaskStatus.PENDING -> binding.chipStatusPending.isChecked = true
            TaskStatus.IN_PROGRESS -> binding.chipStatusInProgress.isChecked = true
            TaskStatus.BLOCKED -> binding.chipStatusBlocked.isChecked = true
            TaskStatus.COMPLETED -> binding.chipStatusCompleted.isChecked = true
        }

        binding.chipGroupStatusUpdate.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedStatus = when (checkedIds.firstOrNull()) {
                R.id.chipStatusInProgress -> TaskStatus.IN_PROGRESS
                R.id.chipStatusBlocked -> TaskStatus.BLOCKED
                R.id.chipStatusCompleted -> TaskStatus.COMPLETED
                else -> TaskStatus.PENDING
            }
        }

        binding.btnSaveTaskUpdate.setOnClickListener {
            binding.btnSaveTaskUpdate.isEnabled = false
            binding.progressUpdateTask.visibility = View.VISIBLE

            taskViewModel.updateTaskProgress(
                taskId = task.id,
                progressPct = progressValue,
                status = selectedStatus,
                onSuccess = {
                    binding.progressUpdateTask.visibility = View.GONE
                    Toast.makeText(requireContext(), "Task progress updated", Toast.LENGTH_SHORT).show()
                    onTaskUpdated()
                    dismiss()
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "TaskUpdateBottomSheetDialog"
    }
}
