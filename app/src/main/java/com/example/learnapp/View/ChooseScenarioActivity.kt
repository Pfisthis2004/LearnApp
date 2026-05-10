package com.example.learnapp.View

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.learnapp.Model.Chat.ChatConfig
import com.example.learnapp.Model.Chat.ScenarioOption
import com.example.learnapp.R
import com.example.learnapp.View.ui.bottomsheet.SettingConversationBottomSheet
import com.example.learnapp.databinding.ActivityChooseScenarioBinding

class ChooseScenarioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChooseScenarioBinding
    private var selectedConfig: ChatConfig? = null
    private var scenarios: ArrayList<ScenarioOption>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChooseScenarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Nhận danh sách kịch bản từ Intent
        scenarios = intent.getParcelableArrayListExtra<ScenarioOption>("SCENARIOS")

        setupData()
        setupClickListeners()
    }

    private fun setupData() {
        scenarios?.let { list ->
            if (list.size >= 2) {
                // Đổ dữ liệu thật cho Card 1
                binding.tvTitle1.text = list[0].title
                binding.tvDesc1.text = list[0].description


                // Đổ dữ liệu thật cho Card 2
                binding.tvTitle2.text = list[1].title
                binding.tvDesc2.text = list[1].description
            }
        }
    }

    private fun setupClickListeners() {
        // Xử lý nhấn Card 1
        binding.cardScenario1.setOnClickListener { selectScenario(1) }
        binding.rbScenario1.setOnClickListener { selectScenario(1) }

        // Xử lý nhấn Card 2
        binding.cardScenario2.setOnClickListener { selectScenario(2) }
        binding.rbScenario2.setOnClickListener { selectScenario(2) }

        binding.btnContinue.setOnClickListener {
            if (selectedConfig != null) {
                val settingSheet = SettingConversationBottomSheet(selectedConfig!!)
                settingSheet.show(supportFragmentManager, "SettingSheet")
            } else {
                Toast.makeText(this, "Vui lòng chọn một tình huống!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnEdit.setOnClickListener { finish() }
        binding.ivBack.setOnClickListener { finish() }
    }

    // Hàm bổ trợ để quản lý trạng thái chọn
    private fun selectScenario(index: Int) {
        scenarios?.getOrNull(index - 1)?.let { scenario ->

            // Đảm bảo config không null trước khi copy
            scenario.config?.let { config ->
                selectedConfig = config.copy(
                    description = scenario.description ?: "" // Backup bằng chuỗi rỗng nếu null
                )
            }
        }
        if (index == 1) {
            binding.rbScenario1.isChecked = true
            binding.rbScenario2.isChecked = false
//            selectedConfig = scenarios?.get(0)?.config

            // Highlight Card 1
            binding.cardScenario1.strokeWidth = 6
            binding.cardScenario1.setStrokeColor(getColorStateList(R.color.colorPrimary))
            binding.cardScenario2.strokeWidth = 0
        } else {
            binding.rbScenario1.isChecked = false
            binding.rbScenario2.isChecked = true
//            selectedConfig = scenarios?.get(1)?.config

            // Highlight Card 2
            binding.cardScenario2.strokeWidth = 6
            binding.cardScenario2.setStrokeColor(getColorStateList(R.color.colorPrimary))
            binding.cardScenario1.strokeWidth = 0
        }
    }
}