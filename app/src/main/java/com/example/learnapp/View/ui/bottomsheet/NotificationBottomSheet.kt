package com.example.learnapp.View.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.learnapp.Model.NotificationItem
import com.example.learnapp.View.ui.adapter.NotificationAdapter
import com.example.learnapp.databinding.LayoutNotificationListBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Date

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
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        // 1. Lấy mốc thời gian tạo tài khoản
        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { userDoc ->
                // Trong rules của bạn 'createdAt' là số (number/long)
                val createdAtLong = userDoc.getLong("createdAt") ?: 0L
                val userCreatedAt = Timestamp(Date(createdAtLong))

                // 2. Truy vấn có điều kiện
                db.collection("notifications")
                    .whereGreaterThan("sentAt", userCreatedAt)
                    .orderBy("sentAt", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { result ->
                        val list = mutableListOf<NotificationItem>()
                        for (document in result) {
                            try {
                                val item = document.toObject(NotificationItem::class.java).copy(id = document.id)
                                list.add(item)
                            } catch (e: Exception) {
                                android.util.Log.e("DEBUG_NOTI", "Lỗi lấy dữ liệu: ${e.message}")
                            }
                        }
                        updateNotificationUI(list)
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(requireContext(), "Không thể tải thông báo", Toast.LENGTH_SHORT).show()
                    }
            }
    }

    private fun updateNotificationUI(list: List<NotificationItem>) {
        binding.rcvNotifications.adapter = NotificationAdapter(list)

        if (list.isEmpty()) {
            binding.rcvNotifications.visibility = View.GONE
            binding.tvEmptyMessage.visibility = View.VISIBLE
        } else {
            binding.rcvNotifications.visibility = View.VISIBLE
            binding.tvEmptyMessage.visibility = View.GONE
        }
    }
}