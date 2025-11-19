package com.example.learnapp.View.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.learnapp.Model.Lesson
import com.example.learnapp.R
import com.example.learnapp.View.ui.fragment.LessonFragment

class LessonAdapter(private var lesson: MutableList<Lesson>): RecyclerView.Adapter<LessonAdapter.LessonViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LessonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.lesson_item, parent,false)
        return LessonViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: LessonViewHolder,
        position: Int
    ) {
        val lesson= lesson[position]
        Log.d("LessonAdapter", "Lesson: ${lesson.title}, isLocked=${lesson.isLocked}")
        val context = holder.itemView.context
        val resId = context.resources.getIdentifier(lesson.icon, "drawable", context.packageName)
        if (resId != 0) {
            holder.baihoc.setImageResource(resId)
        } else {
            holder.baihoc.setImageResource(R.drawable.outline_disabled_by_default_24) // ảnh mặc định nếu không tìm thấy
        }
        holder.tdbaihoc.text = lesson.title
        holder.trangthai.text =  if (lesson.isCompleted) "Đã hoàn thành" else "Chưa hoàn thành"
        if (lesson.isLocked) {
            holder.lockIcon.visibility = View.VISIBLE
            holder.itemView.isEnabled = false
            holder.itemView.alpha = 0.5f
        } else {
            holder.lockIcon.visibility = View.GONE
            holder.itemView.isEnabled = true
            holder.itemView.alpha = 1.0f
        }
    }

    override fun getItemCount(): Int = lesson.size

    class LessonViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val baihoc = view.findViewById<ImageView>(R.id.lessonIcon)
        val tdbaihoc = view.findViewById<TextView>(R.id.lessonTitle)
        val trangthai = view.findViewById<TextView>(R.id.lessonStatus)
        val lockIcon = view.findViewById<ImageView>(R.id.lockIcon)
    }
}