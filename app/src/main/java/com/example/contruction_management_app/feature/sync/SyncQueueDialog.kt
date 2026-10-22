package com.example.contruction_management_app.feature.sync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contruction_management_app.core.network.NetworkClient
import com.example.contruction_management_app.databinding.DialogSyncQueueBinding
import com.example.contruction_management_app.domain.repository.SyncQueueRepository
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class SyncQueueDialog : BottomSheetDialogFragment() {

    private var _binding: DialogSyncQueueBinding? = null
    private val binding get() = _binding!!

    private lateinit var syncRepository: SyncQueueRepository
    private lateinit var adapter: SyncQueueAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSyncQueueBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val apiService = NetworkClient.getApiService(requireContext().applicationContext)
        syncRepository = SyncQueueRepository(apiService)

        adapter = SyncQueueAdapter()
        binding.rvSyncQueue.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSyncQueue.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            syncRepository.syncState.collect { queue ->
                adapter.submitList(queue)
                val pending = syncRepository.getPendingCount()
                binding.tvSyncSummary.text = if (pending > 0) {
                    "$pending local drafts waiting for cloud sync"
                } else {
                    "All records synced with cloud database"
                }
            }
        }

        binding.btnSyncNow.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                binding.btnSyncNow.isEnabled = false
                val result = syncRepository.syncAllPending()
                binding.btnSyncNow.isEnabled = true
                result.fold(
                    onSuccess = { count ->
                        Toast.makeText(requireContext(), "Successfully synced $count items", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = {
                        Toast.makeText(requireContext(), "Sync failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        binding.btnClearConfirmed.setOnClickListener {
            syncRepository.clearConfirmed()
            Toast.makeText(requireContext(), "Cleared confirmed records", Toast.LENGTH_SHORT).show()
        }

        binding.btnCloseSync.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "SyncQueueDialog"

        fun newInstance(): SyncQueueDialog {
            return SyncQueueDialog()
        }
    }
}
