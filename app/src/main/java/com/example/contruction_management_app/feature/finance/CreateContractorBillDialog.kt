package com.example.contruction_management_app.feature.finance

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.contruction_management_app.databinding.DialogCreateContractorBillBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

class CreateContractorBillDialog(
    private val onSubmit: (contractorName: String, billNumber: String?, gross: BigDecimal, date: String, workDesc: String?) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogCreateContractorBillBinding? = null
    private val binding get() = _binding!!

    private val inrFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCreateContractorBillBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etGrossAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                updateCalculationsPreview(s?.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSubmitBill.setOnClickListener {
            val contractor = binding.etContractorName.text?.toString()?.trim()
            if (contractor.isNullOrBlank()) {
                binding.tilContractorName.error = "Contractor name is required"
                return@setOnClickListener
            }

            val billNo = binding.etBillNumber.text?.toString()?.trim()?.ifBlank { null }

            val grossStr = binding.etGrossAmount.text?.toString()?.trim()
            if (grossStr.isNullOrBlank()) {
                binding.tilGrossAmount.error = "Gross amount is required"
                return@setOnClickListener
            }
            val gross = try {
                BigDecimal(grossStr)
            } catch (_: Exception) {
                binding.tilGrossAmount.error = "Invalid gross amount"
                return@setOnClickListener
            }

            if (gross.compareTo(BigDecimal.ZERO) <= 0) {
                binding.tilGrossAmount.error = "Gross amount must be positive"
                return@setOnClickListener
            }

            val desc = binding.etWorkDescription.text?.toString()?.trim()?.ifBlank { null }
            val date = LocalDate.now().toString()

            onSubmit(contractor, billNo, gross, date, desc)
            dismiss()
        }
    }

    private fun updateCalculationsPreview(grossText: String?) {
        val gross = try {
            if (grossText.isNullOrBlank()) BigDecimal.ZERO else BigDecimal(grossText)
        } catch (_: Exception) {
            BigDecimal.ZERO
        }

        val retention = gross.multiply(BigDecimal.valueOf(5.00)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
        val tds = gross.multiply(BigDecimal.valueOf(2.00)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
        val net = gross.subtract(retention).subtract(tds)

        binding.tvPreviewRetention.text = "Retention (5%): ${inrFormat.format(retention)}"
        binding.tvPreviewTds.text = "TDS (2%): ${inrFormat.format(tds)}"
        binding.tvPreviewNetPayable.text = "Net Payable: ${inrFormat.format(net)}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CreateContractorBillDialog"
    }
}
