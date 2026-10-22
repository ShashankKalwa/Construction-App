package com.example.contruction_management_app.feature.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.contruction_management_app.R
import com.example.contruction_management_app.core.auth.SessionDataStore
import com.example.contruction_management_app.databinding.FragmentLoginBinding
import com.example.contruction_management_app.domain.model.Role
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by lazy {
        val sessionDataStore = SessionDataStore(requireContext().applicationContext)
        ViewModelProvider(
            requireActivity(),
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AuthViewModel(sessionDataStore) as T
                }
            }
        )[AuthViewModel::class.java]
    }

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
        setupListeners()
        observeUiState()
    }

    private fun setupRoleDropdown() {
        val roles = Role.values()
        val roleNames = roles.map { it.displayName }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, roleNames)
        binding.actvRole.setAdapter(adapter)
        binding.actvRole.setText(selectedRole.displayName, false)
        binding.etEmail.setText("${selectedRole.id}@apexinfra.com")

        binding.actvRole.setOnItemClickListener { _, _, position, _ ->
            selectedRole = roles[position]
            binding.etEmail.setText("${selectedRole.id}@apexinfra.com")
        }
    }

    private fun setupListeners() {
        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text?.toString().orEmpty()
            authViewModel.login(email, selectedRole)
        }

        binding.btnOpenGallery.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_gallery)
        }
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
                            findNavController().navigate(R.id.action_login_to_mainShell)
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
