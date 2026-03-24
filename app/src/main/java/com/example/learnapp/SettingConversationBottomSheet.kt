package com.example.learnapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.learnapp.Model.Chat.ChatConfig
import com.example.learnapp.databinding.BottomSettingConverBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SettingConversationBottomSheet(private val config: ChatConfig) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSettingConverBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomSettingConverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Đổ dữ liệu từ Gemini vào UI
        updateUIFromGemini()

        // 2. Xử lý sự kiện thay đổi lựa chọn của người dùng
        setupListeners()

        // 3. Nút Khởi tạo
        binding.btnCreate.setOnClickListener {
            startChatFlow()
        }
    }

    private fun updateUIFromGemini() {
        // Hiển thị 2 lựa chọn vai trò cho AI dựa trên config nhận được
        binding.rbBotrole1.text = "${config.botRole}"
        binding.rbBotrole2.text = "${config.userRole}"

        // Mặc định chọn dòng đầu tiên cho Bee AI
        binding.rbBotrole1.isChecked = true
        binding.tvPersonalityDesc.text = "AI sẽ tập trung sửa lỗi và dùng từ vựng học thuật."
        binding.tvAttitudeDesc.text = "AI sẽ ủng hộ và đồng tình với ý kiến của bạn."
    }

    private fun setupListeners() {
        binding.rgPersonality.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbBotCheerful -> binding.tvPersonalityDesc.text = "AI sẽ dùng tông giọng vui vẻ như một người bạn."
                R.id.rbBotSerious -> binding.tvPersonalityDesc.text = "AI sẽ tập trung sửa lỗi và dùng từ vựng học thuật."
            }
        }
        binding.rgAttitude.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbBotAgree -> binding.tvAttitudeDesc.text = "AI sẽ ủng hộ và đồng tình với ý kiến của bạn"
                R.id.rbBotDebate -> binding.tvAttitudeDesc.text = "AI sẽ đưa ra phản hồi mang tính xây dựng và có thể phản bác ý kiến của bạn"
            }
        }
    }

    private fun startChatFlow() {
        binding.btnCreate.isEnabled = false
        // Hiện hiệu ứng loading
        binding.loadingSetting.visibility = View.VISIBLE
        binding.mainContent.visibility = View.GONE

        val isBotRole1 = binding.rbBotrole1.isChecked
        // Thu thập lựa chọn cuối cùng của người dùng để ghi đè lên config cũ
        val finalBotRole = if (isBotRole1) config.botRole else config.userRole
        val finalUserRole = if (isBotRole1) config.userRole else config.botRole

        // Tạo config cuối cùng để gửi đi
        val finalConfig = config.copy(
            title = config.title,
            description = this.config.description,
            botRole = finalBotRole,
            userRole = finalUserRole,
            personality = if (binding.rbBotCheerful.isChecked) "Cheerful" else "Serious",
            attitude = if (binding.rbBotAgree.isChecked) "Supportive" else "Challenging",
            openingHeader = config.openingHeader
        )

        // Chuyển màn hình sau 1.2 giây
        binding.root.postDelayed({
            val intent = Intent(requireContext(), SreenChatActivity::class.java).apply {
                putExtra("CONFIG_KEY", finalConfig)
            }
            startActivity(intent)
            dismiss()
        }, 1200)
    }
}