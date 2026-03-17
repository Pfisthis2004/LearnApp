package com.example.learnapp.View.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.learnapp.Model.Vocabulary
import com.example.learnapp.R
import com.example.learnapp.View.DetailVocabActivity
import com.example.learnapp.View.ui.adapter.VocabAdapter
import com.example.learnapp.ViewModel.VocabViewModel
import com.example.learnapp.databinding.ActivityDetailVocabBinding
import com.example.learnapp.databinding.FragmentVocabularyBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class VocabularyFragment : Fragment(), TextToSpeech.OnInitListener {
    private lateinit var adapter: VocabAdapter
    private val viewModel: VocabViewModel by viewModels()
    private lateinit var binding: FragmentVocabularyBinding
    private val pendingChanges = mutableMapOf<String, Boolean>()
    private var isShowingAll = true
    private var tts: TextToSpeech? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVocabularyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tts = TextToSpeech(requireContext(), this)
        // 1. Khởi tạo UI và Adapter
        setupRecyclerView()

        // 2. Lắng nghe dữ liệu từ ViewModel
        observeData()
        selectTab(isAll = true)
        // 3. Lấy dữ liệu từ Firebase
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { viewModel.fetchVocabularies(it) }

        // 4. Xử lý sự kiện Tabs
        setupTabListeners()
    }

    private fun setupRecyclerView() {
        adapter = VocabAdapter(
            vocabList = emptyList(),
            onFavoriteClick = { vocab ->
                pendingChanges[vocab.id] = vocab.isFavorite

                // 2. Cập nhật lại số lượng trên Header ngay lập tức
                val currentList = viewModel.vocabList.value ?: emptyList()
                // Tính toán lại số lượng dựa trên cả data gốc và những gì đang chờ lưu
                val favCount = currentList.count {
                    pendingChanges[it.id] ?: it.isFavorite
                }
                binding.favWord.text = favCount.toString()

                // 3. Nếu đang ở Tab Favorite và vừa bỏ yêu thích -> Xóa khỏi UI ngay
                if (!isShowingAll && !vocab.isFavorite) {
                    // Lấy danh sách hiện tại của adapter và lọc bỏ từ vừa click
                    val currentDisplayList = (viewModel.vocabList.value ?: emptyList()).filter {
                        val status = pendingChanges[it.id] ?: it.isFavorite
                        status == true
                    }
                    adapter.updateData(currentDisplayList)
                }
            },
            onVolumeClick = { word ->
                speakOut(word) // Gọi hàm phát âm
            },
            onItemClick = { vocab ->
                // Gọi hàm hiện Popup thay vì Intent
                showVocabDetailDialog(vocab)
            }
        )

        binding.rvVocabulary.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@VocabularyFragment.adapter
        }
    }

    private fun observeData() {
        viewModel.vocabList.observe(viewLifecycleOwner) { list ->
            // Luôn cập nhật Header số lượng
            binding.countWord.text = list.size.toString()
            binding.favWord.text = list.count { it.isFavorite }.toString()

            // Hiển thị dữ liệu dựa trên Tab hiện tại (Mặc định là All khi mới vào)
            if (pendingChanges.isEmpty()) {
                if (isShowingAll) {
                    adapter.updateData(list)
                } else {
                    adapter.updateData(list.filter { it.isFavorite })
                }
            }
        }
    }
    private fun showVocabDetailDialog(vocab: Vocabulary) {
        // 1. Khởi tạo Binding cho layout popup
        val dialogBinding = ActivityDetailVocabBinding.inflate(layoutInflater)
        val dialog = android.app.Dialog(requireContext())
        dialog.setContentView(dialogBinding.root)

        // 3. Đổ dữ liệu vào các view của Popup
        dialogBinding.apply {
            tvcontext.text = vocab.vocab
            tvTrans.text = vocab.translation
            tvExample.text = vocab.example.ifEmpty { "Không có ví dụ" }
            tvLevelValue.text = vocab.levelId
            val chapterNumber = vocab.chapterId.substringAfterLast("ch").filter { it.isDigit() }
            tvReviewsValue.text = chapterNumber.ifEmpty { "1" }
            val lessonNumber = vocab.lessonId.substringAfterLast("l").filter { it.isDigit() }
            tvlessonvalue.text = lessonNumber.ifEmpty { "1" }

            // Nút quay lại (đóng popup)
            btnback.setOnClickListener {
                dialog.dismiss()
            }
        }

        // 4. Cấu hình để Dialog hiện giữa màn hình và có nền trong suốt (bo góc)
        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)

            // --- DÒNG QUAN TRỌNG NHẤT ĐÂY ---
            setWindowAnimations(R.style.DialogAnimation)

            setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setDimAmount(0.7f)
        }

        dialog.show()
        speakOut(vocab.vocab)
    }
    private fun setupTabListeners() {
        binding.tabAll.setOnClickListener {
            selectTab(isAll = true)
        }

        binding.tabFavorite.setOnClickListener {
            selectTab(isAll = false)
        }
    }

    private fun selectTab(isAll: Boolean) {
        isShowingAll = isAll // Cập nhật trạng thái

        // Cập nhật giao diện Tab
        if (isAll) {
            binding.tabAll.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.tabFavorite.setBackgroundResource(0)
        } else {
            binding.tabAll.setBackgroundResource(0)
            binding.tabFavorite.setBackgroundResource(R.drawable.bg_tab_selected)
        }

        // Lấy dữ liệu hiện tại từ ViewModel để lọc lại
        val currentList = viewModel.vocabList.value ?: emptyList()
        if (isAll) {
            adapter.updateData(currentList)
        } else {
            adapter.updateData(currentList.filter { it.isFavorite })
        }
    }
    override fun onDestroyView() {
        // Lưu ý: Lấy userId TRƯỚC khi view bị hủy hoàn toàn
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null && pendingChanges.isNotEmpty()) {
            Log.d("VocabularyFragment", "Đang lưu ${pendingChanges.size} thay đổi...")

            // Tạo một bản sao của Map để gửi đi, tránh lỗi thread-safe
            val changesToSend = HashMap(pendingChanges)
            viewModel.updateAllFavorites(userId, changesToSend)

            // Xóa map tạm
            pendingChanges.clear()
        }
        super.onDestroyView()
    }
    override fun onDestroy() {
        tts?.let {
            it.stop()
            it.shutdown()
        }
        super.onDestroy()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US) // Hoặc Locale.UK cho giọng Anh-Anh
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Ngôn ngữ không hỗ trợ")
            }
        }
    }

    private fun speakOut(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }
}