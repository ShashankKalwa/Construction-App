package com.example.contruction_management_app.feature.finance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contruction_management_app.databinding.ItemMaterialConsumptionBinding
import com.example.contruction_management_app.domain.model.MaterialConsumption
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

class MaterialConsumptionAdapter :
    ListAdapter<MaterialConsumption, MaterialConsumptionAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMaterialConsumptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemMaterialConsumptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val inrFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }

        fun bind(item: MaterialConsumption) {
            binding.tvMaterialName.text = item.materialName
            binding.tvQuantityConsumed.text = "${item.quantityConsumed} ${item.unit} consumed"

            val costStr = if (item.totalCost.compareTo(BigDecimal("10000000")) >= 0) {
                val cr = item.totalCost.divide(BigDecimal("10000000"), 2, java.math.RoundingMode.HALF_UP)
                "₹$cr Cr"
            } else if (item.totalCost.compareTo(BigDecimal("100000")) >= 0) {
                val l = item.totalCost.divide(BigDecimal("100000"), 1, java.math.RoundingMode.HALF_UP)
                "₹$l L"
            } else {
                inrFormat.format(item.totalCost)
            }

            binding.tvTotalCost.text = costStr
            binding.tvConsumptionPct.text = "${item.consumptionPercentage}%"
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<MaterialConsumption>() {
        override fun areItemsTheSame(oldItem: MaterialConsumption, newItem: MaterialConsumption): Boolean {
            return oldItem.materialId == newItem.materialId
        }

        override fun areContentsTheSame(oldItem: MaterialConsumption, newItem: MaterialConsumption): Boolean {
            return oldItem == newItem
        }
    }
}
