package com.example.learnapp.View.ui.fragment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.learnapp.R
import com.example.learnapp.Repository.LevelRepostitory
import com.example.learnapp.View.QuestionActivity
import com.example.learnapp.View.ui.adapter.ChapterAdapter
import com.example.learnapp.View.ui.bottomsheet.NotificationBottomSheet
import com.example.learnapp.ViewModel.LessonViewModel
import com.example.learnapp.ViewModel.UserViewModel
import com.example.learnapp.databinding.FragmentLessonBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LessonFragment : Fragment() {
    private lateinit var chapterAdapter: ChapterAdapter
    private val viewModel: LessonViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var binding: FragmentLessonBinding
    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        if (key == "has_new_notification") {
            val hasNew = sharedPreferences.getBoolean("has_new_notification", false)
            // Cập nhật giao diện trên luồng chính
            activity?.runOnUiThread {
                binding.notificationBadge.visibility = if (hasNew) View.VISIBLE else View.GONE
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLessonBinding.inflate(inflater, container, false)
        val prefs = requireContext().getSharedPreferences("LearnAppPrefs", Context.MODE_PRIVATE)
        prefs.registerOnSharedPreferenceChangeListener(prefListener)

        setupInitialData()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Kiểm tra xin quyền thông báo ngay khi vào màn hình (Android 13+)
        checkNotificationPermission()
    }

    private fun setupInitialData() {
        val prefs = requireContext().getSharedPreferences("LearnAppPrefs", Context.MODE_PRIVATE)
        val levelId = prefs.getString("selectedLevelId", null)

        val levelTitle = prefs.getString("selectedLevelTitle", "Chưa chọn level")
        binding.leveltv.text = levelTitle

        val streakCount = prefs.getInt("current_streak", 0) // Mặc định là 0 nếu chưa có
        binding.tvStreakCount.text = streakCount.toString()

        // Load dữ liệu lần đầu nếu đã có LevelId lưu trong máy
        levelId?.let { viewModel.loadChaptersByLevel(it) }
    }

    private fun setupRecyclerView() {
        chapterAdapter = ChapterAdapter(emptyList(), emptyList()) { lesson ->
            val intent = Intent(requireContext(), QuestionActivity::class.java)
            intent.apply {
                putExtra("chapterId", lesson.chapterId)
                putExtra("id", lesson.id)
                putExtra("levelId", lesson.levelId)
                putExtra("xpReward", lesson.xpReward)
            }
            startActivity(intent)
        }
        binding.rcvchapter.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chapterAdapter
        }
    }

    private fun setupObservers() {
        // Quan sát trạng thái Loading
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (loading) showLoading() else hideLoading()
        }
        userViewModel.loadUserDataRealtime()
        userViewModel.loadData()
        userViewModel.userData.observe(viewLifecycleOwner) { user ->
            user?.let {

                binding.tvStreakCount.text = it.streak.toString()
                // Cập nhật lại cache local để đồng bộ
                val prefs = requireContext().getSharedPreferences("LearnAppPrefs", Context.MODE_PRIVATE)
                prefs.edit().putInt("current_streak", it.streak).apply()
            }
        }
        // Quan sát danh sách Chapter
        viewModel.chapters.observe(viewLifecycleOwner) { chapters ->
            val completed = viewModel.completedLessons.value ?: emptyList()
            chapterAdapter.updateData(chapters ?: emptyList(), completed)
        }

        // Quan sát danh sách bài học đã hoàn thành
        viewModel.completedLessons.observe(viewLifecycleOwner) { completed ->
            val chapters = viewModel.chapters.value ?: emptyList()
            chapterAdapter.updateData(chapters, completed ?: emptyList())
        }
    }

    private fun setupClickListeners() {
        // Chọn lại Level
        binding.levelListView.setOnClickListener {
            showLevelDialog()
        }
        binding.streakLayout.setOnClickListener {
            showStreakDetail()
        }
        // Nút thông báo
        binding.notification.setOnClickListener {
            handleNotificationClick()
        }
    }
    private fun showStreakDetail() {
        val prefs = requireContext().getSharedPreferences("LearnAppPrefs", Context.MODE_PRIVATE)
        val streakCount = prefs.getInt("current_streak", 0)

        // Tạo Dialog
        val dialogView = layoutInflater.inflate(R.layout.streakdetailslayout, null)
        val builder = AlertDialog.Builder(requireContext()).setView(dialogView)
        val dialog = builder.create()
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        // Ánh xạ View trong Dialog
        val imgDetail = dialogView.findViewById<ImageView>(R.id.imgStreakDetail)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvStreakTitle)
        val btnClose = dialogView.findViewById<Button>(R.id.btnStreakClose)

        // Dùng Glide load ảnh trong Dialog cho "mượt"
        Glide.with(this).load(R.raw.fire).into(imgDetail)
        tvTitle.text = "$streakCount Ngày Liên Tiếp!"

        btnClose.setOnClickListener { dialog.dismiss() }

        // Làm nền dialog trong suốt để thấy bo góc của layout
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }
    private fun handleNotificationClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            checkNotificationPermission()
        } else {
            // 1. Tắt chấm đỏ ngay lập tức
            binding.notificationBadge.visibility = View.GONE

            // 2. Lưu trạng thái đã đọc vào máy
            val prefs = requireContext().getSharedPreferences("LearnAppPrefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("has_new_notification", false).apply()

            // 3. Mở danh sách thông báo chi tiết
            val bottomSheet = NotificationBottomSheet()
            bottomSheet.show(childFragmentManager, "NotificationList")
        }
    }
    private fun showCongratsDialog(streak: Int) {
        val dialogView = layoutInflater.inflate(R.layout.streakslayout, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

        val img = dialogView.findViewById<ImageView>(R.id.imgCuteFlame)
        val tvMsg = dialogView.findViewById<TextView>(R.id.tvStreakMessage)
        val btn = dialogView.findViewById<Button>(R.id.btnContinue)

        tvMsg.text = "Bạn đã đạt mốc $streak ngày học tập liên tiếp!"

        // Dùng Glide load ảnh pháo hoa hoặc chúc mừng khác
        Glide.with(this)
            .asGif()
            .load(R.raw.cuteflame) // File gif pháo hoa/chúc mừng
            .into(img)

        btn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }
    private fun showLevelDialog() {
        val popupView = layoutInflater.inflate(R.layout.bottom_choose_level, null)
        val listView = popupView.findViewById<ListView>(R.id.levelListView)
        val dialog = AlertDialog.Builder(requireContext()).setView(popupView).create()

        LevelRepostitory().fetchLevels { levels ->
            val levelNames = levels.map { it.title }
            listView.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, levelNames)

            listView.setOnItemClickListener { _, _, position, _ ->
                val selected = levels[position]
                binding.leveltv.text = selected.title

                requireContext().getSharedPreferences("LearnAppPrefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString("selectedLevelId", selected.id)
                    .putString("selectedLevelTitle", selected.title)
                    .apply()

                viewModel.loadChaptersByLevel(selected.id)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
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
        // Cập nhật chấm đỏ và dữ liệu bài học mỗi khi quay lại màn hình
        val prefs = requireContext().getSharedPreferences("LearnAppPrefs", Context.MODE_PRIVATE)

        userViewModel.userData.observe(viewLifecycleOwner) { user ->
            binding.tvStreakCount.text = (user?.streak ?: 0).toString()
        }

        val pendingStreak = prefs.getInt("pending_streak_count", -1)
        if (pendingStreak > 0) {
                showCongratsDialog(pendingStreak)
                // Xóa dấu hiệu chờ hiện
                prefs.edit().remove("pending_streak_count").apply()

        }

        val hasNew = prefs.getBoolean("has_new_notification", false)
        android.util.Log.d("DEBUG_NOTI", "Giá trị trong máy là: " + prefs.getBoolean("has_new_notification", false))
        binding.notificationBadge.visibility = if (hasNew) View.VISIBLE else View.GONE

        prefs.getString("selectedLevelId", null)?.let {
            viewModel.loadChaptersByLevel(it)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        // Hủy đăng ký để tránh rò rỉ bộ nhớ
        val prefs = requireContext().getSharedPreferences("LearnAppPrefs", Context.MODE_PRIVATE)
        prefs.unregisterOnSharedPreferenceChangeListener(prefListener)
    }
}