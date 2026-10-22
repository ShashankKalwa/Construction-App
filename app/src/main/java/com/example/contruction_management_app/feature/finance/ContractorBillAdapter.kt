package com.example.contruction_management_app.feature.finance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contruction_management_app.databinding.ItemContractorBillBinding
import com.example.contruction_management_app.domain.model.BillPaymentStatus
import com.example.contruction_management_app.domain.model.ContractorBill
import java.text.NumberFormat
import java.util.Locale

class ContractorBillAdapter(
    private val onAdvanceBillClick: (ContractorBill) -> Unit
) : ListAdapter<ContractorBill, ContractorBillAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContractorBillBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onAdvanceBillClick)
    }

    class ViewHolder(private val binding: ItemContractorBillBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val inrFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }

        fun bind(bill: ContractorBill, onAdvanceBillClick: (ContractorBill) -> Unit) {
            binding.tvBillNumber.text = bill.billNumber
            binding.tvBillStatus.text = bill.status.name
            binding.tvContractorName.text = bill.contractorName
            binding.tvWorkDescription.text = bill.workDescription ?: "Work certified as per measurements"

            binding.tvGrossAmount.text = inrFormat.format(bill.grossAmount)
            binding.tvRetentionLabel.text = "Less Retention (${bill.retentionPct}%):"
            binding.tvRetentionAmount.text = "- ${inrFormat.format(bill.retentionAmount)}"
            binding.tvTdsLabel.text = "Less TDS (${bill.tdsPct}%):"
            binding.tvTdsAmount.text = "- ${inrFormat.format(bill.tdsAmount)}"
            binding.tvNetPayable.text = inrFormat.format(bill.netPayable)

            binding.tvBillDate.text = "Certified: ${bill.billDate} • By ${bill.certifiedBy ?: "PM"}"

            when (bill.status) {
                BillPaymentStatus.PENDING -> {
                    binding.btnAdvanceBillStatus.visibility = View.VISIBLE
                    binding.btnAdvanceBillStatus.text = "Approve Bill"
                }
                BillPaymentStatus.APPROVED -> {
                    binding.btnAdvanceBillStatus.visibility = View.VISIBLE
                    binding.btnAdvanceBillStatus.text = "Disburse Payment"
                }
                BillPaymentStatus.PAID, BillPaymentStatus.REJECTED -> {
                    binding.btnAdvanceBillStatus.visibility = View.GONE
                }
            }

            binding.btnAdvanceBillStatus.setOnClickListener {
                onAdvanceBillClick(bill)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ContractorBill>() {
        override fun areItemsTheSame(oldItem: ContractorBill, newItem: ContractorBill): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ContractorBill, newItem: ContractorBill): Boolean {
            return oldItem == newItem
        }
    }
}
