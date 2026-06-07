package com.example.learnapp.View.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.learnapp.Model.Article
import com.example.learnapp.View.ui.activity.DetailArticleActivity
import com.example.learnapp.databinding.ItemArticleBinding

class ArticleAdapter(private var articleList: List<Article>) :
    RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    // ViewHolder nhận vào 'binding' thay vì 'view'
    class ArticleViewHolder(val binding: ItemArticleBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemArticleBinding.inflate(layoutInflater, parent, false)
        return ArticleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = articleList[position]
        val binding = holder.binding

        binding.txtTitle.text = article.title
        binding.txtLevel.text = article.level
        binding.txtQuestions.text = "${article.quizCount} questions"

        // Hiển thị trạng thái Hoàn thành (Dữ liệu riêng của User)
        binding.txtComplete.visibility = if (article.isCompleted) View.VISIBLE else View.GONE

        // Load ảnh
        Glide.with(binding.root.context).load(article.thumbnail).into(binding.imgArticle)

        // Click chuyển sang màn hình Detail
        holder.itemView.setOnClickListener {
            val intent = Intent(binding.root.context, DetailArticleActivity::class.java)
            intent.putExtra("ARTICLE_DATA", article)
            binding.root.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = articleList.size

    // Hàm để update dữ liệu mới
    fun updateData(newList: List<Article>) {
        articleList = newList
        notifyDataSetChanged()
    }
}
