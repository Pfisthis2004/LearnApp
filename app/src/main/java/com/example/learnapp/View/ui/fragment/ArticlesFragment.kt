package com.example.learnapp.View.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.learnapp.R
import com.example.learnapp.View.ui.adapter.ArticleAdapter
import com.example.learnapp.ViewModel.ArticleViewModel
import com.example.learnapp.databinding.FragmentArticlesBinding
import kotlin.getValue

class ArticlesFragment : Fragment() {
    private lateinit var articleAdapter: ArticleAdapter
    private val viewModel: ArticleViewModel by viewModels()
    private lateinit var binding: FragmentArticlesBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_articles, container, false)
    }
}