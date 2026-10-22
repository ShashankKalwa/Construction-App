package com.example.contruction_management_app.feature.documents

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contruction_management_app.databinding.ItemDocumentVersionBinding
import com.example.contruction_management_app.domain.model.DocumentVersion

class DocumentVersionAdapter : ListAdapter<DocumentVersion, DocumentVersionAdapter.VersionViewHolder>(DiffCallback) {

    inner class VersionViewHolder(private val binding: ItemDocumentVersionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(version: DocumentVersion, isCurrent: Boolean) {
            binding.tvVersionTag.text = if (isCurrent) "${version.versionNumber} (Current)" else version.versionNumber
            binding.tvVersionFileName.text = version.fileName
            binding.tvVersionSize.text = version.formattedSize
            binding.tvVersionNotes.text = version.changeNotes ?: "No revision notes provided."
            binding.tvVersionMeta.text = "Uploaded by ${version.uploadedBy} • ${version.uploadedAt}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VersionViewHolder {
        val binding = ItemDocumentVersionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VersionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VersionViewHolder, position: Int) {
        holder.bind(getItem(position), position == 0)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<DocumentVersion>() {
        override fun areItemsTheSame(oldItem: DocumentVersion, newItem: DocumentVersion): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: DocumentVersion, newItem: DocumentVersion): Boolean = oldItem == newItem
    }
}
