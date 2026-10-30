package com.example.contruction_management_app.feature.siteops

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.ItemDailyReportBinding
import com.example.contruction_management_app.domain.model.DailySiteReport
import com.example.contruction_management_app.domain.model.DsrStatus
import com.example.contruction_management_app.domain.model.WeatherCondition

class DailyReportAdapter(
    private val onReportClicked: ((DailySiteReport) -> Unit)? = null,
    private val onApproveClicked: ((DailySiteReport) -> Unit)? = null
) : ListAdapter<DailySiteReport, DailyReportAdapter.ReportViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemDailyReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReportViewHolder(private val binding: ItemDailyReportBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(report: DailySiteReport) {
            binding.tvDsrDate.text = report.reportDate

            val weatherEmoji = when (report.weather) {
                WeatherCondition.SUNNY -> "SUNNY ☀️"
                WeatherCondition.RAINY -> "RAINY 🌧️"
                WeatherCondition.CLOUDY -> "CLOUDY ⛅"
                WeatherCondition.STORMY -> "STORMY ⛈️"
                WeatherCondition.WINDY -> "WINDY 💨"
            }
            binding.tvDsrWeather.text = weatherEmoji
            binding.tvDsrHeadcount.text = "${report.totalHeadcount} Workers"
            binding.tvDsrWorkSummary.text = report.workSummary

            // Status Badge
            binding.tvDsrStatusBadge.text = report.status.name
            when (report.status) {
                DsrStatus.APPROVED -> {
                    binding.tvDsrStatusBadge.setTextColor(ContextCompat.getColor(binding.root.context, R.color.cms_success))
                    binding.btnApproveDsr.visibility = View.GONE
                }
                DsrStatus.SUBMITTED -> {
                    binding.tvDsrStatusBadge.setTextColor(ContextCompat.getColor(binding.root.context, R.color.cms_accent))
                    binding.btnApproveDsr.visibility = View.VISIBLE
                }
                DsrStatus.REJECTED -> {
                    binding.tvDsrStatusBadge.setTextColor(ContextCompat.getColor(binding.root.context, R.color.cms_error))
                    binding.btnApproveDsr.visibility = View.GONE
                }
                DsrStatus.DRAFT -> {
                    binding.tvDsrStatusBadge.setTextColor(ContextCompat.getColor(binding.root.context, R.color.cms_text_tertiary))
                    binding.btnApproveDsr.visibility = View.GONE
                }
            }

            if (!report.approvalNotes.isNullOrBlank() || !report.approvedBy.isNullOrBlank()) {
                binding.layoutApprovalNotes.visibility = View.VISIBLE
                val approver = report.approvedBy ?: "Manager"
                val notes = report.approvalNotes ?: "Approved"
                binding.tvDsrApprovalDetails.text = "Reviewed by $approver: $notes"
            } else {
                binding.layoutApprovalNotes.visibility = View.GONE
            }

            if (!report.plannedTomorrow.isNullOrBlank()) {
                binding.layoutTomorrow.visibility = View.VISIBLE
                binding.tvDsrPlannedTomorrow.text = report.plannedTomorrow
            } else {
                binding.layoutTomorrow.visibility = View.GONE
            }

            if (report.laborAttendances.isNotEmpty()) {
                val roster = report.laborAttendances.joinToString(" • ") {
                    "${it.headcount} ${it.trade.name.replace("_", " ")}"
                }
                binding.tvDsrTradesRoster.visibility = View.VISIBLE
                binding.tvDsrTradesRoster.text = "Roster: $roster"
            } else {
                binding.tvDsrTradesRoster.visibility = View.GONE
            }

            binding.tvDsrCreatedBy.text = report.createdBy?.let { "Reported by $it" } ?: "Reported by Site Team"

            binding.btnApproveDsr.setOnClickListener {
                onApproveClicked?.invoke(report)
            }

            binding.root.setOnClickListener {
                onReportClicked?.invoke(report)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<DailySiteReport>() {
        override fun areItemsTheSame(oldItem: DailySiteReport, newItem: DailySiteReport): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: DailySiteReport, newItem: DailySiteReport): Boolean =
            oldItem == newItem
    }
}
