package com.example.contruction_management_app.feature.tools

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contruction_management_app.databinding.ItemCalculationHistoryBinding
import com.example.contruction_management_app.domain.model.CalculationHistoryItem
import java.util.Locale

class CalculationHistoryAdapter(
    private val onItemClick: (CalculationHistoryItem) -> Unit = {}
) : ListAdapter<CalculationHistoryItem, CalculationHistoryAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCalculationHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemCalculationHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CalculationHistoryItem) {
            binding.tvHistoryTypeBadge.text = item.calculatorType.name
            binding.tvHistoryTitle.text = item.title
            binding.tvHistoryQuantity.text = String.format(
                Locale.US,
                "%.2f %s",
                item.primaryQuantity,
                item.primaryUnit
            )

            val elementStr = if (!item.elementType.isNullOrBlank()) {
                "Element: ${item.elementType}"
            } else {
                item.calculatorType.displayName
            }
            binding.tvHistoryElement.text = elementStr

            val formattedDate = if (item.createdAt.length >= 16) {
                item.createdAt.substring(0, 16).replace("T", " ")
            } else {
                item.createdAt
            }
            binding.tvHistoryDate.text = "Saved: $formattedDate"
            binding.tvHistoryAuthor.text = if (!item.createdBy.isNullOrBlank()) "By: ${item.createdBy}" else "By: Site Engineer"

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<CalculationHistoryItem>() {
        override fun areItemsTheSame(oldItem: CalculationHistoryItem, newItem: CalculationHistoryItem): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: CalculationHistoryItem, newItem: CalculationHistoryItem): Boolean =
            oldItem == newItem
    }
}
