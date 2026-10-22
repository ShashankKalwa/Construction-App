package com.example.contruction_management_app.feature.siteops

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.contruction_management_app.databinding.DialogCreateDsrBinding
import com.example.contruction_management_app.domain.model.LaborAttendance
import com.example.contruction_management_app.domain.model.LaborTrade
import com.example.contruction_management_app.domain.model.WeatherCondition
import java.math.BigDecimal

class CreateDsrDialog(
    private val onDsrCreated: (
        reportDate: String,
        weather: WeatherCondition,
        workSummary: String,
        plannedTomorrow: String?,
        laborAttendances: List<LaborAttendance>
    ) -> Unit
) : DialogFragment() {

    private var _binding: DialogCreateDsrBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogCreateDsrBinding.inflate(LayoutInflater.from(requireContext()))

        binding.btnCancelDsr.setOnClickListener { dismiss() }
        binding.btnSaveDsr.setOnClickListener {
            val date = binding.etDsrDate.text?.toString()?.trim().orEmpty()
            val weatherStr = binding.etDsrWeather.text?.toString()?.trim().orEmpty().uppercase()
            val weather = try {
                WeatherCondition.valueOf(weatherStr)
            } catch (_: Exception) {
                WeatherCondition.SUNNY
            }
            val summary = binding.etDsrWorkSummary.text?.toString()?.trim().orEmpty()
            val tomorrow = binding.etDsrTomorrow.text?.toString()?.trim()

            val masonsCount = binding.etLaborMasons.text?.toString()?.trim()?.toIntOrNull() ?: 0
            val steelCount = binding.etLaborSteelFixers.text?.toString()?.trim()?.toIntOrNull() ?: 0
            val carpCount = binding.etLaborCarpenters.text?.toString()?.trim()?.toIntOrNull() ?: 0
            val helperCount = binding.etLaborHelpers.text?.toString()?.trim()?.toIntOrNull() ?: 0

            val list = mutableListOf<LaborAttendance>()
            if (masonsCount > 0) list.add(LaborAttendance(trade = LaborTrade.MASON, headcount = masonsCount, hoursWorked = BigDecimal("8.0")))
            if (steelCount > 0) list.add(LaborAttendance(trade = LaborTrade.STEEL_FIXER, headcount = steelCount, hoursWorked = BigDecimal("8.0")))
            if (carpCount > 0) list.add(LaborAttendance(trade = LaborTrade.CARPENTER, headcount = carpCount, hoursWorked = BigDecimal("8.0")))
            if (helperCount > 0) list.add(LaborAttendance(trade = LaborTrade.HELPER, headcount = helperCount, hoursWorked = BigDecimal("8.0")))

            if (date.isNotBlank() && summary.isNotBlank()) {
                onDsrCreated(date, weather, summary, tomorrow, list)
                dismiss()
            } else {
                if (summary.isBlank()) binding.tilDsrWorkSummary.error = "Summary required"
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CreateDsrDialog"
    }
}
