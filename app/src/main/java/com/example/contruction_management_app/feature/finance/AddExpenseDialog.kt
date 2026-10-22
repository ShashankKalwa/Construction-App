package com.example.contruction_management_app.feature.finance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.contruction_management_app.databinding.DialogAddExpenseBinding
import com.example.contruction_management_app.domain.model.ExpenseCategory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.math.BigDecimal
import java.time.LocalDate

class AddExpenseDialog(
    private val onSubmit: (category: ExpenseCategory, title: String, amount: BigDecimal, date: String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogAddExpenseBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categories = ExpenseCategory.values().map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.actvCategory.setAdapter(adapter)
        binding.actvCategory.setText(categories.first(), false)

        binding.etDate.setText(LocalDate.now().toString())

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSubmitExpense.setOnClickListener {
            val catName = binding.actvCategory.text?.toString()?.trim() ?: ExpenseCategory.MATERIALS.name
            val category = try {
                ExpenseCategory.valueOf(catName)
            } catch (_: Exception) {
                ExpenseCategory.MATERIALS
            }

            val title = binding.etTitle.text?.toString()?.trim()
            if (title.isNullOrBlank()) {
                binding.tilTitle.error = "Title is required"
                return@setOnClickListener
            }

            val amountStr = binding.etAmount.text?.toString()?.trim()
            if (amountStr.isNullOrBlank()) {
                binding.tilAmount.error = "Amount is required"
                return@setOnClickListener
            }
            val amount = try {
                BigDecimal(amountStr)
            } catch (_: Exception) {
                binding.tilAmount.error = "Invalid amount"
                return@setOnClickListener
            }

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                binding.tilAmount.error = "Amount must be greater than zero"
                return@setOnClickListener
            }

            val date = binding.etDate.text?.toString()?.trim()?.ifBlank { LocalDate.now().toString() }
                ?: LocalDate.now().toString()

            onSubmit(category, title, amount, date)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AddExpenseDialog"
    }
}
