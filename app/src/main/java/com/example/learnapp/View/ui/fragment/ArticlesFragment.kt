package com.example.learnapp.View.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.learnapp.R
import com.example.learnapp.View.ui.adapter.ArticleAdapter
import com.example.learnapp.ViewModel.ArticleViewModel
import com.example.learnapp.databinding.FragmentArticlesBinding

class ArticlesFragment : Fragment() {
    private lateinit var binding: FragmentArticlesBinding
    private val viewModel: ArticleViewModel by viewModels()
    private lateinit var adapter: ArticleAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentArticlesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setupRecyclerView()
        setupLevelFilters()

        // Mặc định chọn Level A1 khi ứng dụng bắt đầu
        binding.btnA1.performClick()
    }

    private fun setupRecyclerView() {
        adapter = ArticleAdapter(emptyList())
        binding.rvArticles.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ArticlesFragment.adapter
        }
    }

    private fun observeViewModel() {

        // Quản lý trạng thái loading
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (loading) showLoading() else hideLoading()
        }
        viewModel.articlesLiveData.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)
        }
    }

    private fun setupLevelFilters() {
        val buttons = listOf(
            binding.btnA1,
            binding.btnA2,
            binding.btnB1,
            binding.btnB2,
            binding.btnC1
        )

        buttons.forEach { button ->
            button.setOnClickListener {
                // 1. Cập nhật UI: Đổi màu nút để nhận diện trạng thái "đang chọn"
                buttons.forEach { btn ->
                    btn.setBackgroundResource(R.drawable.bg_white_rounded)
                    btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                }

                button.setBackgroundResource(R.drawable.bogoc_level)
                button.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

                // 2. Gọi ViewModel tải dữ liệu theo trình độ (A1, A2,...)
                val selectedLevel = button.text.toString()
                viewModel.loadArticles(selectedLevel)
            }
        }
    }

    private fun showLoading() {
        binding.rvArticles.visibility = View.GONE
        binding.loadingImage.visibility = View.VISIBLE
        // Sử dụng tài nguyên raw/loading để hiển thị gif
        Glide.with(this).asGif().load(R.raw.loading).into(binding.loadingImage)
    }

    private fun hideLoading() {
        binding.loadingImage.visibility = View.GONE
        binding.rvArticles.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        // Cập nhật lại danh sách để làm mới trạng thái "Hoàn thành" sau khi User làm Quiz
        viewModel.loadArticles()
    }
}