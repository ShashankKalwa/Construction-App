package com.example.contruction_management_app.feature.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.contruction_management_app.databinding.BottomSheetRoleCredentialsBinding
import com.example.contruction_management_app.databinding.ItemRoleCredentialBinding
import com.example.contruction_management_app.domain.model.Role
import com.example.contruction_management_app.domain.model.RoleCredential
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RoleCredentialsBottomSheetDialog(
    private val credentials: List<RoleCredential>,
    private val onCredentialSelected: (Role, RoleCredential) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetRoleCredentialsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetRoleCredentialsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvRoleCredentials.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRoleCredentials.adapter = RoleCredentialsAdapter(credentials) { cred ->
            onCredentialSelected(cred.role, cred)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class RoleCredentialsAdapter(
        private val items: List<RoleCredential>,
        private val onItemClick: (RoleCredential) -> Unit
    ) : RecyclerView.Adapter<RoleCredentialsAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: ItemRoleCredentialBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemRoleCredentialBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.binding.tvRoleName.text = item.role.displayName
            holder.binding.tvRoleDescription.text = item.description
            holder.binding.tvUsername.text = item.username
            holder.binding.tvPassword.text = item.password

            holder.binding.root.setOnClickListener { onItemClick(item) }
            holder.binding.btnUseRole.setOnClickListener { onItemClick(item) }
        }

        override fun getItemCount(): Int = items.size
    }

    companion object {
        const val TAG = "RoleCredentialsBottomSheetDialog"
    }
}
