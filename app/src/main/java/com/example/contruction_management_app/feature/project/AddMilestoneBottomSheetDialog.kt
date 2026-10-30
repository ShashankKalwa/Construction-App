package com.example.contruction_management_app.feature.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.contruction_management_app.databinding.BottomSheetAddMilestoneBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddMilestoneBottomSheetDialog(
    private val projectId: String,
    private val projectViewModel: ProjectViewModel,
    private val onCreated: () -> Unit = {}
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddMilestoneBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddMilestoneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.sliderMilestoneTarget.addOnChangeListener { _, value, _ ->
            binding.tvMilestoneTargetValue.text = "${value.toInt()}%"
        }

        binding.btnCreateMilestone.setOnClickListener {
            val title = binding.etMilestoneTitle.text?.toString()?.trim()
            if (title.isNullOrEmpty()) {
                binding.tilMilestoneTitle.error = "Milestone title is required"
                return@setOnClickListener
            }
            binding.tilMilestoneTitle.error = null

            val dueDate = binding.etMilestoneDueDate.text?.toString()?.trim()
            val targetPct = binding.sliderMilestoneTarget.value.toDouble()

            binding.btnCreateMilestone.isEnabled = false
            binding.progressCreateMilestone.visibility = View.VISIBLE

            projectViewModel.createMilestone(
                projectId = projectId,
                title = title,
                dueDate = if (dueDate.isNullOrBlank()) null else dueDate,
                targetCompletionPct = targetPct,
                onSuccess = {
                    binding.progressCreateMilestone.visibility = View.GONE
                    Toast.makeText(requireContext(), "Milestone added", Toast.LENGTH_SHORT).show()
                    onCreated()
                    dismiss()
                },
                onError = { err ->
                    binding.progressCreateMilestone.visibility = View.GONE
                    binding.btnCreateMilestone.isEnabled = true
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
        const val TAG = "AddMilestoneBottomSheetDialog"
    }
}
