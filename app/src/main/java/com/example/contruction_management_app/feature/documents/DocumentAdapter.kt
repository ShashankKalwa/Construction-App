package com.example.contruction_management_app.feature.documents

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contruction_management_app.databinding.ItemDocumentCardBinding
import com.example.contruction_management_app.domain.model.Document

class DocumentAdapter(
    private val onHistoryClick: (Document) -> Unit,
    private val onAddRevisionClick: (Document) -> Unit
) : ListAdapter<Document, DocumentAdapter.DocumentViewHolder>(DiffCallback) {

    inner class DocumentViewHolder(private val binding: ItemDocumentCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Document) {
            binding.tvDocumentTitle.text = item.title
            binding.tvDocumentType.text = "${item.documentType.displayName} • ${item.category ?: "General"}"
            binding.tvVersionBadge.text = item.latestVersion

            val latestVer = item.versions.firstOrNull()
            val sizeStr = latestVer?.formattedSize ?: "0 B"
            binding.tvFileMeta.text = "$sizeStr • ${item.updatedAt}"

            binding.btnViewHistory.setOnClickListener { onHistoryClick(item) }
            binding.btnAddRevision.setOnClickListener { onAddRevisionClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
        val binding = ItemDocumentCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DocumentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Document>() {
        override fun areItemsTheSame(oldItem: Document, newItem: Document): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Document, newItem: Document): Boolean = oldItem == newItem
    }
}
