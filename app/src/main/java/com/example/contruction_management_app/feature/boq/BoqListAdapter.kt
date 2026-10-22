package com.example.contruction_management_app.feature.boq

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.ItemBoqBinding
import com.example.contruction_management_app.domain.model.Boq
import com.example.contruction_management_app.domain.model.BoqStatus

class BoqListAdapter(
    private val onBoqClicked: (Boq) -> Unit
) : ListAdapter<Boq, BoqListAdapter.BoqViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoqViewHolder {
        val binding = ItemBoqBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BoqViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BoqViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BoqViewHolder(private val binding: ItemBoqBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(boq: Boq) {
            val context = binding.root.context
            binding.tvBoqVersion.text = "v${boq.version}.0"
            binding.tvBoqStatus.text = boq.status.name
            binding.tvBoqTitle.text = boq.title
            binding.tvBoqTotalAmount.text = boq.formatTotalAmount()
            binding.tvBoqItemsCount.text = "${boq.items.size} Items"
            binding.tvBoqCreatedBy.text = boq.createdBy?.let { "By $it" } ?: "By Team"

            val statusColorRes = when (boq.status) {
                BoqStatus.APPROVED -> R.color.cms_success
                BoqStatus.SUBMITTED -> R.color.cms_info
                BoqStatus.DRAFT -> R.color.cms_warning
                BoqStatus.REJECTED -> R.color.cms_error
            }
            binding.tvBoqStatus.setTextColor(ContextCompat.getColor(context, statusColorRes))

            binding.root.setOnClickListener {
                onBoqClicked(boq)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Boq>() {
        override fun areItemsTheSame(oldItem: Boq, newItem: Boq): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Boq, newItem: Boq): Boolean = oldItem == newItem
    }
}
