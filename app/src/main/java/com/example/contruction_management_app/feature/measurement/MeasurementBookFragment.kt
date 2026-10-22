package com.example.contruction_management_app.feature.measurement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.FragmentMeasurementBookBinding
import kotlinx.coroutines.launch

class MeasurementBookFragment : Fragment() {

    private var _binding: FragmentMeasurementBookBinding? = null
    private val binding get() = _binding!!

    private val measurementViewModel: MeasurementViewModel by activityViewModels()
    private var projectId: String = "prj_commercial_tower_b"
    private lateinit var adapter: MeasurementAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMeasurementBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        projectId = arguments?.getString(ARG_PROJECT_ID) ?: "prj_commercial_tower_b"

        setupRecyclerView()
        setupListeners()
        observeState()

        measurementViewModel.loadMeasurements(projectId)
    }

    private fun setupRecyclerView() {
        adapter = MeasurementAdapter()
        binding.rvMeasurements.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMeasurements.adapter = adapter
    }

    private fun setupListeners() {
        binding.swipeRefreshMb.setColorSchemeResources(R.color.cms_accent)
        binding.swipeRefreshMb.setOnRefreshListener {
            measurementViewModel.loadMeasurements(projectId)
        }

        binding.btnAddMeasurement.setOnClickListener {
            val dialog = AddMeasurementDialog { desc, nos, length, width, depth, ded, unit, location ->
                measurementViewModel.addMeasurement(desc, nos, length, width, depth, ded, unit, location)
            }
            dialog.show(childFragmentManager, AddMeasurementDialog.TAG)
        }

        binding.viewError.btnRetry.setOnClickListener {
            measurementViewModel.loadMeasurements(projectId)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                measurementViewModel.uiState.collect { state ->
                    binding.swipeRefreshMb.isRefreshing = false
                    when (state) {
                        is MeasurementUiState.Loading -> {
                            binding.viewLoading.root.visibility = View.VISIBLE
                            binding.viewError.root.visibility = View.GONE
                            binding.viewEmpty.root.visibility = View.GONE
                        }
                        is MeasurementUiState.Success -> {
                            binding.viewLoading.root.visibility = View.GONE
                            binding.viewError.root.visibility = View.GONE
                            if (state.measurements.isEmpty()) {
                                binding.viewEmpty.root.visibility = View.VISIBLE
                                binding.viewEmpty.tvEmptyTitle.text = "No MB entries recorded"
                                binding.viewEmpty.tvEmptySubtitle.text = "Record IS:1200 dimension measurements for verification"
                            } else {
                                binding.viewEmpty.root.visibility = View.GONE
                                adapter.submitList(state.measurements)
                            }
                        }
                        is MeasurementUiState.Error -> {
                            binding.viewLoading.root.visibility = View.GONE
                            binding.viewEmpty.root.visibility = View.GONE
                            binding.viewError.root.visibility = View.VISIBLE
                            binding.viewError.tvErrorMessage.text = state.message
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PROJECT_ID = "projectId"

        fun newInstance(projectId: String): MeasurementBookFragment {
            val fragment = MeasurementBookFragment()
            val args = Bundle()
            args.putString(ARG_PROJECT_ID, projectId)
            fragment.arguments = args
            return fragment
        }
    }
}
