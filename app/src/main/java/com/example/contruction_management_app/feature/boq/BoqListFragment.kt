package com.example.contruction_management_app.feature.boq

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
import com.example.contruction_management_app.databinding.FragmentBoqListBinding
import kotlinx.coroutines.launch

class BoqListFragment : Fragment() {

    private var _binding: FragmentBoqListBinding? = null
    private val binding get() = _binding!!

    private val boqViewModel: BoqViewModel by activityViewModels()
    private var projectId: String = "prj_commercial_tower_b"
    private lateinit var adapter: BoqListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBoqListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        projectId = arguments?.getString(ARG_PROJECT_ID) ?: "prj_commercial_tower_b"

        setupRecyclerView()
        setupListeners()
        observeState()

        boqViewModel.loadBoqs(projectId)
    }

    private fun setupRecyclerView() {
        adapter = BoqListAdapter { boq ->
            openBoqDetail(boq.id)
        }
        binding.rvBoqs.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBoqs.adapter = adapter
    }

    private fun setupListeners() {
        binding.swipeRefreshBoqs.setColorSchemeResources(R.color.cms_accent)
        binding.swipeRefreshBoqs.setOnRefreshListener {
            boqViewModel.loadBoqs(projectId)
        }

        binding.btnNewBoq.setOnClickListener {
            val dialog = CreateBoqDialog { title, version ->
                boqViewModel.createBoq(projectId, title, version)
            }
            dialog.show(childFragmentManager, CreateBoqDialog.TAG)
        }

        binding.viewError.btnRetry.setOnClickListener {
            boqViewModel.loadBoqs(projectId)
        }
    }

    private fun openBoqDetail(boqId: String) {
        val detailFrag = BoqDetailFragment.newInstance(boqId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, detailFrag)
            .addToBackStack(null)
            .commit()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                boqViewModel.listState.collect { state ->
                    binding.swipeRefreshBoqs.isRefreshing = false
                    when (state) {
                        is BoqListUiState.Loading -> {
                            binding.viewLoading.root.visibility = View.VISIBLE
                            binding.viewError.root.visibility = View.GONE
                            binding.viewEmpty.root.visibility = View.GONE
                        }
                        is BoqListUiState.Success -> {
                            binding.viewLoading.root.visibility = View.GONE
                            binding.viewError.root.visibility = View.GONE
                            if (state.boqs.isEmpty()) {
                                binding.viewEmpty.root.visibility = View.VISIBLE
                                binding.viewEmpty.tvEmptyTitle.text = "No BOQs created yet"
                                binding.viewEmpty.tvEmptySubtitle.text = "Create an initial Bill of Quantities revision"
                            } else {
                                binding.viewEmpty.root.visibility = View.GONE
                                adapter.submitList(state.boqs)
                            }
                        }
                        is BoqListUiState.Error -> {
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

        fun newInstance(projectId: String): BoqListFragment {
            val fragment = BoqListFragment()
            val args = Bundle()
            args.putString(ARG_PROJECT_ID, projectId)
            fragment.arguments = args
            return fragment
        }
    }
}
