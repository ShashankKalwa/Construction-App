package com.example.contruction_management_app.feature.boq

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contruction_management_app.databinding.ItemBoqItemBinding
import com.example.contruction_management_app.domain.model.BoqItem
import java.math.RoundingMode

class BoqItemAdapter(
    private val onItemClicked: ((BoqItem) -> Unit)? = null
) : ListAdapter<BoqItem, BoqItemAdapter.BoqItemViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoqItemViewHolder {
        val binding = ItemBoqItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BoqItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BoqItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BoqItemViewHolder(private val binding: ItemBoqItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BoqItem) {
            binding.tvItemCode.text = item.itemCode
            binding.tvItemCategory.text = item.category
            binding.tvItemDescription.text = item.description
            binding.tvItemTotalAmount.text = item.formatAmount()

            val qtyFormatted = item.estimatedQuantity.setScale(2, RoundingMode.HALF_UP).toPlainString()
            binding.tvItemQuantity.text = "Qty: $qtyFormatted ${item.unit}"
            binding.tvItemUnitRate.text = "Rate: ${item.formatRate()}"

            binding.root.setOnClickListener {
                onItemClicked?.invoke(item)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<BoqItem>() {
        override fun areItemsTheSame(oldItem: BoqItem, newItem: BoqItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: BoqItem, newItem: BoqItem): Boolean = oldItem == newItem
    }
}
