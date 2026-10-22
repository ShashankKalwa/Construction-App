package com.example.contruction_management_app.feature.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.contruction_management_app.databinding.BottomSheetCreateProjectBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.math.BigDecimal

class CreateProjectBottomSheetDialog(
    private val projectViewModel: ProjectViewModel,
    private val onProjectCreated: () -> Unit = {}
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCreateProjectBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCreateProjectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSubmitProject.setOnClickListener {
            val name = binding.etProjectName.text?.toString()?.trim().orEmpty()
            val code = binding.etProjectCode.text?.toString()?.trim().orEmpty()
            val location = binding.etProjectLocation.text?.toString()?.trim().orEmpty()
            val budgetStr = binding.etProjectBudget.text?.toString()?.trim().orEmpty()

            if (name.isBlank() || code.isBlank() || location.isBlank() || budgetStr.isBlank()) {
                Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val budget = budgetStr.toBigDecimalOrNull()
            if (budget == null || budget <= BigDecimal.ZERO) {
                Toast.makeText(requireContext(), "Please enter a valid budget amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnSubmitProject.isEnabled = false
            binding.progressCreateProject.visibility = View.VISIBLE

            projectViewModel.createProject(
                name = name,
                code = code,
                location = location,
                budgetInr = budget,
                onSuccess = {
                    binding.progressCreateProject.visibility = View.GONE
                    Toast.makeText(requireContext(), "Project created successfully", Toast.LENGTH_SHORT).show()
                    onProjectCreated()
                    dismiss()
                },
                onError = { error ->
                    binding.btnSubmitProject.isEnabled = true
                    binding.progressCreateProject.visibility = View.GONE
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CreateProjectBottomSheetDialog"
    }
}
