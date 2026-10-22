package com.example.contruction_management_app.feature.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.contruction_management_app.databinding.LayoutTabOverviewBinding
import kotlinx.coroutines.launch

class ProjectOverviewTabFragment : Fragment() {

    private var _binding: LayoutTabOverviewBinding? = null
    private val binding get() = _binding!!

    private val projectViewModel: ProjectViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutTabOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                projectViewModel.detailState.collect { state ->
                    if (state is ProjectDetailUiState.Success) {
                        val p = state.project
                        binding.tvOverviewDescription.text = "Project code ${p.code} at ${p.location} with ${p.phasesCount} active phases and ${p.tasksCount} scheduled tasks."
                        binding.tvOverviewStartDate.text = p.status
                        binding.tvOverviewEndDate.text = p.budgetDisplay
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
        fun newInstance(): ProjectOverviewTabFragment = ProjectOverviewTabFragment()
    }
}
