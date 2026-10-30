package com.example.contruction_management_app.feature.project

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contruction_management_app.R
import com.example.contruction_management_app.databinding.ItemMemberBinding
import com.example.contruction_management_app.domain.model.ProjectMember

class TeamMemberAdapter(
    private val onMemberClick: (ProjectMember) -> Unit = {}
) : ListAdapter<ProjectMember, TeamMemberAdapter.MemberViewHolder>(DiffCallback) {

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
            val context = itemView.context
            binding.tvMemberName.text = member.user.fullName
            binding.tvMemberEmail.text = member.user.email
            binding.tvMemberRole.text = (member.assignedRole ?: "MEMBER").replace("_", " ")

            val initial = member.user.fullName.trim().take(1).uppercase()
            binding.tvMemberAvatar.text = if (initial.isNotEmpty()) initial else "U"

            val isActive = member.status.equals("ACTIVE", ignoreCase = true)
            binding.tvMemberStatus.text = if (isActive) "ACTIVE" else "INACTIVE"
            val statusColorRes = if (isActive) R.color.cms_success else R.color.cms_text_tertiary
            binding.tvMemberStatus.setTextColor(ContextCompat.getColor(context, statusColorRes))

            binding.cardMember.setOnClickListener {
                onMemberClick(member)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ProjectMember>() {
        override fun areItemsTheSame(oldItem: ProjectMember, newItem: ProjectMember): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ProjectMember, newItem: ProjectMember): Boolean =
            oldItem == newItem
    }
}
