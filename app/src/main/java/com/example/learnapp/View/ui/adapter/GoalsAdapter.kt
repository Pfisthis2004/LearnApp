package com.example.learnapp.View.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.learnapp.R

class GoalsAdapter(
    private val goals: List<String>,
    private var status: List<Boolean>
) : RecyclerView.Adapter<GoalsAdapter.GoalViewHolder>() {

    class GoalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvGoal = view.findViewById<TextView>(R.id.tvObjective)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_objective, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goalText = goals[position]
        val isDone = if (position < status.size) status[position] else false

        // Hiển thị Icon và Text
        val prefix = if (isDone) "✅ " else "• "
        holder.tvGoal.text = "$prefix$goalText"

        // Hiệu ứng màu sắc
        if (isDone) {
            holder.tvGoal.setTextColor(Color.parseColor("#4CAF50"))
            holder.tvGoal.alpha = 0.6f
            // Thêm gạch ngang chữ nếu muốn xịn hơn
            holder.tvGoal.paintFlags = holder.tvGoal.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.tvGoal.setTextColor(Color.WHITE)
            holder.tvGoal.alpha = 1.0f
            // Xóa gạch ngang chữ khi chưa xong
            holder.tvGoal.paintFlags = holder.tvGoal.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }
    fun updateData(newStatus: List<Boolean>) {
        this.status = newStatus
        notifyDataSetChanged() // Ép RecyclerView vẽ lại toàn bộ với dữ liệu mới nhất
    }
    override fun getItemCount() = goals.size
}