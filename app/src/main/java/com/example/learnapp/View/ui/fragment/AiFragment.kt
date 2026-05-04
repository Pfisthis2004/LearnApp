package com.example.learnapp.View.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.learnapp.Model.Chat.HistoryItem
import com.example.learnapp.View.AIResultActivity
import com.example.learnapp.View.ui.bottomsheet.CreateConversationBottomSheet
import com.example.learnapp.View.ui.adapter.HistoryAdapter
import com.example.learnapp.ViewModel.ChatViewModel
import com.example.learnapp.databinding.FragmentAiBinding

class AiFragment : Fragment() {

    private var _binding: FragmentAiBinding? = null
    private val binding get() = _binding!!

    // Khai báo ViewModel và Adapter theo đúng chuẩn MVVM
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Cài đặt RecyclerView (Giữ nguyên ID rcvConversations từ XML của bạn)
        setupRecyclerView()

        // 2. Lắng nghe dữ liệu từ ViewModel
        observeViewModel()

        // 3. Sự kiện nút Khởi tạo
        binding.btnKhoiTao.setOnClickListener {
            showCreateBottomSheet()
        }

        // 4. Bắt đầu lấy dữ liệu từ Repository qua ViewModel
        viewModel.loadHistory()
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(
            onItemClick = { historyItem ->
                // Mở màn hình xem lại
                val intent = Intent(requireContext(), AIResultActivity::class.java).apply {
                    putExtra("HISTORY_ITEM", historyItem)
                    putExtra("IS_PREVIEW", true)
                }
                startActivity(intent)
            },
            onDeleteLongClick = { historyItem ->
                // Khi nhấn giữ: Hiện thông báo xác nhận
                showDeleteDialog(historyItem)
            }
        )

        binding.rcvConversations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun showDeleteDialog(historyItem: HistoryItem) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa bài học '${historyItem.lessonTitle}' không?")
            .setPositiveButton("OK") { dialog, _ ->
                // GỌI HÀM XÓA CỦA BẠN Ở ĐÂY
                viewModel.deleteHistory(historyItem)
                dialog.dismiss()
            }
            .setNegativeButton("Hủy") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun observeViewModel() {
        viewModel.historyList.observe(viewLifecycleOwner) { list ->
            if (list.isNullOrEmpty()) {
                // Hiển thị Toast nếu không có dữ liệu
                Toast.makeText(requireContext(), "Chưa có bài học nào gần đây", Toast.LENGTH_SHORT).show()
                historyAdapter.submitList(emptyList())
            } else {
                // Cập nhật danh sách vào Adapter
                historyAdapter.submitList(list)
            }
        }
    }

    private fun showCreateBottomSheet() {
        val bottomSheet = CreateConversationBottomSheet()
        bottomSheet.show(parentFragmentManager, "CreateConversationBottomSheet")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Tránh rò rỉ bộ nhớ
    }
}