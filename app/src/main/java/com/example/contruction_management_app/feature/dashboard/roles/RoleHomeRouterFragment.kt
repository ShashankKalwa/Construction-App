package com.example.contruction_management_app.feature.dashboard.roles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.contruction_management_app.R
import com.example.contruction_management_app.core.auth.SessionDataStore
import com.example.contruction_management_app.databinding.FragmentRoleHomeRouterBinding
import com.example.contruction_management_app.domain.model.Role
import com.example.contruction_management_app.feature.dashboard.HomeDashboardFragment
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class RoleHomeRouterFragment : Fragment() {

    private var _binding: FragmentRoleHomeRouterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoleHomeRouterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val session = SessionDataStore(requireContext().applicationContext)

        lifecycleScope.launch {
            val user = session.currentUserFlow.firstOrNull()
            val role = user?.role ?: Role.PROJECT_MANAGER

            val fragment: Fragment = when (role) {
                Role.COMPANY_ADMIN -> CompanyAdminHomeFragment()
                Role.SITE_SUPERVISOR -> SiteSupervisorHomeFragment()
                Role.STOREKEEPER -> StorekeeperHomeFragment()
                Role.ACCOUNTANT -> AccountantHomeFragment()
                Role.CIVIL_ENGINEER -> CivilEngineerHomeFragment()
                else -> HomeDashboardFragment()
            }

            childFragmentManager.beginTransaction()
                .replace(R.id.roleHomeContainer, fragment)
                .commitAllowingStateLoss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): RoleHomeRouterFragment = RoleHomeRouterFragment()
    }
}
