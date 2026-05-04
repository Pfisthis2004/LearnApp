package com.example.learnapp.View.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.learnapp.R
import com.example.learnapp.View.SettingActivity
import com.example.learnapp.View.ui.adapter.DayAdapter
import com.example.learnapp.ViewModel.UserViewModel
import com.example.learnapp.databinding.FragmentLessonBinding
import com.example.learnapp.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserViewModel by viewModels()
    private lateinit var dayAdapter: DayAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSettings.setOnClickListener {
            val intent = Intent(requireContext(), SettingActivity::class.java)
            startActivity(intent)
            // Thêm animation chuyển cảnh nếu muốn xịn hơn
            requireActivity().overridePendingTransition(R.anim.slide_up, R.anim.slide_down)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (loading) showLoading() else hideLoading()
        }
        dayAdapter = DayAdapter(emptyList())
        binding.rvDaySelector.apply {
            layoutManager = GridLayoutManager(context, 7)
            adapter = dayAdapter
            setHasFixedSize(true) // Tối ưu hiệu năng vì số lượng ngày luôn là 7
        }

        viewModel.loadData()
        observeViewModel()
    }
    private fun showLoading() {
        binding.layoutprofile.visibility = View.GONE
        binding.loadingImage.visibility = View.VISIBLE
        Glide.with(this).asGif().load(R.raw.loading).into(binding.loadingImage)
    }

    private fun hideLoading() {
        binding.loadingImage.visibility = View.GONE
        binding.layoutprofile.visibility = View.VISIBLE
    }
    private fun observeViewModel() {
        viewModel.userData.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.apply {
                    tvName.text = it.displayName
                    viewModel.checkAndAwardCertificate(it, "A1")
                    binding.tvCertificates.text = "${it.certificates} Certificates"

                    // Cập nhật dữ liệu cho Adapter hiện tại
                    val studyDays = viewModel.getWeeklyStudyData(it.completedDays)
                    dayAdapter.updateData(studyDays)

                    Glide.with(this@ProfileFragment) // Dùng context của Fragment
                        .load(it.photoURL)
                        .placeholder(R.drawable.user_avt)
                        .into(imgAvatar)
                }
            }
        }
        viewModel.vocabCount.observe(viewLifecycleOwner) { count ->
            binding.tvWordsLearned.text = "$count Vocabularies"
        }

        viewModel.progressPercent.observe(viewLifecycleOwner) { percent ->
            binding.progressCircle.setProgress(percent, true)
            binding.tvProgressPercent.text = "$percent%"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}