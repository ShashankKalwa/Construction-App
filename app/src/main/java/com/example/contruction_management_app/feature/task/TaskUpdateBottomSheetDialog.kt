package com.example.contruction_management_app.feature.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.contruction_management_app.R
import com.example.contruction_management_app.core.auth.SessionDataStore
import com.example.contruction_management_app.databinding.BottomSheetTaskUpdateBinding
import com.example.contruction_management_app.domain.model.Task
import com.example.contruction_management_app.domain.model.TaskStatus
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

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
            TaskStatus.COMPLETED, TaskStatus.APPROVED -> binding.chipStatusCompleted.isChecked = true
            else -> binding.chipStatusInProgress.isChecked = true
        }

        binding.chipGroupStatusUpdate.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedStatus = when (checkedIds.firstOrNull()) {
                R.id.chipStatusInProgress -> TaskStatus.IN_PROGRESS
                R.id.chipStatusBlocked -> TaskStatus.BLOCKED
                R.id.chipStatusCompleted -> TaskStatus.COMPLETED
                else -> TaskStatus.PENDING
            }
        }

        // Standard progress update
        binding.btnSaveTaskUpdate.setOnClickListener {
            setLoading(true)
            taskViewModel.updateTaskProgress(
                taskId = task.id,
                progressPct = progressValue,
                status = selectedStatus,
                onSuccess = {
                    setLoading(false)
                    Toast.makeText(requireContext(), "Task progress updated", Toast.LENGTH_SHORT).show()
                    onTaskUpdated()
                    dismiss()
                }
            )
        }

        // Submit for Review action
        binding.btnSubmitReview.setOnClickListener {
            setLoading(true)
            val notes = binding.etSubmissionNotes.text?.toString()?.trim()
            taskViewModel.submitTaskReview(
                taskId = task.id,
                progressPct = progressValue,
                notes = if (notes.isNullOrEmpty()) null else notes,
                onSuccess = {
                    setLoading(false)
                    Toast.makeText(requireContext(), "Task submitted for approval", Toast.LENGTH_SHORT).show()
                    onTaskUpdated()
                    dismiss()
                },
                onError = { err ->
                    setLoading(false)
                    Toast.makeText(requireContext(), err, Toast.LENGTH_LONG).show()
                }
            )
        }

        setupReviewSection()
    }

    private fun setupReviewSection() {
        val session = SessionDataStore(requireContext().applicationContext)
        val isUnderReview = task.status == TaskStatus.SUBMITTED_FOR_REVIEW || task.reviewStatus == "SUBMITTED"

        if (isUnderReview) {
            binding.layoutReviewDecision.visibility = View.VISIBLE

            lifecycleScope.launch {
                val currentUser = session.currentUserFlow.firstOrNull()
                val isAssignee = currentUser != null && currentUser.id == task.assignee?.id

                if (isAssignee) {
                    binding.tvSelfReviewWarning.visibility = View.VISIBLE
                    binding.btnApproveTask.isEnabled = false
                    binding.btnRejectTask.isEnabled = false
                } else {
                    binding.tvSelfReviewWarning.visibility = View.GONE
                    binding.btnApproveTask.isEnabled = true
                    binding.btnRejectTask.isEnabled = true
                }
            }

            binding.btnApproveTask.setOnClickListener {
                setLoading(true)
                val notes = binding.etReviewNotes.text?.toString()?.trim()
                taskViewModel.reviewTask(
                    taskId = task.id,
                    action = "APPROVE",
                    notes = if (notes.isNullOrEmpty()) null else notes,
                    onSuccess = {
                        setLoading(false)
                        Toast.makeText(requireContext(), "Task approved successfully", Toast.LENGTH_SHORT).show()
                        onTaskUpdated()
                        dismiss()
                    },
                    onError = { err ->
                        setLoading(false)
                        Toast.makeText(requireContext(), err, Toast.LENGTH_LONG).show()
                    }
                )
            }

            binding.btnRejectTask.setOnClickListener {
                setLoading(true)
                val notes = binding.etReviewNotes.text?.toString()?.trim()
                taskViewModel.reviewTask(
                    taskId = task.id,
                    action = "REJECT",
                    notes = if (notes.isNullOrEmpty()) null else notes,
                    onSuccess = {
                        setLoading(false)
                        Toast.makeText(requireContext(), "Corrections requested", Toast.LENGTH_SHORT).show()
                        onTaskUpdated()
                        dismiss()
                    },
                    onError = { err ->
                        setLoading(false)
                        Toast.makeText(requireContext(), err, Toast.LENGTH_LONG).show()
                    }
                )
            }
        } else {
            binding.layoutReviewDecision.visibility = View.GONE
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressUpdateTask.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSaveTaskUpdate.isEnabled = !isLoading
        binding.btnSubmitReview.isEnabled = !isLoading
        binding.btnApproveTask.isEnabled = !isLoading
        binding.btnRejectTask.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "TaskUpdateBottomSheetDialog"
    }
}
