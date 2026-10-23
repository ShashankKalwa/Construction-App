package com.example.contruction_management_app.feature.shell

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.FragmentMainShellBinding
import com.example.contruction_management_app.feature.auth.AuthUiState
import com.example.contruction_management_app.feature.auth.AuthViewModel
import com.example.contruction_management_app.feature.dashboard.HomeDashboardFragment
import com.example.contruction_management_app.feature.project.CreateProjectBottomSheetDialog
import com.example.contruction_management_app.feature.project.ProjectListFragment
import com.example.contruction_management_app.feature.project.ProjectViewModel
import com.example.contruction_management_app.feature.task.TaskListFragment
import com.example.contruction_management_app.feature.tools.ToolsFragment
import kotlinx.coroutines.launch

class MainShellFragment : Fragment() {

    private var _binding: FragmentMainShellBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()
    private val projectViewModel: ProjectViewModel by activityViewModels()

    private var isNavigatingInternally = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainShellBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        setupBottomNavigation()
        setupFab()
        observeAuth()
    }

    private fun setupViewPager() {
        val adapter = ShellPagerAdapter(this)
        binding.viewPagerShell.adapter = adapter
        binding.viewPagerShell.isUserInputEnabled = false // switch only via bottom tabs

        binding.viewPagerShell.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val menuId = when (position) {
                    0 -> R.id.nav_home
                    1 -> R.id.nav_projects
                    2 -> R.id.nav_tasks
                    3 -> R.id.nav_tools
                    else -> R.id.nav_home
                }
                if (binding.bottomNavView.selectedItemId != menuId) {
                    isNavigatingInternally = true
                    binding.bottomNavView.selectedItemId = menuId
                    isNavigatingInternally = false
                }
            }
        })
    }

    private fun setupBottomNavigation() {
        binding.bottomNavView.setOnItemSelectedListener { item ->
            if (isNavigatingInternally) return@setOnItemSelectedListener true

            val targetPage = when (item.itemId) {
                R.id.nav_home -> 0
                R.id.nav_projects -> 1
                R.id.nav_tasks -> 2
                R.id.nav_tools -> 3
                else -> -1
            }

            if (targetPage != -1 && binding.viewPagerShell.currentItem != targetPage) {
                binding.viewPagerShell.setCurrentItem(targetPage, false)
            }
            targetPage != -1
        }
    }

    private fun setupFab() {
        binding.fabQuickAction.setOnClickListener {
            val quickSheet = QuickCreateBottomSheetDialog(
                onNewProjectSelected = {
                    val createDialog = CreateProjectBottomSheetDialog(projectViewModel)
                    createDialog.show(parentFragmentManager, CreateProjectBottomSheetDialog.TAG)
                },
                onTasksSelected = {
                    binding.viewPagerShell.setCurrentItem(2, false)
                },
                onToolsSelected = {
                    binding.viewPagerShell.setCurrentItem(3, false)
                }
            )
            quickSheet.show(parentFragmentManager, QuickCreateBottomSheetDialog.TAG)
        }
    }

    private fun observeAuth() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.uiState.collect { state ->
                    if (state is AuthUiState.Idle) {
                        if (findNavController().currentDestination?.id == R.id.mainShellFragment) {
                            findNavController().navigate(R.id.action_mainShell_to_login)
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

    private class ShellPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> HomeDashboardFragment()
                1 -> ProjectListFragment()
                2 -> TaskListFragment()
                3 -> ToolsFragment()
                else -> HomeDashboardFragment()
            }
        }
    }
}
