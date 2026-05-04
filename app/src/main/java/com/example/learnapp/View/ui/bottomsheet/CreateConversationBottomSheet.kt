package com.example.learnapp.View.ui.bottomsheet

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.learnapp.View.ChooseScenarioActivity
import com.example.learnapp.ViewModel.ChatViewModel
import com.example.learnapp.databinding.BottomCreateConverBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CreateConversationBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomCreateConverBinding? = null
    private val binding get() = _binding!!

    // SỬ DỤNG VIEWMODEL: Thay vì geminiManager trực tiếp
    private val viewModel: ChatViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomCreateConverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // Xử lý giới hạn từ (Giữ nguyên logic của bạn)
        var isUpdating = false
        binding.etInput.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) return
                val words = s?.trim()?.split("\\s+".toRegex())?.filter { it.isNotEmpty() } ?: emptyList()
                if (words.size > 200) {
                    isUpdating = true
                    val limitedText = words.take(200).joinToString(" ")
                    binding.etInput.setText(limitedText)
                    binding.etInput.setSelection(limitedText.length)
                    isUpdating = false
                }
                binding.tvWordLimit.text = "${words.size.coerceAtMost(200)}/200 từ"
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // Nút Tạo: Chỉ gửi yêu cầu tới ViewModel
        binding.btnCreate.setOnClickListener {
            val userIdea = binding.etInput.text.toString().trim()
            if (userIdea.isEmpty()) {
                Toast.makeText(context, "Hãy nhập ý tưởng của bạn nhé!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Gọi hàm trong ViewModel (Bạn cần thêm hàm createScenarios vào ChatViewModel)
            viewModel.createScenarios(userIdea)
        }
    }

    private fun observeViewModel() {
        // Quan sát trạng thái Loading để hiện ProgressBar
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingSetting.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnCreate.isEnabled = !isLoading
        }

        // Quan sát kết quả Scenarios để chuyển màn hình
        viewModel.scenarios.observe(viewLifecycleOwner) { options ->
            if (options != null && options.isNotEmpty()) {
                val intent = Intent(requireContext(), ChooseScenarioActivity::class.java)
                intent.putParcelableArrayListExtra("SCENARIOS", ArrayList(options))
                startActivity(intent)

                // Sau khi chuyển màn, xóa dữ liệu trong ViewModel để tránh việc
                // quay lại màn hình này nó tự động nhảy sang ChooseScenario lần nữa
                viewModel.clearScenarios()
                dismiss()
            }
        }

        // Quan sát lỗi (Sử dụng biến _error đã thêm trước đó)
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}