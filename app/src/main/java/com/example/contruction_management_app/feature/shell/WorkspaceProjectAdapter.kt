package com.example.contruction_management_app.feature.shell

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contruction_management_app.databinding.ItemWorkspaceProjectBinding
import com.example.contruction_management_app.domain.model.Project

class WorkspaceProjectAdapter(
    private val activeProjectId: String?,
    private val onProjectSelected: (Project) -> Unit
) : ListAdapter<Project, WorkspaceProjectAdapter.ProjectViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val binding = ItemWorkspaceProjectBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProjectViewHolder(
        private val binding: ItemWorkspaceProjectBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(project: Project) {
            binding.tvProjectCode.text = project.code
            binding.tvProjectName.text = project.name
            binding.tvProjectLocation.text = project.location
            binding.tvProjectStatusBadge.text = project.status

            val isSelected = project.id == activeProjectId
            binding.ivSelectedCheck.visibility = if (isSelected) View.VISIBLE else View.GONE

            binding.cardWorkspaceProject.setOnClickListener {
                onProjectSelected(project)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Project>() {
        override fun areItemsTheSame(oldItem: Project, newItem: Project): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Project, newItem: Project): Boolean =
            oldItem == newItem
    }
}
