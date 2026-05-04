package com.example.learnapp.View.ui.bottomsheet

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.learnapp.Model.Chat.ChatConfig
import com.example.learnapp.R
import com.example.learnapp.View.SreenChatActivity
import com.example.learnapp.ViewModel.ChatViewModel
import com.example.learnapp.databinding.BottomSettingConverBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SettingConversationBottomSheet(private val config: ChatConfig) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSettingConverBinding

    // Sử dụng activityViewModels để dùng chung instance ChatViewModel với Activity
    private val viewModel: ChatViewModel by activityViewModels()

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
        binding.rbBotrole1.text = "${config.roles[0]}"
        binding.rbBotrole2.text = "${config.roles[1]}"

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
        binding.loadingSetting.visibility = View.VISIBLE
        binding.mainContent.visibility = View.GONE

        // 1. Xác định vai
        val aiIndex = if (binding.rbBotrole1.isChecked) 0 else 1
        val userIndex = if (aiIndex == 0) 1 else 0

        // 2. Cập nhật header
        val updatedHeader = config.openingHeader
            .replace("[Role0]", config.roles[0])
            .replace("[Role1]", config.roles[1])

        // 3. Tạo finalConfig (Giữ nguyên cấu trúc logic của bạn)
        val finalConfig = config.copy(
            title = config.title,
            description = this.config.description,
            botRole = config.roles[aiIndex],
            userRole = config.roles[userIndex],
            goals = config.goals_for_roles[userIndex],
            personality = if (binding.rbBotCheerful.isChecked) "Cheerful" else "Serious",
            attitude = if (binding.rbBotAgree.isChecked) "Supportive" else "Challenging",
            openingHeader = updatedHeader
        )

        // 4. CHỈ SỬA Ở ĐÂY: Đồng bộ hóa với ViewModel nếu cần hoặc mở màn hình Chat
        // Vì SreenChatActivity sẽ tự khởi tạo một ViewModel mới, ta truyền config qua Intent
        binding.root.postDelayed({
            val intent = Intent(requireContext(), SreenChatActivity::class.java).apply {
                putExtra("CONFIG_KEY", finalConfig)
                // Xóa flag cũ để tránh chồng lấp nếu cần
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)
            dismiss()
        }, 1200)
    }
}