package com.example.learnapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.learnapp.databinding.BottomCreateConverBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class CreateConversationBottomSheet: BottomSheetDialogFragment() {
    private var _binding: BottomCreateConverBinding? = null
    private val binding get() = _binding!!

    // Khởi tạo GeminiManager (Truyền API Key của bạn vào)
    private lateinit var geminiManager: GeminiManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomCreateConverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Khởi tạo Manager (Thay "YOUR_API_KEY" bằng key thật hoặc BuildConfig)
        geminiManager = GeminiManager()

        // 2. Xử lý khi nhấn nút Tạo (Giả định ID nút là btnGenerate trong XML của bạn)
        binding.btnCreate.setOnClickListener {
            val userIdea = binding.etInput.text.toString().trim()

            if (userIdea.isEmpty()) {
                Toast.makeText(context, "Hãy nhập ý tưởng của bạn nhé!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            generateAndNavigate(userIdea)
        }
    }

    private fun generateAndNavigate(idea: String) {
        // Hiện hiệu ứng chờ (nếu layout của bạn có ProgressBar)
        // binding.progressBar.visibility = View.VISIBLE
        binding.btnCreate.isEnabled = false

        lifecycleScope.launch {
            try {
                // GỌI HÀM TẠO 2 TÌNH HUỐNG
                val options = geminiManager.generateTwoScenarios(idea)

                if (options != null && options.isNotEmpty()) {
                    // Mở màn hình Lựa chọn (ChooseScenarioActivity)
                    val intent = Intent(requireContext(), ChooseScenarioActivity::class.java)

                    // Truyền danh sách 2 tình huống dưới dạng ArrayList
                    intent.putParcelableArrayListExtra("SCENARIOS", ArrayList(options))

                    startActivity(intent)
                    dismiss() // Đóng BottomSheet này lại
                } else {
                    Toast.makeText(context, "AI đang bận, thử lại sau nhé!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnCreate.isEnabled = true
                // binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}