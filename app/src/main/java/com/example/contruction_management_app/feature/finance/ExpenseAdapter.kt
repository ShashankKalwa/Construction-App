package com.example.contruction_management_app.feature.finance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contruction_management_app.databinding.ItemExpenseBinding
import com.example.contruction_management_app.domain.model.Expense
import com.example.contruction_management_app.domain.model.ExpenseStatus
import java.text.NumberFormat
import java.util.Locale

class ExpenseAdapter(
    private val onAdvanceStatusClick: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExpenseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onAdvanceStatusClick)
    }

    class ViewHolder(private val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val inrFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }

        fun bind(expense: Expense, onAdvanceStatusClick: (Expense) -> Unit) {
            binding.tvExpenseCategory.text = expense.category.name
            binding.tvExpenseStatus.text = expense.status.name
            binding.tvExpenseTitle.text = expense.title
            binding.tvExpenseDate.text = "Date: ${expense.expenseDate} • By ${expense.submittedBy ?: "Engineer"}"
            binding.tvExpenseAmount.text = inrFormat.format(expense.amount)

            when (expense.status) {
                ExpenseStatus.SUBMITTED -> {
                    binding.btnAdvanceStatus.visibility = View.VISIBLE
                    binding.btnAdvanceStatus.text = "Verify Expense"
                }
                ExpenseStatus.VERIFIED -> {
                    binding.btnAdvanceStatus.visibility = View.VISIBLE
                    binding.btnAdvanceStatus.text = "Approve Expense"
                }
                ExpenseStatus.APPROVED -> {
                    binding.btnAdvanceStatus.visibility = View.VISIBLE
                    binding.btnAdvanceStatus.text = "Mark as Paid"
                }
                ExpenseStatus.PAID, ExpenseStatus.REJECTED -> {
                    binding.btnAdvanceStatus.visibility = View.GONE
                }
            }

            binding.btnAdvanceStatus.setOnClickListener {
                onAdvanceStatusClick(expense)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem == newItem
        }
    }
}
