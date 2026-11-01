package com.example.contruction_management_app.feature.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()

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
        setupListeners()
        observeUiState()
    }

    private fun setupListeners() {
        binding.btnSignIn.setOnClickListener {
            val username = binding.etUsername.text?.toString().orEmpty()
            val password = binding.etPassword.text?.toString().orEmpty()
            authViewModel.loginWithCredentials(username, password)
        }

        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Please contact your company administrator to reset credentials.",
                Toast.LENGTH_SHORT
            ).show()
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
