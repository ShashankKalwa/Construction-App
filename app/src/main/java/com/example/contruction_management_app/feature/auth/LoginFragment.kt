package com.example.contruction_management_app.feature.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.FragmentLoginBinding
import com.example.contruction_management_app.domain.model.Role
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()

    private var selectedRole: Role = Role.SUPER_ADMIN

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRoleDropdown()
        updateCredentialsForRole(selectedRole)
        setupListeners()
        observeUiState()
    }

    private fun setupRoleDropdown() {
        val roles = Role.values()
        val roleNames = roles.map { it.displayName }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, roleNames)
        binding.actvRole.setAdapter(adapter)
        binding.actvRole.setText(selectedRole.displayName, false)

        binding.actvRole.setOnItemClickListener { _, _, position, _ ->
            selectedRole = roles[position]
            updateCredentialsForRole(selectedRole)
        }
    }

    private fun updateCredentialsForRole(role: Role) {
        val cred = authViewModel.getCredentialsForRole(role)
        binding.etUsername.setText(cred.username)
        binding.etPassword.setText(cred.password)
        binding.tvCredentialsHint.text = "${cred.username} • ${cred.password}"
    }

    private fun setupListeners() {
        binding.btnSignIn.setOnClickListener {
            val username = binding.etUsername.text?.toString().orEmpty()
            val password = binding.etPassword.text?.toString().orEmpty()
            authViewModel.loginWithCredentials(username, password, selectedRole)
        }

        binding.layoutCredentialsHint.setOnClickListener {
            showCredentialsCheatsheet()
        }

        binding.btnShowCredentials.setOnClickListener {
            showCredentialsCheatsheet()
        }

        binding.btnOpenGallery.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_gallery)
        }
    }

    private fun showCredentialsCheatsheet() {
        val dialog = RoleCredentialsBottomSheetDialog(authViewModel.getAllCredentials()) { role, cred ->
            selectedRole = role
            binding.actvRole.setText(role.displayName, false)
            binding.etUsername.setText(cred.username)
            binding.etPassword.setText(cred.password)
            binding.tvCredentialsHint.text = "${cred.username} • ${cred.password}"
        }
        dialog.show(parentFragmentManager, RoleCredentialsBottomSheetDialog.TAG)
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.uiState.collect { state ->
                    when (state) {
                        is AuthUiState.Idle -> {
                            binding.progressBarLogin.visibility = View.GONE
                            binding.btnSignIn.isEnabled = true
                        }
                        is AuthUiState.Loading -> {
                            binding.progressBarLogin.visibility = View.VISIBLE
                            binding.btnSignIn.isEnabled = false
                        }
                        is AuthUiState.Authenticated -> {
                            binding.progressBarLogin.visibility = View.GONE
                            binding.btnSignIn.isEnabled = true
                            if (findNavController().currentDestination?.id == R.id.loginFragment) {
                                findNavController().navigate(R.id.action_login_to_mainShell)
                            }
                        }
                        is AuthUiState.Error -> {
                            binding.progressBarLogin.visibility = View.GONE
                            binding.btnSignIn.isEnabled = true
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
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
}
