package com.example.learnapp.View

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.example.learnapp.ViewModel.VocabViewModel
import com.example.learnapp.databinding.ActivityQuestionBinding
import com.example.learnapp.databinding.FeedbackBottomSheetBinding
import com.google.firebase.auth.FirebaseAuth

class QuestionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuestionBinding
    private val viewModel: QuestionViewModel by viewModels {
        QuestionViewModelFactory(QuestionRepository())
    }
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
            when (res) {
                is ResultState.FillBlankResult -> {
                    val isCorrect = res.isCorrect
                    binding.tvPrompt.setTextColor(
                        ContextCompat.getColor(this,
                            if (isCorrect) R.color.colorSuccess else R.color.colorError
                        )
                    )
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

        // Hiển thị câu hỏi, thay thế ____ nếu có lựa chọn
        val promptText = if (selected != null) {
            q.prompt.replace("____", selected)
        } else {
            q.prompt
        }
        binding.tvPrompt.text = promptText

        // Gán nội dung cho các nút lựa chọn
        binding.btnOption1.text = q.options.getOrNull(0) ?: ""
        binding.btnOption2.text = q.options.getOrNull(1) ?: ""

        // Xử lý khi người dùng chọn đáp án
        binding.btnOption1.setOnClickListener {
            val chosen = q.options[0]
            val updatedPrompt = q.prompt.replace("____", chosen)
            binding.tvPrompt.text = updatedPrompt
            viewModel.checkAnswer(chosen)
        }

        binding.btnOption2.setOnClickListener {
            val chosen = q.options[1]
            val updatedPrompt = q.prompt.replace("____", chosen)
            binding.tvPrompt.text = updatedPrompt
            viewModel.checkAnswer(chosen)
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

        binding.includeResult.tvScore.text = "Điểm của bạn: $scorePercent%"
        binding.includeResult.tvStars.text = "Phần thưởng: +$xp XP"

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
        binding.includeResult.btnContinueLesson.setOnClickListener{finish()}
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}