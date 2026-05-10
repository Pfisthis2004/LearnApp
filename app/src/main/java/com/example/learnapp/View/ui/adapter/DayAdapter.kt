package com.example.learnapp.View.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.learnapp.Model.DayStudy
import com.example.learnapp.R

class DayAdapter(private var days: List<DayStudy>): RecyclerView.Adapter<DayAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_day, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val day = days[position]
        holder.tvDay.text = day.dayName
        holder.tvDay.isActivated = day.isSelected
        // Đổi background dựa trên việc ngày đó có được học hay không
        if (day.isSelected) {
            // Ngày đã học: Hiện hình tròn màu xanh (colorPrimary)
            holder.tvDay.setBackgroundResource(R.drawable.day_selected)
        } else {
            // Ngày chưa học: Hiện hình tròn màu xám nhạt (colorCard)
            holder.tvDay.setBackgroundResource(R.drawable.day_unselected)
        }
    }

    override fun getItemCount()= days.size
    fun updateData(newDays: List<DayStudy>) {
        this.days = newDays
        notifyDataSetChanged() // Thông báo cho RecyclerView vẽ lại dữ liệu mới
    }
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDay: TextView = view.findViewById(R.id.tvDayItem)
    }
}