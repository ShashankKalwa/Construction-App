package com.example.contruction_management_app.feature.siteops

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.contruction_management_app.databinding.FragmentSiteOpsBinding
import com.google.android.material.tabs.TabLayoutMediator

class SiteOpsFragment : Fragment() {

    private var _binding: FragmentSiteOpsBinding? = null
    private val binding get() = _binding!!

    private var projectId: String = "prj_commercial_tower_b"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSiteOpsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        projectId = arguments?.getString(ARG_PROJECT_ID) ?: "prj_commercial_tower_b"

        val adapter = SiteOpsPagerAdapter(this, projectId)
        binding.viewPagerSiteOps.adapter = adapter

        TabLayoutMediator(binding.tabLayoutSiteOps, binding.viewPagerSiteOps) { tab, position ->
            tab.text = when (position) {
                0 -> "Daily Reports"
                1 -> "Issues & Snags"
                else -> ""
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class SiteOpsPagerAdapter(
        fragment: Fragment,
        private val projectId: String
    ) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> DailySiteReportListFragment.newInstance(projectId)
                1 -> SiteIssueListFragment.newInstance(projectId)
                else -> DailySiteReportListFragment.newInstance(projectId)
            }
        }
    }

    companion object {
        private const val ARG_PROJECT_ID = "projectId"

        fun newInstance(projectId: String): SiteOpsFragment {
            val fragment = SiteOpsFragment()
            val args = Bundle()
            args.putString(ARG_PROJECT_ID, projectId)
            fragment.arguments = args
            return fragment
        }
    }
}
