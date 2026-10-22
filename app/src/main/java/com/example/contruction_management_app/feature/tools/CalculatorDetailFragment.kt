package com.example.contruction_management_app.feature.tools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.FragmentCalculatorDetailBinding
import com.example.contruction_management_app.domain.model.*
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import java.util.Locale

class CalculatorDetailFragment : Fragment() {

    private var _binding: FragmentCalculatorDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CalculatorViewModel by viewModels()
    private lateinit var historyAdapter: CalculationHistoryAdapter

    private var currentType = CalculatorType.CONCRETE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalculatorDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val typeArg = arguments?.getString("calculatorType") ?: "CONCRETE"
        val projectArg = arguments?.getString("projectId")

        currentType = try {
            CalculatorType.valueOf(typeArg)
        } catch (_: Exception) {
            CalculatorType.CONCRETE
        }

        viewModel.setCalculatorType(currentType)
        viewModel.setProjectId(projectArg)

        setupToolbar()
        setupTabs()
        setupRecyclerView()
        configureCalculatorUi(currentType)
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.title = currentType.displayName
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupTabs() {
        binding.tabLayoutCalc.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        binding.layoutTabCalculator.visibility = View.VISIBLE
                        binding.layoutTabHistory.visibility = View.GONE
                        binding.layoutTabStandards.visibility = View.GONE
                    }
                    1 -> {
                        binding.layoutTabCalculator.visibility = View.GONE
                        binding.layoutTabHistory.visibility = View.VISIBLE
                        binding.layoutTabStandards.visibility = View.GONE
                        viewModel.loadHistory()
                    }
                    2 -> {
                        binding.layoutTabCalculator.visibility = View.GONE
                        binding.layoutTabHistory.visibility = View.GONE
                        binding.layoutTabStandards.visibility = View.VISIBLE
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        historyAdapter = CalculationHistoryAdapter { item ->
            Toast.makeText(requireContext(), "Selected: ${item.title}", Toast.LENGTH_SHORT).show()
        }
        binding.rvCalculationHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCalculationHistory.adapter = historyAdapter
    }

    private fun configureCalculatorUi(type: CalculatorType) {
        binding.tvStandardBadge.text = type.standardCode
        binding.tvUnitBadge.text = "Primary Unit: ${type.defaultUnit}"
        binding.tvCalcTitle.text = type.displayName

        binding.sliderWastage.addOnChangeListener { _, value, _ ->
            binding.tvWastageValue.text = "${value.toInt()}%"
        }

        when (type) {
            CalculatorType.CONCRETE -> {
                binding.tvCalcSubtitle.text = "Wet and dry volume (factor 1.54), cement bags (50kg), coarse sand, aggregate, and water."
                binding.tilInput1.hint = "Length (m)"
                binding.etInput1.setText("10.0")
                binding.tilInput2.hint = "Width (m)"
                binding.etInput2.setText("5.0")
                binding.tilInput3.hint = "Depth / Thickness (m)"
                binding.etInput3.setText("0.15")
                binding.tilInput4.hint = "Deductions (sq.m)"
                binding.etInput4.setText("0.0")
                binding.tvGradeSelectorLabel.text = "CONCRETE MIX GRADE"
                binding.chipGrade1.text = "M15 (1:2:4)"
                binding.chipGrade2.text = "M20 (1:1.5:3)"
                binding.chipGrade3.text = "M25 (1:1:2)"
                binding.chipGrade2.isChecked = true
                binding.etSaveTitle.setText("Tower A Ground Floor Slab M20")

                binding.tvStandardCodeTitle.text = "IS 456:2000 Code Provisions"
                binding.tvStandardFormulaDetails.text = "1. Dry Volume Factor: 1.54 accounts for internal voids and dry compaction.\n2. Cement Density: 1,440 kg/m³ (50 kg per bag).\n3. Coarse Sand Density: 1,600 kg/m³ (1.60 t/m³).\n4. 20mm/10mm Metal Density: 1,500 kg/m³ (1.50 t/m³).\n5. Water-Cement Ratio: ~0.50 (25L water per 50kg bag)."
            }
            CalculatorType.STEEL -> {
                binding.tvCalcSubtitle.text = "Standard unit weight formula W = D²/162 kg/m, bar bundle packaging, cutting length."
                binding.tilInput1.hint = "Bar Diameter (mm)"
                binding.etInput1.setText("16")
                binding.tilInput2.hint = "Length per Bar (m)"
                binding.etInput2.setText("12.0")
                binding.tilInput3.hint = "Number of Bars"
                binding.etInput3.setText("48")
                binding.tilInput4.hint = "Bend / Hook Allowance (m)"
                binding.etInput4.setText("0.0")
                binding.tvGradeSelectorLabel.text = "COMMON DIAMETERS (mm)"
                binding.chipGrade1.text = "12 mm"
                binding.chipGrade2.text = "16 mm"
                binding.chipGrade3.text = "20 mm"
                binding.chipGrade2.isChecked = true
                binding.etSaveTitle.setText("Main Columns 16mm Rebar Takeoff")

                binding.tvStandardCodeTitle.text = "IS 1786 / SP 34 Reinforcement Standard"
                binding.tvStandardFormulaDetails.text = "1. Rebar Unit Weight: W = D²/162.0 kg per meter of length.\n2. Standard delivery commercial bar length: 12.0 meters.\n3. Standard Bundle Counts: 8mm (12 bars), 10mm (8 bars), 12mm (6 bars), 16mm (4 bars), 20mm (2 bars).\n4. Recommended lap allowance: 40D to 50D for tension splices."
            }
            CalculatorType.BRICKWORK -> {
                binding.tvCalcSubtitle.text = "9-inch & 4.5-inch masonry walls, door/window deductions, mortar mix (1:4, 1:6)."
                binding.tilInput1.hint = "Wall Length (m)"
                binding.etInput1.setText("30.0")
                binding.tilInput2.hint = "Wall Height (m)"
                binding.etInput2.setText("3.0")
                binding.tilInput3.hint = "Wall Thickness (m)"
                binding.etInput3.setText("0.23")
                binding.tilInput4.hint = "Openings / Deductions (sq.m)"
                binding.etInput4.setText("4.5")
                binding.tvGradeSelectorLabel.text = "CEMENT-SAND MORTAR RATIO"
                binding.chipGrade1.text = "1:4 Mix"
                binding.chipGrade2.text = "1:6 Mix"
                binding.chipGrade3.text = "1:3 Mix"
                binding.chipGrade2.isChecked = true
                binding.etSaveTitle.setText("Block A Perimeter 9-inch Brick Wall")

                binding.tvStandardCodeTitle.text = "IS 2212 / CPWD Brickwork Standards"
                binding.tvStandardFormulaDetails.text = "1. Standard Modular Brick Size with Mortar: 200 x 100 x 100 mm (500 bricks per m³ of wall).\n2. Wet Mortar Volume: 30% of gross wall masonry volume.\n3. Dry Mortar Volume Factor: 1.33 (accounts for voids and frog filling).\n4. 1:6 Mix Cement Consumption: ~1.05 to 1.10 bags per m³ of masonry."
            }
            CalculatorType.EXCAVATION -> {
                binding.tvCalcSubtitle.text = "Pit & trench earthwork, soil bulking/swell factor (1.25), compaction, and tipper trips."
                binding.tilInput1.hint = "Pit Length (m)"
                binding.etInput1.setText("20.0")
                binding.tilInput2.hint = "Pit Width (m)"
                binding.etInput2.setText("12.0")
                binding.tilInput3.hint = "Excavation Depth (m)"
                binding.etInput3.setText("2.5")
                binding.tilInput4.hint = "Truck Capacity (m³)"
                binding.etInput4.setText("10.0")
                binding.tvGradeSelectorLabel.text = "SOIL SWELL / BULKING"
                binding.chipGrade1.text = "Soft Soil (1.20)"
                binding.chipGrade2.text = "Hard Soil (1.25)"
                binding.chipGrade3.text = "Rock (1.35)"
                binding.chipGrade2.isChecked = true
                binding.etSaveTitle.setText("Tower A Foundation Earthwork Excavation")

                binding.tvStandardCodeTitle.text = "IS 1200 Part 1: Earthwork Measurement"
                binding.tvStandardFormulaDetails.text = "1. In-situ Pit Volume: Length x Width x Depth in cubic meters.\n2. Loose / Bulked Volume: Pit Volume x 1.25 swell factor.\n3. Compacted Backfill Volume: Pit Volume x 0.85 compaction factor.\n4. Haulage Trips: Loose volume divided by tipper truck rated volume."
            }
            CalculatorType.PLASTER -> {
                binding.tvCalcSubtitle.text = "12mm, 15mm, and 20mm internal and external plaster mortar takeoff."
                binding.tilInput1.hint = "Wall Length (m)"
                binding.etInput1.setText("20.0")
                binding.tilInput2.hint = "Wall Height (m)"
                binding.etInput2.setText("3.0")
                binding.tilInput3.hint = "Thickness (mm)"
                binding.etInput3.setText("12.0")
                binding.tilInput4.hint = "Deductions (sq.m)"
                binding.etInput4.setText("0.0")
                binding.tvGradeSelectorLabel.text = "PLASTER MORTAR RATIO"
                binding.chipGrade1.text = "1:3 Mix"
                binding.chipGrade2.text = "1:4 Mix"
                binding.chipGrade3.text = "1:6 Mix"
                binding.chipGrade2.isChecked = true
                binding.etSaveTitle.setText("Internal Wall 12mm Smooth Plaster")

                binding.tvStandardCodeTitle.text = "IS 1661: Code for Cement Plaster"
                binding.tvStandardFormulaDetails.text = "1. Surface Area: (Length x Height) minus door/window deductions.\n2. Wet Mortar Volume: Surface Area x (Thickness / 1000).\n3. Dry Factor: 1.30 to account for masonry joints, uneven brick surfaces, and drying shrinkage."
            }
            CalculatorType.PAINT -> {
                binding.tvCalcSubtitle.text = "Room wall + ceiling surface area, primer coat, finish coats, and container sizes."
                binding.tilInput1.hint = "Room Length (m)"
                binding.etInput1.setText("5.0")
                binding.tilInput2.hint = "Room Width (m)"
                binding.etInput2.setText("4.0")
                binding.tilInput3.hint = "Ceiling Height (m)"
                binding.etInput3.setText("3.0")
                binding.tilInput4.hint = "Openings / Deductions (sq.m)"
                binding.etInput4.setText("4.0")
                binding.tvGradeSelectorLabel.text = "NUMBER OF FINISH COATS"
                binding.chipGrade1.text = "1 Coat"
                binding.chipGrade2.text = "2 Coats"
                binding.chipGrade3.text = "3 Coats"
                binding.chipGrade2.isChecked = true
                binding.etSaveTitle.setText("Master Bedroom Premium Emulsion Painting")

                binding.tvStandardCodeTitle.text = "CPWD DSR 2023 Painting Provisions"
                binding.tvStandardFormulaDetails.text = "1. Wall Area: 2 x (L + W) x H minus deductions.\n2. Ceiling Area: L x W (included by default).\n3. Primer Coverage: ~12.0 sq.m per liter.\n4. Paint Spreading Rate: ~10.0 sq.m per liter per coat."
            }
            CalculatorType.UNIT_CONVERT -> {
                // Not reached as converter is opened via dialog
            }
        }
    }

    private fun setupListeners() {
        binding.btnCalculate.setOnClickListener {
            executeCalculation()
        }

        binding.btnReset.setOnClickListener {
            configureCalculatorUi(currentType)
            binding.cardResult.visibility = View.GONE
        }

        binding.btnSaveCalculation.setOnClickListener {
            val title = binding.etSaveTitle.text?.toString()?.trim()
            if (title.isNullOrBlank()) {
                Toast.makeText(requireContext(), "Please enter a calculation title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val selectedChipId = binding.chipGroupElements.checkedChipId
            val elementChip = binding.chipGroupElements.findViewById<Chip>(selectedChipId)
            val elementType = elementChip?.text?.toString() ?: "GENERAL"
            viewModel.saveCurrentCalculation(title, elementType)
        }
    }

    private fun executeCalculation() {
        val in1 = binding.etInput1.text?.toString()?.toDoubleOrNull()
        val in2 = binding.etInput2.text?.toString()?.toDoubleOrNull()
        val in3 = binding.etInput3.text?.toString()?.toDoubleOrNull()
        val in4 = binding.etInput4.text?.toString()?.toDoubleOrNull() ?: 0.0
        val wastage = binding.sliderWastage.value.toDouble()

        if (in1 == null || in2 == null || in3 == null || in1 <= 0 || in2 <= 0 || in3 <= 0) {
            Toast.makeText(requireContext(), "Please enter valid positive dimensions", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedChipId = binding.chipGroupElements.checkedChipId
        val elementChip = binding.chipGroupElements.findViewById<Chip>(selectedChipId)
        val elementType = elementChip?.text?.toString() ?: "GENERAL"

        when (currentType) {
            CalculatorType.CONCRETE -> {
                val grade = if (binding.chipGrade1.isChecked) "M15" else (if (binding.chipGrade3.isChecked) "M25" else "M20")
                viewModel.calculateConcrete(in1, in2, in3, grade, wastage, elementType)
            }
            CalculatorType.STEEL -> {
                val count = in3.toInt()
                viewModel.calculateSteel(in1, in2, count, wastage, elementType)
            }
            CalculatorType.BRICKWORK -> {
                val ratio = if (binding.chipGrade1.isChecked) "1:4" else "1:6"
                viewModel.calculateBrickwork(in1, in2, in3, in4, ratio, wastage)
            }
            CalculatorType.EXCAVATION -> {
                val bulking = if (binding.chipGrade1.isChecked) 1.20 else (if (binding.chipGrade3.isChecked) 1.35 else 1.25)
                val truckCap = if (in4 > 0) in4 else 10.0
                viewModel.calculateExcavation(in1, in2, in3, bulking, 0.85, truckCap)
            }
            CalculatorType.PLASTER -> {
                val ratio = if (binding.chipGrade1.isChecked) "1:3" else (if (binding.chipGrade3.isChecked) "1:6" else "1:4")
                viewModel.calculatePlaster(in1, in2, in3, in4, ratio, wastage)
            }
            CalculatorType.PAINT -> {
                val coats = if (binding.chipGrade1.isChecked) 1 else (if (binding.chipGrade3.isChecked) 3 else 2)
                viewModel.calculatePaint(in1, in2, in3, in4, true, coats, true, 10.0, wastage)
            }
            CalculatorType.UNIT_CONVERT -> {}
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.activeResult.collect { result ->
                        if (result != null) {
                            displayResult(result)
                        }
                    }
                }
                launch {
                    viewModel.historyList.collect { list ->
                        historyAdapter.submitList(list)
                        binding.tvHistoryEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.userMessage.collect { msg ->
                        if (msg != null) {
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                            viewModel.clearUserMessage()
                        }
                    }
                }
            }
        }
    }

    private fun displayResult(res: Any) {
        binding.cardResult.visibility = View.VISIBLE

        when (res) {
            is ConcreteResult -> {
                binding.tvPrimaryResult.text = String.format(Locale.US, "%.2f m³", res.wetVolumeM3)
                binding.tvDryVolumeResult.text = String.format(
                    Locale.US,
                    "Dry Volume (x1.54 + %.0f%% wastage): %.2f m³ • Grade %s (%s)",
                    res.wastagePct, res.dryVolumeM3, res.grade, res.mixRatio
                )
                binding.tvMaterialBreakdown.text = String.format(
                    Locale.US,
                    "• Cement Bags (50kg): %.1f Bags (%.0f kg)\n• Coarse Sand: %.2f Tonnes (%.1f CFT)\n• Coarse Aggregate (20mm): %.2f Tonnes (%.1f CFT)\n• Water Required: ~%.0f Liters",
                    res.cementBags, res.cementKg, res.sandTonnes, res.sandCft,
                    res.aggregateTonnes, res.aggregateCft, res.waterLiters
                )
            }
            is SteelResult -> {
                binding.tvPrimaryResult.text = String.format(Locale.US, "%.2f kg", res.totalWeightKg)
                binding.tvDryVolumeResult.text = String.format(
                    Locale.US,
                    "Total Rebar: %.3f Tonnes • %d Bars of %.1fm (Total: %.1fm)",
                    res.totalWeightTonnes, res.numberOfBars, res.lengthPerBarM, res.totalLengthM
                )
                binding.tvMaterialBreakdown.text = String.format(
                    Locale.US,
                    "• Unit Weight (D²/162): %.3f kg/m (for %.0fmm bar)\n• Raw Weight: %.2f kg\n• Total with %.0f%% Wastage: %.2f kg\n• Bundles Required (12m commercial): %d Bundles",
                    res.unitWeightKgPerM, res.diameterMm, res.rawWeightKg,
                    res.wastagePct, res.totalWeightKg, res.bundleCount
                )
            }
            is BrickworkResult -> {
                binding.tvPrimaryResult.text = String.format(Locale.US, "%.0f Bricks", res.brickCount)
                binding.tvDryVolumeResult.text = String.format(
                    Locale.US,
                    "Net Masonry Volume: %.2f m³ (Gross: %.2f m³ - Openings: %.2f m³)",
                    res.netWallVolumeM3, res.grossWallVolumeM3, res.deductionsVolumeM3
                )
                binding.tvMaterialBreakdown.text = String.format(
                    Locale.US,
                    "• Bricks Required (modular 500/m³): %.0f Bricks\n• Dry Mortar Volume: %.2f m³ (%s Mix)\n• Cement Required: %.1f Bags (%.0f kg)\n• Coarse Sand: %.2f Tonnes (%.1f CFT)",
                    res.brickCount, res.mortarDryVolumeM3, res.mortarRatio,
                    res.cementBags, res.cementKg, res.sandTonnes, res.sandCft
                )
            }
            is ExcavationResult -> {
                binding.tvPrimaryResult.text = String.format(Locale.US, "%.2f m³", res.pitVolumeM3)
                binding.tvDryVolumeResult.text = String.format(
                    Locale.US,
                    "Bulked Loose Volume (x%.2f): %.2f m³ • Backfill Volume: %.2f m³",
                    res.bulkingFactor, res.looseVolumeM3, res.compactedBackfillM3
                )
                binding.tvMaterialBreakdown.text = String.format(
                    Locale.US,
                    "• In-Situ Pit Volume: %.2f m³\n• Loose Soil for Haulage: %.2f m³\n• Compacted Backfill Required: %.2f m³\n• Dump Truck Trips Required (%.0f m³ Tipper): %d Trips",
                    res.pitVolumeM3, res.looseVolumeM3, res.compactedBackfillM3,
                    res.truckCapacityM3, res.truckTripsRequired
                )
            }
            is PlasterResult -> {
                binding.tvPrimaryResult.text = String.format(Locale.US, "%.2f m²", res.surfaceAreaSqM)
                binding.tvDryVolumeResult.text = String.format(
                    Locale.US,
                    "Plaster Mortar: Wet %.2f m³ • Dry %.2f m³ (Dry factor 1.30, %s Mix)",
                    res.wetVolumeM3, res.dryVolumeM3, res.mortarRatio
                )
                binding.tvMaterialBreakdown.text = String.format(
                    Locale.US,
                    "• Surface Area Covered: %.2f m²\n• Cement Required: %.1f Bags (%.0f kg)\n• Sand Required: %.2f Tonnes (%.1f CFT)",
                    res.surfaceAreaSqM, res.cementBags, res.cementKg, res.sandTonnes, res.sandCft
                )
            }
            is PaintResult -> {
                binding.tvPrimaryResult.text = String.format(Locale.US, "%.2f Liters", res.paintLiters)
                binding.tvDryVolumeResult.text = String.format(
                    Locale.US,
                    "Total Surface: %.2f m² (Walls: %.2f m² + Ceiling: %.2f m²) • %d Coats",
                    res.totalAreaSqM, res.wallAreaSqM, res.ceilingAreaSqM, res.numberOfCoats
                )
                binding.tvMaterialBreakdown.text = String.format(
                    Locale.US,
                    "• Primer Coat Required: %.2f Liters\n• Finish Emulsion Required: %.2f Liters\n• Container Breakdown: %d x 20L Drum, %d x 4L Tin, %d x 1L Can",
                    res.primerLiters, res.paintLiters, res.cans20L, res.cans4L, res.cans1L
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
