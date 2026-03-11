package com.example.learnapp.View.ui.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.learnapp.Model.Lesson
import com.example.learnapp.Model.QuestionType
import com.example.learnapp.R
import com.example.learnapp.View.QuestionActivity
import com.example.learnapp.View.SpeakingQuestionActivity

class LessonAdapter(
    private var lessons: MutableList<Lesson>,
    private var completedLessons: List<String>,
    private val isFirstInLevel: Boolean,
    private val onLessonClick: (Lesson) -> Unit
) : RecyclerView.Adapter<LessonAdapter.LessonViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.lesson_item, parent, false)
        return LessonViewHolder(view)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        val lesson = lessons[position]
        val isUnlocked = when {
            // 1. Nếu là BÀI ĐẦU TIÊN của CHƯƠNG ĐẦU TIÊN trong Level -> Luôn mở
            position == 0 && isFirstInLevel -> true

            // 2. Nếu là các bài tiếp theo trong cùng một chương
            position > 0 -> {
                val previousLessonId = lessons[position - 1].id
                completedLessons.contains(previousLessonId)
            }

            // 3. Nếu là bài đầu tiên (pos 0) của các chương sau (isFirst = false)
            // Cần logic kiểm tra bài cuối của chương trước, hoặc tạm thời để false
            // nếu bạn muốn người dùng phải học tuần tự qua từng Chapter.
            else -> false
        }
        // Cập nhật giao diện dựa trên trạng thái mở khóa
        if (!isUnlocked) {
            holder.lockIcon.visibility = View.VISIBLE
            holder.trangthai.text = "Đang khóa"
            holder.itemView.alpha = 0.5f
            holder.itemView.setOnClickListener{
                // Hiển thị thông báo nhẹ cho người dùng biết tại sao bị khóa
                android.widget.Toast.makeText(holder.itemView.context, "Hoàn thành bài trước để mở khóa!", android.widget.Toast.LENGTH_SHORT).show()
            }
        } else {
            holder.lockIcon.visibility = View.GONE
            val isDone = completedLessons.contains(lesson.id)
            holder.trangthai.text = if (isDone) "Đã hoàn thành" else "Sẵn sàng"
            holder.itemView.alpha = 1.0f

            holder.itemView.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, QuestionActivity::class.java).apply {
                    putExtra("id", lesson.id)
                    putExtra("chapterId", lesson.chapterId)
                    putExtra("levelId", lesson.levelId)
                    putExtra("xpReward", lesson.xpReward) // Truyền XP sang để cộng khi xong

                    // Truyền ID bài tiếp theo (nếu có) để Repository biết bài nào sẽ mở tiếp
                    if (position + 1 < lessons.size) {
                        putExtra("nextLessonId", lessons[position + 1].id)
                    }
                }
                context.startActivity(intent)

            }
        }

        // Load Icon và Title
        Glide.with(holder.itemView.context)
            .load(lesson.icon)
            .placeholder(R.drawable.outline_disabled_by_default_24)
            .into(holder.baihoc)
        holder.tdbaihoc.text = lesson.title
    }

    override fun getItemCount(): Int {
        return lessons.size
    }
    fun updateData(newLessons: List<Lesson>, newCompleted: List<String>) {
        this.lessons = newLessons.toMutableList()
        this.completedLessons = newCompleted
        notifyDataSetChanged()
    }

    class LessonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val baihoc: ImageView = view.findViewById(R.id.lessonIcon)
        val tdbaihoc: TextView = view.findViewById(R.id.lessonTitle)
        val trangthai: TextView = view.findViewById(R.id.lessonStatus)
        val lockIcon: ImageView = view.findViewById(R.id.lockIcon)
    }
}