package com.example.learnapp.View.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.learnapp.Model.NotificationItem
import com.example.learnapp.View.ui.adapter.NotificationAdapter
import com.example.learnapp.databinding.LayoutNotificationListBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NotificationBottomSheet : BottomSheetDialogFragment() {
    private var _binding: LayoutNotificationListBinding? = null
    private val binding get() = _binding!!
    private lateinit var notificationAdapter: NotificationAdapter
    private val notificationList = mutableListOf<NotificationItem>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutNotificationListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        fetchNotificationsFromFirestore()

        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(notificationList)
        binding.rcvNotifications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificationAdapter
        }
    }
    // Trong NotificationBottomSheet.kt
    private fun fetchNotificationsFromFirestore() {
        val db = FirebaseFirestore.getInstance()

        db.collection("notifications")
            .orderBy("sentAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val list = mutableListOf<NotificationItem>()
                for (document in result) {
                    // Ánh xạ document thành object và lấy thêm ID của document
                    val item = document.toObject(NotificationItem::class.java).copy(id = document.id)
                    list.add(item)
                }
                // Gán adapter vào RecyclerView
                binding.rcvNotifications.adapter = NotificationAdapter(list)

                // Ẩn loading nếu có
                if (list.isEmpty()) {
                    // Xử lý khi không có thông báo nào
                }
            }
            .addOnFailureListener {
                // Xử lý lỗi kết nối
            }
    }
}