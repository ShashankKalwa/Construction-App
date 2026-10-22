package com.example.contruction_management_app.feature.project

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contruction_management_app.databinding.ItemMemberBinding
import com.example.contruction_management_app.domain.model.ProjectMember

class TeamMemberAdapter : ListAdapter<ProjectMember, TeamMemberAdapter.MemberViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = ItemMemberBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MemberViewHolder(
        private val binding: ItemMemberBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(member: ProjectMember) {
            binding.tvMemberName.text = member.user.fullName
            binding.tvMemberEmail.text = member.user.email
            binding.tvMemberRole.text = (member.assignedRole ?: "MEMBER").replace("_", " ")

            val initial = member.user.fullName.trim().take(1).uppercase()
            binding.tvMemberAvatar.text = if (initial.isNotEmpty()) initial else "U"
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ProjectMember>() {
        override fun areItemsTheSame(oldItem: ProjectMember, newItem: ProjectMember): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ProjectMember, newItem: ProjectMember): Boolean =
            oldItem == newItem
    }
}
