package com.example.learnapp.View

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.learnapp.Model.Lesson
import com.example.learnapp.Model.Question
import com.example.learnapp.Model.QuestionType
import com.example.learnapp.Model.ResultState
import com.example.learnapp.Model.Vocabulary
import com.example.learnapp.R
import com.example.learnapp.Repository.QuestionRepository
import com.example.learnapp.View.ui.adapter.VocabResultAdapter
import com.example.learnapp.ViewModel.QuestionViewModel
import com.example.learnapp.ViewModel.QuestionViewModelFactory
import com.example.learnapp.ViewModel.UserViewModel
import com.example.learnapp.ViewModel.VocabViewModel
import com.example.learnapp.databinding.ActivityQuestionBinding
import com.example.learnapp.databinding.FeedbackBottomSheetBinding
import com.google.firebase.auth.FirebaseAuth

class QuestionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuestionBinding
    private val viewModel: QuestionViewModel by viewModels {
        QuestionViewModelFactory(QuestionRepository())
    }
    private val userviewModel: UserViewModel by viewModels()
    private val vocabViewModel: VocabViewModel by viewModels()
    private lateinit var player: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        player = ExoPlayer.Builder(this).build()
        binding.videoView.player = player

        val lessonId = intent.getStringExtra("id") ?: ""

        if (savedInstanceState == null) {
            viewModel.loadQuestions(lessonId)
        }

        // Quan sát danh sách câu hỏi
        viewModel.questions.observe(this) { list ->
            if (list.isEmpty()) return@observe

            if (viewModel.currentIndex.value == 0 && list[0].type == QuestionType.SPEAKING) {
                val intent = Intent(this, SpeakingQuestionActivity::class.java).apply {
                    putExtras(this@QuestionActivity.intent)
                }
                startActivity(intent)
                finish()
            }
        }

        // Quan sát chỉ số câu hỏi
        viewModel.currentIndex.observe(this) { index ->
            val list = viewModel.questions.value ?: return@observe
            if (index < list.size) {
                showQuestion(list[index])
            } else if (list.isNotEmpty()) { // Tránh gọi khi list rỗng
                showFinalResult()
            }
        }

        // Quan sát kết quả từ Strategy Pattern
        viewModel.result.observe(this) { res ->
            val currentQuestion = viewModel.questions.value?.getOrNull(viewModel.currentIndex.value ?: 0) ?: return@observe
            when (res) {
                is ResultState.FillBlankResult -> {
                    binding.tvPrompt.text = currentQuestion.prompt.replace("____", res.userAnswer)
                    binding.tvPrompt.setTextColor(ContextCompat.getColor(this,
                        if (res.isCorrect) R.color.colorSuccess else R.color.colorError))
                }
                is ResultState.OrderingResult -> {
                    val originPrompt = currentQuestion.prompt
                    var finalDisplay = originPrompt
                    // Dùng chính Regex này để tìm vị trí điền
                    val regex = Regex("_{2,}")
                    // res.userAnswer lúc này là chuỗi các từ cách nhau bởi dấu cách (ví dụ: "Where are you from")
                    val words = res.userAnswer.split(" ")

                    words.forEach { word ->
                        // Điền từ thật vào, không dùng ngoặc [] vì đây là kết quả cuối cùng
                        finalDisplay = finalDisplay.replaceFirst(regex, word)
                    }

                    binding.tvPrompt.text = finalDisplay
                    binding.tvPrompt.setTextColor(ContextCompat.getColor(this,
                        if (res.isCorrect) R.color.colorSuccess else R.color.colorError))
                }
                is ResultState.QuizResult -> {
                    val isCorrect = res.correct > 0
                    binding.tvPrompt.setTextColor(
                        ContextCompat.getColor(this,
                            if (isCorrect) R.color.colorSuccess else R.color.colorError
                        )
                    )
                }
                else -> {}
            }

            showFeedback(res) // vẫn giữ phần feedback như cũ
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun showQuestion(q: Question, selected: String? = null) {
        // Reset màu prompt về mặc định
        binding.tvPrompt.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.btnOption1.visibility = View.GONE
        binding.btnOption2.visibility = View.GONE

        binding.inputLayoutFillBlank?.visibility = View.GONE
        binding.flexAvailableWords?.visibility = View.GONE
        binding.btnSubmit?.visibility = View.GONE
        // Hiển thị câu hỏi, thay thế ____ nếu có lựa chọn
        val promptText = if (selected != null) {
            q.prompt.replace("____", selected)
        } else {
            q.prompt
        }

        binding.tvPrompt.text = promptText

        when (q.type) {
            QuestionType.MULTIPLE_CHOICE -> {
                binding.btnOption1.visibility = View.VISIBLE
                binding.btnOption2.visibility = View.VISIBLE

                binding.btnOption1.text = q.options.getOrNull(0) ?: ""
                binding.btnOption2.text = q.options.getOrNull(1) ?: ""

                binding.btnOption1.setOnClickListener {
                    val chosen = q.options.getOrNull(0) ?: ""
                    binding.tvPrompt.text = q.prompt.replace("____", chosen)
                    viewModel.checkAnswer(chosen)
                }
                binding.btnOption2.setOnClickListener {
                    val chosen = q.options.getOrNull(1) ?: ""
                    binding.tvPrompt.text = q.prompt.replace("____", chosen)
                    viewModel.checkAnswer(chosen)
                }
            }

            QuestionType.FILL_IN_THE_BLANK -> {
                binding.inputLayoutFillBlank?.visibility = View.VISIBLE
                binding.btnSubmit?.visibility = View.VISIBLE
                // Xóa text cũ trong EditText nếu có
                binding.etFillBlank?.text?.clear()

                binding.btnSubmit?.setOnClickListener {
                    val input = binding.etFillBlank?.text.toString().trim()
                    if (input.isNotEmpty()) {
                        viewModel.checkAnswer(input) // Chỉ gọi checkAnswer
                    }
                }
            }

            QuestionType.ORDERING -> {
                binding.flexAvailableWords?.visibility = View.VISIBLE
                binding.flexSelectedWords?.visibility = View.VISIBLE
                binding.btnSubmit?.visibility = View.VISIBLE

                viewModel.resetOrdering() // THÊM DÒNG NÀY để làm sạch dữ liệu cũ
                setupOrderingUI(q)

                binding.btnSubmit?.setOnClickListener {
                    val userSentence = viewModel.selectedOrderingWords.value?.joinToString(" ") ?: ""
                    if (userSentence.isNotEmpty()) {
                        viewModel.checkAnswer(userSentence) // Chỉ gọi checkAnswer
                    }
                }
            }

            // Bạn đã xử lý SPEAKING ở Activity khác nên ở đây có thể để trống hoặc log
            else -> {
                Log.d("QuestionActivity", "Type ${q.type} handled elsewhere or not supported here")
            }
        }

        // Phát video nếu có
        if (q.videoUrl.isNotEmpty()) {
            val mediaItem = MediaItem.fromUri(Uri.parse(q.videoUrl))
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        }
    }
    private fun showFeedback(res: ResultState?) {
        if (res == null) return

        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)

        // Sử dụng Binding dành riêng cho file layout của BottomSheet
        val sheetBinding = FeedbackBottomSheetBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        val isCorrect = when (res) {
            is ResultState.QuizResult -> res.correct > 0
            is ResultState.FillBlankResult -> res.isCorrect
            is ResultState.OrderingResult -> res.isCorrect
            else -> false
        }

        // Dùng sheetBinding để truy cập các View cực kỳ an toàn
        sheetBinding.tvResult.text = if (isCorrect) "Chính xác!" else "Sai rồi"
        sheetBinding.tvResult.setTextColor(
            ContextCompat.getColor(this, if (isCorrect) R.color.colorSuccess else R.color.colorError)
        )

        val currentQuestion = viewModel.questions.value?.get(viewModel.currentIndex.value ?: 0)
        sheetBinding.tvExplanation.text = currentQuestion?.explanation

        sheetBinding.btnNext.setOnClickListener {
            dialog.dismiss()
            viewModel.nextQuestion()
        }

        dialog.setCancelable(false)
        dialog.show()
    }

    private fun showFinalResult() {
        binding.main.visibility = View.GONE
        binding.includeResult.root.visibility = View.VISIBLE
        binding.includeResult.vocabularylist.visibility = View.VISIBLE

        val correct = viewModel.correctCount
        val total = viewModel.questions.value?.size ?: 0
        val scorePercent = if (total > 0) (correct * 100) / total else 0

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val lessonId = intent.getStringExtra("id") ?: ""
        val nextLessonId = intent.getStringExtra("nextLessonId") ?: ""
        val xp = intent.getIntExtra("xpReward", 0)
        val chapterId = intent.getStringExtra("chapterId") ?: ""
        val levelId = intent.getStringExtra("levelId") ?: ""

        // Cập nhật thông tin vào ViewModel
        val currentLesson = Lesson(
            id = lessonId,
            xpReward = xp,
            chapterId = chapterId,
            levelId = levelId
        )
        viewModel.setLessonInfo(currentLesson)

        // Thực thi lưu trữ (Ghi đè kết quả + Cộng XP nếu cần)
        viewModel.finishLesson(nextLessonId)

        binding.includeResult.tvScore.text = "Điểm: $scorePercent%"
        binding.includeResult.tvStars.text = "Thưởng: +$xp XP"

        val questions = viewModel.questions.value ?: emptyList()
        Log.d("QuestionActivity", "Tổng số câu hỏi nhận được: ${questions.size}")
        Log.d("DEBUG_DATA", "--- KIỂM TRA MAPPING FIRESTORE ---")
        questions.forEachIndexed { index, q ->
            Log.d("DEBUG_DATA", "Câu số ${index + 1}:")
            Log.d("DEBUG_DATA", " > ID: ${q.id}") // Xem ID có lấy được không
            Log.d("DEBUG_DATA", " > Vocab thô: '${q.vocab}'") // Đây là điểm mấu chốt
            Log.d("DEBUG_DATA", " > Is Vocab Empty: ${q.vocab.isEmpty()}")
        }
        val listVocabToSave = viewModel.questions.value?.filter { it.vocab.isNotEmpty() }?.map { q ->
            Vocabulary(
                vocab = q.vocab,
                example = q.explanation,
                translation = q.translation,
                lessonId = q.lessonId,
                chapterId = currentLesson.chapterId,
                levelId = currentLesson.levelId,
                createdAt = System.currentTimeMillis()
            )
        } ?: emptyList()
        Log.d("DEBUG_DATA", "Kết quả lọc được: ${listVocabToSave.size} từ")
        // Thực hiện lưu vào Subcollection thông qua ViewModel
        if (listVocabToSave.isNotEmpty()) {
            // Thiết lập RecyclerView
            val rv = binding.includeResult.rvVocabularyResult
            rv.layoutManager = LinearLayoutManager(this)
            rv.adapter = VocabResultAdapter(listVocabToSave)

            // Thực hiện lưu vào DB
            vocabViewModel.saveVocabFromLesson(userId, listVocabToSave)
        }else {
            binding.includeResult.vocabularylist.visibility = View.GONE
            Log.w("QuestionActivity", "Không có từ vựng mới nào.")
        }
        binding.includeResult.btnContinueLesson.setOnClickListener{
            userviewModel.markTodayAsLearned()
            Toast.makeText(this, "Đã đánh dấu hôm nay là ngày học", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    private fun setupOrderingUI(q: Question) {
        val availableWords = q.options.toMutableList().apply { shuffle() }
        renderWords(availableWords)

        binding.btnSubmit.setOnClickListener {
            val finalSentence = viewModel.selectedOrderingWords.value?.joinToString(" ") ?: ""
            viewModel.checkAnswer(finalSentence)
        }
    }

    private fun renderWords(available: List<String>) {
        binding.flexAvailableWords.removeAllViews()
        binding.flexSelectedWords.removeAllViews()

        val selected = viewModel.selectedOrderingWords.value ?: mutableListOf()
        val currentQuestion = viewModel.questions.value?.getOrNull(viewModel.currentIndex.value ?: 0) ?: return

        // Định nghĩa Regex tìm cụm gạch dưới (2 dấu trở lên)
        val placeholderRegex = Regex("_{2,}")

        // --- BƯỚC 1: HIỂN THỊ TV_PROMPT ---
        var displayPrompt = currentQuestion.prompt
        selected.forEach { word ->
            // Thay thế lần lượt từng cụm gạch bằng [từ]
            displayPrompt = displayPrompt.replaceFirst(placeholderRegex, "[$word]")
        }
        binding.tvPrompt.text = displayPrompt

        // --- BƯỚC 2: HIỂN THỊ CÁC TỪ ĐÃ CHỌN ---
        selected.forEach { word ->
            val btn = createWordButton(word, isSelected = true) {
                viewModel.removeWordFromOrdering(word)
                renderWords(available)
            }
            binding.flexSelectedWords.addView(btn)
        }

        // --- BƯỚC 3: HIỂN THỊ CÁC TỪ CÒN LẠI ---
        val tempSelected = selected.toMutableList()
        available.forEach { word ->
            if (tempSelected.contains(word)) {
                tempSelected.remove(word)
            } else {
                val btn = createWordButton(word, isSelected = false) {
                    // SỬA TẠI ĐÂY: Đếm số ô trống bằng Regex thay vì split
                    val totalBlanks = placeholderRegex.findAll(currentQuestion.prompt).count()

                    if (selected.size < totalBlanks) {
                        viewModel.addWordToOrdering(word)
                        renderWords(available)
                    } else {
                        Log.d("DEBUG", "Đã chọn đủ $totalBlanks từ, không cho chọn thêm")
                    }
                }
                binding.flexAvailableWords.addView(btn)
            }
        }
    }

    private fun createWordButton(text: String, isSelected: Boolean = false, onClick: () -> Unit): View {
        // Sử dụng Style chuẩn của Material Design
        val styleAttr = if (isSelected) {
            com.google.android.material.R.attr.materialButtonStyle
        } else {
            com.google.android.material.R.attr.materialButtonOutlinedStyle
        }

        val button = com.google.android.material.button.MaterialButton(this, null, styleAttr)
        button.text = text
        button.setOnClickListener { onClick() }
        return button
    }
    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}