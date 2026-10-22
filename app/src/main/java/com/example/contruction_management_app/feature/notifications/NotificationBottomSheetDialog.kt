package com.example.contruction_management_app.feature.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contruction_management_app.databinding.DialogNotificationsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class NotificationBottomSheetDialog : BottomSheetDialogFragment() {

    private var _binding: DialogNotificationsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotificationViewModel by activityViewModels()
    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = NotificationAdapter { item ->
            if (!item.isRead) {
                viewModel.markAsRead(item.id)
            }
            Toast.makeText(requireContext(), "Opening: ${item.title}", Toast.LENGTH_SHORT).show()
        }

        binding.rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotifications.adapter = adapter

        binding.btnMarkAllRead.setOnClickListener {
            viewModel.markAllAsRead()
            Toast.makeText(requireContext(), "All notifications marked as read", Toast.LENGTH_SHORT).show()
        }

        binding.btnCloseNotifications.setOnClickListener { dismiss() }

        observeState()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is NotificationUiState.Loading -> {}
                        is NotificationUiState.Success -> {
                            adapter.submitList(state.notifications)
                            val unread = state.unreadCount
                            binding.tvUnreadSummary.text = if (unread > 0) {
                                "$unread unread alert${if (unread > 1) "s" else ""} require your attention"
                            } else {
                                "All caught up! No unread notifications"
                            }
                        }
                        is NotificationUiState.Error -> {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
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

    companion object {
        const val TAG = "NotificationBottomSheetDialog"

        fun newInstance(): NotificationBottomSheetDialog {
            return NotificationBottomSheetDialog()
        }
    }
}
