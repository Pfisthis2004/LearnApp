package com.example.learnapp.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.learnapp.Model.Chat.ChatConfig
import com.example.learnapp.Model.Chat.ScenarioOption

class ScenarioViewModel : ViewModel() {
    // 1. Danh sách kịch bản gốc
    private val _scenarios = MutableLiveData<List<ScenarioOption>>()
    val scenarios: LiveData<List<ScenarioOption>> get() = _scenarios

    // 2. Vị trí kịch bản đang được chọn (-1 là chưa chọn, 1 hoặc 2)
    private val _selectedIndex = MutableLiveData<Int>(-1)
    val selectedIndex: LiveData<Int> get() = _selectedIndex

    // 3. Config cuối cùng sau khi đã được xử lý (Copy description)
    private val _selectedConfig = MutableLiveData<ChatConfig?>()
    val selectedConfig: LiveData<ChatConfig?> get() = _selectedConfig

    fun setScenarios(list: List<ScenarioOption>) {
        if (_scenarios.value == null) {
            _scenarios.value = list
        }
    }

    fun selectScenario(index: Int) {
        _selectedIndex.value = index
        val scenario = _scenarios.value?.getOrNull(index - 1)

        // Logic xử lý dữ liệu: Nạp description từ ScenarioOption vào ChatConfig
        _selectedConfig.value = scenario?.config?.copy(
            description = scenario.description ?: ""
        )
    }
}