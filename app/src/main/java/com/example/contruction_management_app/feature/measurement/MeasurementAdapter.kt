package com.example.contruction_management_app.feature.measurement

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contruction_management_app.databinding.ItemMeasurementBinding
import com.example.contruction_management_app.domain.model.Measurement
import java.math.BigDecimal

class MeasurementAdapter(
    private val onItemClicked: ((Measurement) -> Unit)? = null
) : ListAdapter<Measurement, MeasurementAdapter.MeasurementViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeasurementViewHolder {
        val binding = ItemMeasurementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MeasurementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MeasurementViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MeasurementViewHolder(private val binding: ItemMeasurementBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(m: Measurement) {
            binding.tvMbDescription.text = m.itemDescription
            binding.tvMbDimensions.text = m.formatDimensions()
            binding.tvMbNetQuantity.text = m.formatNetQuantity()

            if (m.deductionM3 > BigDecimal.ZERO) {
                binding.tvMbDeduction.visibility = View.VISIBLE
                binding.tvMbDeduction.text = "Ded: ${m.deductionM3.stripTrailingZeros().toPlainString()} ${m.unit}"
            } else {
                binding.tvMbDeduction.visibility = View.GONE
            }

            binding.tvMbLocation.text = m.locationDetails ?: "Site Location"
            binding.tvMbMeasuredBy.text = m.measuredBy?.let { "By $it" } ?: "By Engineer"

            binding.root.setOnClickListener {
                onItemClicked?.invoke(m)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Measurement>() {
        override fun areItemsTheSame(oldItem: Measurement, newItem: Measurement): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Measurement, newItem: Measurement): Boolean =
            oldItem == newItem
    }
}
