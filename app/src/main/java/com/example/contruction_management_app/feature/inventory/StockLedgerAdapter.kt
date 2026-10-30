package com.example.contruction_management_app.feature.inventory

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.ItemStockLedgerBinding
import com.example.contruction_management_app.domain.model.MovementType
import com.example.contruction_management_app.domain.model.StockMovement
import java.math.BigDecimal

class StockLedgerAdapter :
    ListAdapter<StockMovement, StockLedgerAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStockLedgerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemStockLedgerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(movement: StockMovement) {
            val context = binding.root.context
            binding.tvMaterialName.text = movement.materialName
            binding.tvMovementDate.text = movement.createdAt?.take(19)?.replace("T", " ") ?: ""

            val isPositive = movement.quantity >= BigDecimal.ZERO
            val qtyStr = if (isPositive) "+${movement.quantity}" else "${movement.quantity}"
            binding.tvQuantityChange.text = qtyStr

            binding.tvBalanceAfter.text = "Balance: ${movement.balanceAfter}"

            if (!movement.referenceId.isNullOrBlank()) {
                binding.tvReferenceId.visibility = View.VISIBLE
                binding.tvReferenceId.text = movement.referenceId
            } else {
                binding.tvReferenceId.visibility = View.GONE
            }

            if (!movement.notes.isNullOrBlank()) {
                binding.tvNotes.visibility = View.VISIBLE
                binding.tvNotes.text = "Notes: ${movement.notes}"
            } else {
                binding.tvNotes.visibility = View.GONE
            }

            binding.tvPerformedBy.text = "By ${movement.performedBy ?: "Authorized Personnel"}"

            when (movement.movementType) {
                MovementType.INWARD_GRN -> {
                    binding.tvMovementBadge.text = "INWARD (GRN)"
                    binding.tvMovementBadge.setTextColor(ContextCompat.getColor(context, R.color.cms_success))
                    binding.tvMovementBadge.backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.cms_success_tint))
                    binding.tvQuantityChange.setTextColor(ContextCompat.getColor(context, R.color.cms_success))
                }
                MovementType.OUTWARD_ISSUE -> {
                    binding.tvMovementBadge.text = "OUTWARD (ISSUE)"
                    binding.tvMovementBadge.setTextColor(ContextCompat.getColor(context, R.color.cms_accent))
                    binding.tvMovementBadge.backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.cms_accent_tint))
                    binding.tvQuantityChange.setTextColor(ContextCompat.getColor(context, R.color.cms_accent))
                }
                MovementType.RETURN -> {
                    binding.tvMovementBadge.text = "RETURN TO STORE"
                    binding.tvMovementBadge.setTextColor(ContextCompat.getColor(context, R.color.cms_info))
                    binding.tvMovementBadge.backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.cms_info_tint))
                    binding.tvQuantityChange.setTextColor(ContextCompat.getColor(context, R.color.cms_info))
                }
                MovementType.ADJUSTMENT -> {
                    binding.tvMovementBadge.text = "ADJUSTMENT"
                    binding.tvMovementBadge.setTextColor(ContextCompat.getColor(context, R.color.cms_warning))
                    binding.tvMovementBadge.backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.cms_warning_tint))
                    binding.tvQuantityChange.setTextColor(ContextCompat.getColor(context, R.color.cms_text_primary))
                }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<StockMovement>() {
        override fun areItemsTheSame(oldItem: StockMovement, newItem: StockMovement): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: StockMovement, newItem: StockMovement): Boolean {
            return oldItem == newItem
        }
    }
}

