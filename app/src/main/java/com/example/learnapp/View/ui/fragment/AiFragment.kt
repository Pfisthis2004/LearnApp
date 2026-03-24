package com.example.learnapp.View.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.learnapp.CreateConversationBottomSheet
import com.example.learnapp.R
import com.example.learnapp.databinding.FragmentAiBinding
import com.example.learnapp.databinding.FragmentArticlesBinding


class AiFragment : Fragment() {

    private var _binding: FragmentAiBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Sử dụng ViewBinding chuẩn cho Fragment
        _binding = FragmentAiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Xử lý khi nhấn nút "Khởi tạo" (btnKhoiTao trong XML)
        binding.btnKhoiTao.setOnClickListener {
            showCreateBottomSheet()
        }

        // 2. Setup RecyclerView cho danh sách "Gần đây" (Nếu bạn đã có dữ liệu)
        setupRecentConversations()
    }

    private fun showCreateBottomSheet() {
        // Gọi BottomSheet mà chúng ta vừa tạo ở bước trước
        val bottomSheet = CreateConversationBottomSheet()

        // Sử dụng parentFragmentManager để hiển thị Fragment đè lên
        bottomSheet.show(parentFragmentManager, "CreateConversationBottomSheet")
    }

    private fun setupRecentConversations() {
        // Sau này bạn sẽ code lấy dữ liệu từ Firebase ở đây
        // và đổ vào binding.rcvConversations
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Tránh leak memory
    }
}