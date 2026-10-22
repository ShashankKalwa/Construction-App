package com.example.contruction_management_app.feature.tools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.DialogUnitConverterBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

class UnitConverterDialogFragment : BottomSheetDialogFragment() {

    private var _binding: DialogUnitConverterBinding? = null
    private val binding get() = _binding!!

    private var currentCategory = "LINEAR"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogUnitConverterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategoryChips()
        setupUnitDropdowns()
        setupConvertButton()
        performConversion()

        binding.btnDismissConverter.setOnClickListener {
            dismiss()
        }
    }

    private fun setupCategoryChips() {
        binding.chipGroupCategory.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            currentCategory = when (checkedIds.first()) {
                R.id.chipLinear -> "LINEAR"
                R.id.chipArea -> "AREA"
                R.id.chipVolume -> "VOLUME"
                R.id.chipMass -> "MASS"
                else -> "LINEAR"
            }
            setupUnitDropdowns()
            performConversion()
        }
    }

    private fun setupUnitDropdowns() {
        val units = when (currentCategory) {
            "LINEAR" -> listOf("Meter (m)", "Feet (ft)", "Inches (in)", "Millimeter (mm)", "Centimeter (cm)", "Yard (yd)")
            "AREA" -> listOf("Square Meter (m²)", "Square Feet (sq.ft)", "Brass (100 sq.ft)", "Acre", "Guntha (1089 sq.ft)", "Hectare")
            "VOLUME" -> listOf("Cubic Meter (m³)", "Cubic Feet (cft)", "Brass (100 cft)", "Liters (L)", "Gallons (gal)")
            "MASS" -> listOf("Kilogram (kg)", "Tonne (MT)", "Quintal (100 kg)", "Pounds (lbs)")
            else -> listOf("Meter (m)", "Feet (ft)")
        }

        val adapterFrom = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, units)
        val adapterTo = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, units)

        binding.actvFromUnit.setAdapter(adapterFrom)
        binding.actvToUnit.setAdapter(adapterTo)

        binding.actvFromUnit.setText(units[0], false)
        binding.actvToUnit.setText(if (units.size > 1) units[1] else units[0], false)

        binding.actvFromUnit.setOnItemClickListener { _, _, _, _ -> performConversion() }
        binding.actvToUnit.setOnItemClickListener { _, _, _, _ -> performConversion() }
    }

    private fun setupConvertButton() {
        binding.btnConvertExecute.setOnClickListener {
            performConversion()
        }
    }

    private fun performConversion() {
        val inputVal = binding.etConvertValue.text?.toString()?.toDoubleOrNull() ?: 1.0
        val fromText = binding.actvFromUnit.text?.toString() ?: ""
        val toText = binding.actvToUnit.text?.toString() ?: ""

        val (result, breakdown) = when (currentCategory) {
            "LINEAR" -> convertLinear(inputVal, fromText, toText)
            "AREA" -> convertArea(inputVal, fromText, toText)
            "VOLUME" -> convertVolume(inputVal, fromText, toText)
            "MASS" -> convertMass(inputVal, fromText, toText)
            else -> Pair(inputVal, "")
        }

        binding.tvConversionResult.text = String.format(
            Locale.US,
            "%.2f %s = %.4f %s",
            inputVal, extractCode(fromText), result, extractCode(toText)
        )
        binding.tvConversionBreakdown.text = breakdown
    }

    private fun convertLinear(value: Double, from: String, to: String): Pair<Double, String> {
        val baseM = when {
            from.contains("Feet") -> value * 0.3048
            from.contains("Inches") -> value * 0.0254
            from.contains("Millimeter") -> value * 0.001
            from.contains("Centimeter") -> value * 0.01
            from.contains("Yard") -> value * 0.9144
            else -> value
        }
        val target = when {
            to.contains("Feet") -> baseM / 0.3048
            to.contains("Inches") -> baseM / 0.0254
            to.contains("Millimeter") -> baseM / 0.001
            to.contains("Centimeter") -> baseM / 0.01
            to.contains("Yard") -> baseM / 0.9144
            else -> baseM
        }
        val breakdown = String.format(
            Locale.US,
            "Equivalents: %.2f m • %.2f ft • %.1f in • %.0f mm",
            baseM, baseM / 0.3048, baseM / 0.0254, baseM / 0.001
        )
        return Pair(round4(target), breakdown)
    }

    private fun convertArea(value: Double, from: String, to: String): Pair<Double, String> {
        val baseSqM = when {
            from.contains("Square Feet") -> value * 0.092903
            from.contains("Brass") -> value * 9.2903
            from.contains("Acre") -> value * 4046.86
            from.contains("Guntha") -> value * 101.171
            from.contains("Hectare") -> value * 10000.0
            else -> value
        }
        val target = when {
            to.contains("Square Feet") -> baseSqM / 0.092903
            to.contains("Brass") -> baseSqM / 9.2903
            to.contains("Acre") -> baseSqM / 4046.86
            to.contains("Guntha") -> baseSqM / 101.171
            to.contains("Hectare") -> baseSqM / 10000.0
            else -> baseSqM
        }
        val breakdown = String.format(
            Locale.US,
            "Equivalents: %.2f m² • %.2f sq.ft • %.2f Brass • %.4f Acre",
            baseSqM, baseSqM / 0.092903, baseSqM / 9.2903, baseSqM / 4046.86
        )
        return Pair(round4(target), breakdown)
    }

    private fun convertVolume(value: Double, from: String, to: String): Pair<Double, String> {
        val baseCuM = when {
            from.contains("Cubic Feet") -> value * 0.0283168
            from.contains("Brass") -> value * 2.83168
            from.contains("Liters") -> value * 0.001
            from.contains("Gallons") -> value * 0.00378541
            else -> value
        }
        val target = when {
            to.contains("Cubic Feet") -> baseCuM / 0.0283168
            to.contains("Brass") -> baseCuM / 2.83168
            to.contains("Liters") -> baseCuM / 0.001
            to.contains("Gallons") -> baseCuM / 0.00378541
            else -> baseCuM
        }
        val breakdown = String.format(
            Locale.US,
            "Equivalents: %.2f m³ • %.2f CFT • %.2f Brass • %.0f Liters",
            baseCuM, baseCuM / 0.0283168, baseCuM / 2.83168, baseCuM / 0.001
        )
        return Pair(round4(target), breakdown)
    }

    private fun convertMass(value: Double, from: String, to: String): Pair<Double, String> {
        val baseKg = when {
            from.contains("Tonne") -> value * 1000.0
            from.contains("Quintal") -> value * 100.0
            from.contains("Pounds") -> value * 0.45359237
            else -> value
        }
        val target = when {
            to.contains("Tonne") -> baseKg / 1000.0
            to.contains("Quintal") -> baseKg / 100.0
            to.contains("Pounds") -> baseKg / 0.45359237
            else -> baseKg
        }
        val breakdown = String.format(
            Locale.US,
            "Equivalents: %.2f kg • %.3f Tonnes • %.2f Quintals • %.1f lbs",
            baseKg, baseKg / 1000.0, baseKg / 100.0, baseKg / 0.45359237
        )
        return Pair(round4(target), breakdown)
    }

    private fun extractCode(unitName: String): String {
        val start = unitName.indexOf('(')
        val end = unitName.indexOf(')')
        return if (start != -1 && end != -1) unitName.substring(start + 1, end) else unitName
    }

    private fun round4(v: Double): Double = BigDecimal.valueOf(v).setScale(4, RoundingMode.HALF_UP).toDouble()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "UnitConverterDialogFragment"
    }
}
