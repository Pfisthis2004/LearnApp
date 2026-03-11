package com.example.learnapp.View.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.learnapp.R
import com.example.learnapp.Repository.LevelRepostitory
import com.example.learnapp.View.QuestionActivity
import com.example.learnapp.View.ui.adapter.ChapterAdapter
import com.example.learnapp.ViewModel.LessonViewModel
import com.example.learnapp.databinding.FragmentLessonBinding

class LessonFragment : Fragment() {
    private lateinit var chapterAdapter: ChapterAdapter
    private val viewModel: LessonViewModel by viewModels()
    private lateinit var binding: FragmentLessonBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLessonBinding.inflate(inflater, container, false)

        // 1. Lấy thông tin Level từ SharedPreferences hoặc Arguments
        val prefs = requireContext().getSharedPreferences("LearnAppPrefs", AppCompatActivity.MODE_PRIVATE)
        val levelId = arguments?.getString("selectedLevelId") ?: prefs.getString("selectedLevelId", null)
        val levelTitle = arguments?.getString("selectedLevelTitle") ?: prefs.getString("selectedLevelTitle", "Chưa chọn level")

        binding.leveltv.text = levelTitle

        // 2. Thiết lập ChapterAdapter (LessonAdapter sẽ chạy bên trong Adapter này)
        chapterAdapter = ChapterAdapter(emptyList(), emptyList()) { lesson ->
            val intent = Intent(requireContext(), QuestionActivity::class.java)
            intent.putExtra("chapterId", lesson.chapterId)
            intent.putExtra("id", lesson.id)
            intent.putExtra("levelId", lesson.levelId)
            intent.putExtra("xpReward", lesson.xpReward) // Truyền thêm XP nếu cần
            startActivity(intent)
        }

        binding.rcvchapter.layoutManager = LinearLayoutManager(requireContext())
        binding.rcvchapter.adapter = chapterAdapter

        // 3. Hiệu ứng Loading
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (loading) showLoading() else hideLoading()
        }

        // 4.Quan sát cả 2 LiveData để cập nhật Adapter chính xác
        viewModel.chapters.observe(viewLifecycleOwner) { chapters ->
            val completed = viewModel.completedLessons.value ?: emptyList()
            chapterAdapter.updateData(chapters ?: emptyList(), completed)
        }

        viewModel.completedLessons.observe(viewLifecycleOwner) { completed ->
            val chapters = viewModel.chapters.value ?: emptyList()
            chapterAdapter.updateData(chapters, completed ?: emptyList())
        }

        // 5. Xử lý sự kiện chọn lại Level
        binding.levelListView.setOnClickListener {
            showLevelDialog()
        }

        // 6. Load dữ liệu lần đầu
        levelId?.let { viewModel.loadChaptersByLevel(it) }

        return binding.root
    }

    private fun showLevelDialog() {
        val popupView = layoutInflater.inflate(R.layout.bottom_choose_level, null)
        val listView = popupView.findViewById<ListView>(R.id.levelListView)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(popupView)
            .create()

        val levelRepo = LevelRepostitory()
        levelRepo.fetchLevels { levels ->
            val levelNames = levels.map { it.title }
            val levelAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, levelNames)
            listView.adapter = levelAdapter

            listView.setOnItemClickListener { _, _, position, _ ->
                val selectedLevel = levels[position]
                binding.leveltv.text = selectedLevel.title

                // Lưu lại Level đã chọn
                requireContext().getSharedPreferences("LearnAppPrefs", AppCompatActivity.MODE_PRIVATE)
                    .edit()
                    .putString("selectedLevelId", selectedLevel.id)
                    .putString("selectedLevelTitle", selectedLevel.title)
                    .apply()

                showLoading()
                viewModel.loadChaptersByLevel(selectedLevel.id)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showLoading() {
        binding.rcvchapter.visibility = View.GONE
        binding.loadingImage.visibility = View.VISIBLE
        Glide.with(this).asGif().load(R.raw.loading).into(binding.loadingImage)
    }

    private fun hideLoading() {
        binding.loadingImage.visibility = View.GONE
        binding.rcvchapter.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        // Cập nhật lại dữ liệu khi quay lại màn hình (đảm bảo đồng bộ bài học mới)
        val prefs = requireContext().getSharedPreferences("LearnAppPrefs", AppCompatActivity.MODE_PRIVATE)
        val levelId = prefs.getString("selectedLevelId", null)
        levelId?.let { viewModel.loadChaptersByLevel(it) }
    }
}