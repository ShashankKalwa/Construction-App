package com.example.contruction_management_app.feature.finance

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.ItemPurchaseOrderBinding
import com.example.contruction_management_app.domain.model.PurchaseOrder
import com.example.contruction_management_app.domain.model.PurchaseOrderStatus
import java.text.NumberFormat
import java.util.Locale

class PurchaseOrderAdapter(
    private val onRecordGrnClick: (PurchaseOrder) -> Unit
) : ListAdapter<PurchaseOrder, PurchaseOrderAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPurchaseOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onRecordGrnClick)
    }

    class ViewHolder(private val binding: ItemPurchaseOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val inrFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }

        fun bind(po: PurchaseOrder, onRecordGrnClick: (PurchaseOrder) -> Unit) {
            val context = binding.root.context
            binding.tvPoNumber.text = po.poNumber
            binding.tvOrderDate.text = po.orderDate
            binding.tvVendorName.text = po.vendorName ?: "Vendor (ID: ${po.vendorId})"
            binding.tvTotalAmount.text = inrFormat.format(po.totalAmount)
            binding.tvDeadline.text = po.deliveryDeadline ?: "Not specified"

            // Summary of line items
            if (po.items.isNotEmpty()) {
                val summary = po.items.joinToString("; ") {
                    "${it.materialName}: ${it.receivedQuantity} / ${it.orderedQuantity} ${it.unit} received"
                }
                binding.tvItemsSummary.visibility = View.VISIBLE
                binding.tvItemsSummary.text = "Items: $summary"
            } else {
                binding.tvItemsSummary.visibility = View.GONE
            }

            // Status Badge and Action Button
            when (po.status) {
                PurchaseOrderStatus.ISSUED -> {
                    binding.tvPoStatusBadge.text = "ISSUED"
                    binding.tvPoStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.cms_warning))
                    binding.tvPoStatusBadge.backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.cms_warning_tint))
                    binding.btnRecordGrn.visibility = View.VISIBLE
                    binding.btnRecordGrn.setOnClickListener { onRecordGrnClick(po) }
                }
                PurchaseOrderStatus.PARTIALLY_DELIVERED -> {
                    binding.tvPoStatusBadge.text = "PARTIALLY DELIVERED"
                    binding.tvPoStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.cms_info))
                    binding.tvPoStatusBadge.backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.cms_info_tint))
                    binding.btnRecordGrn.visibility = View.VISIBLE
                    binding.btnRecordGrn.setOnClickListener { onRecordGrnClick(po) }
                }
                PurchaseOrderStatus.COMPLETED -> {
                    binding.tvPoStatusBadge.text = "COMPLETED"
                    binding.tvPoStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.cms_success))
                    binding.tvPoStatusBadge.backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.cms_success_tint))
                    binding.btnRecordGrn.visibility = View.GONE
                }
                PurchaseOrderStatus.DRAFT -> {
                    binding.tvPoStatusBadge.text = "DRAFT"
                    binding.tvPoStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.cms_text_secondary))
                    binding.tvPoStatusBadge.backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.cms_surface_bright))
                    binding.btnRecordGrn.visibility = View.GONE
                }
                PurchaseOrderStatus.CANCELLED -> {
                    binding.tvPoStatusBadge.text = "CANCELLED"
                    binding.tvPoStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.cms_error))
                    binding.tvPoStatusBadge.backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.cms_error_tint))
                    binding.btnRecordGrn.visibility = View.GONE
                }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<PurchaseOrder>() {
        override fun areItemsTheSame(oldItem: PurchaseOrder, newItem: PurchaseOrder): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PurchaseOrder, newItem: PurchaseOrder): Boolean {
            return oldItem == newItem
        }
    }
}

