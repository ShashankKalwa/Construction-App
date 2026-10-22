package com.example.contruction_management_app.feature.inventory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contruction_management_app.databinding.ItemMaterialCardBinding
import com.example.contruction_management_app.domain.model.Material
import java.text.NumberFormat
import java.util.Locale

class MaterialAdapter(
    private val onIssueClick: (Material) -> Unit,
    private val onAdjustClick: (Material) -> Unit
) : ListAdapter<Material, MaterialAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMaterialCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onIssueClick, onAdjustClick)
    }

    class ViewHolder(private val binding: ItemMaterialCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val inrFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }

        fun bind(
            material: Material,
            onIssueClick: (Material) -> Unit,
            onAdjustClick: (Material) -> Unit
        ) {
            binding.tvMaterialCode.text = material.code
            binding.tvMaterialCategory.text = material.category.name
            binding.tvMaterialName.text = material.name

            binding.tvCurrentStock.text = "${material.currentStock} ${material.unit}"
            binding.tvMinThreshold.text = "Min: ${material.minStockThreshold} ${material.unit}"
            binding.tvUnitCost.text = "Rate: ${inrFormat.format(material.unitCost)} / ${material.unit}"

            // Warning badge
            binding.tvLowStockBadge.visibility = if (material.isLowStock) View.VISIBLE else View.GONE

            binding.btnIssueStock.setOnClickListener {
                onIssueClick(material)
            }

            binding.btnAdjustStock.setOnClickListener {
                onAdjustClick(material)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Material>() {
        override fun areItemsTheSame(oldItem: Material, newItem: Material): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Material, newItem: Material): Boolean {
            return oldItem == newItem
        }
    }
}
