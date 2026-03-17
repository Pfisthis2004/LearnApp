package com.example.learnapp.View

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.learnapp.Model.Vocabulary
import com.example.learnapp.R
import com.example.learnapp.databinding.ActivityDetailVocabBinding

class DetailVocabActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailVocabBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailVocabBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val vocab = intent.getSerializableExtra("VOCAB_DATA") as? Vocabulary
        vocab?.let {
            binding.tvcontext.text = it.vocab
            binding.tvTrans.text = it.translation
            // Nếu Model của bạn có các trường này thì gán vào, nếu không sẽ dùng mặc định
            binding.tvExample.text = it.example.ifEmpty { "Không có ví dụ" }
            binding.tvLevelValue.text = it.levelId
            binding.tvReviewsValue.text = it.chapterId
            binding.tvlessonvalue.text = it.lessonId
        }

        // 3. Nút quay lại
        binding.btnback.setOnClickListener {
            finish() // Đóng activity này để quay về danh sách
        }
    }


}