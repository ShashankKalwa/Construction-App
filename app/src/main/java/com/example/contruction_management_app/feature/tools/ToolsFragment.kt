package com.example.contruction_management_app.feature.tools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.FragmentToolsBinding
import com.example.contruction_management_app.domain.model.CalculatorType

class ToolsFragment : Fragment() {

    private var _binding: FragmentToolsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentToolsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCards()
    }

    private fun setupCards() {
        binding.cardToolConcrete.setOnClickListener {
            openCalculator(CalculatorType.CONCRETE)
        }

        binding.cardToolSteel.setOnClickListener {
            openCalculator(CalculatorType.STEEL)
        }

        binding.cardToolBrickwork.setOnClickListener {
            openCalculator(CalculatorType.BRICKWORK)
        }

        binding.cardToolExcavation.setOnClickListener {
            openCalculator(CalculatorType.EXCAVATION)
        }

        binding.cardToolPlaster.setOnClickListener {
            openCalculator(CalculatorType.PLASTER)
        }

        binding.cardToolPaint.setOnClickListener {
            openCalculator(CalculatorType.PAINT)
        }

        binding.btnQuickUnitConverter.setOnClickListener {
            val converterDialog = UnitConverterDialogFragment()
            converterDialog.show(parentFragmentManager, UnitConverterDialogFragment.TAG)
        }
    }

    private fun openCalculator(type: CalculatorType) {
        val bundle = bundleOf(
            "calculatorType" to type.name,
            "projectId" to "prj_green_valley_residency"
        )
        try {
            findNavController().navigate(R.id.action_mainShell_to_calculatorDetail, bundle)
        } catch (_: Exception) {
            findNavController().navigate(R.id.calculatorDetailFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
