package com.example.learnapp.View

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.BackgroundColorSpan
import android.text.style.StyleSpan
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.learnapp.Model.Article
import com.example.learnapp.databinding.ActivityDetailArticleBinding

class DetailArticleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailArticleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val article = intent.getSerializableExtra("ARTICLE_DATA") as? Article

        article?.let {
            binding.tvTitle.text = it.title
            binding.txtLevel.text = it.level // Vì level là Enum, bạn có thể dùng .name hoặc .value

            // Load ảnh bài viết
            Glide.with(this).load(it.thumbnail).into(binding.imgArticle)

            // 2. Xử lý nội dung bài viết với Highlight
            setupHighlightedContent(it.content, it.highlightedWords)
        }
        binding.btnback.setOnClickListener { finish() }

        binding.txtQuestions.setOnClickListener {
            // Chuyển sang ArticleQuizActivity (sẽ làm ở bước sau)
            val intent = Intent(this, ArticleQuizActivity::class.java)
            intent.putExtra("ARTICLE_ID", article?.id)
            intent.putExtra("ARTICLE_CONTENT", article?.content)
            intent.putExtra("XP_REWARD", article?.xp)
            startActivity(intent)
        }
    }
    fun setupHighlightedContent(content: String, highlightList: List<String>) {
        val spannableString = SpannableString(content)

        for (word in highlightList) {
            // Chỉ lấy lần xuất hiện đầu tiên
            val startIndex = content.indexOf(word, ignoreCase = true)
            if (startIndex >= 0) {
                val endIndex = startIndex + word.length

                // Tô vàng và bôi đậm
                spannableString.setSpan(
                    BackgroundColorSpan(Color.YELLOW),
                    startIndex, endIndex,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannableString.setSpan(
                    StyleSpan(Typeface.NORMAL),
                    startIndex, endIndex,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            binding.txtDescription.text = spannableString
        }

        binding.txtDescription.apply {
            text = spannableString
            movementMethod = LinkMovementMethod.getInstance()
            highlightColor = Color.TRANSPARENT
        }
    }

}