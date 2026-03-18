package com.example.learnapp.View.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.learnapp.Model.Article
import com.example.learnapp.R
import com.example.learnapp.View.DetailArticleActivity
import com.example.learnapp.View.ui.adapter.ArticleAdapter
import com.example.learnapp.ViewModel.ArticleViewModel
import com.example.learnapp.databinding.FragmentArticlesBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.getValue

class ArticlesFragment : Fragment() {
    private lateinit var binding: FragmentArticlesBinding
    private val viewModel: ArticleViewModel by viewModels()
    private lateinit var adapter: ArticleAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentArticlesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Setup RecyclerView
        adapter = ArticleAdapter(emptyList())
        binding.rvArticles.layoutManager = LinearLayoutManager(requireContext())
        binding.rvArticles.adapter = adapter

        // 2. Quan sát dữ liệu từ ViewModel
        viewModel.articlesLiveData.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (loading) showLoading() else hideLoading()
        }

        // 3. Load dữ liệu lần đầu
        viewModel.loadArticles()
    }
    private fun showLoading() {
        binding.rvArticles.visibility = View.GONE
        binding.loadingImage.visibility = View.VISIBLE
        Glide.with(this).asGif().load(R.raw.loading).into(binding.loadingImage)
    }

    private fun hideLoading() {
        binding.loadingImage.visibility = View.GONE
        binding.rvArticles.visibility = View.VISIBLE
    }
    // Quan trọng: Khi làm Quiz xong quay lại, danh sách cần cập nhật lại dấu "Hoàn thành"
    override fun onResume() {
        super.onResume()
        viewModel.loadArticles()
    }
}
