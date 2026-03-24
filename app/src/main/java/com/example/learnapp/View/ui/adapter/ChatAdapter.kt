package com.example.learnapp.View.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.learnapp.Model.Chat.ChatConfig
import com.example.learnapp.Model.Chat.ChatMessage
import com.example.learnapp.R

class ChatAdapter(private val config: ChatConfig,
                  private val onSpeakClick: (String) -> Unit)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var messages = mutableListOf<ChatMessage>()
    private var goalStatus = listOf<Boolean>()

    companion object {
        private const val TYPE_HEADER = 0 // Layout mô tả + mục tiêu
        private const val TYPE_AI = 1     // Layout bot chat
        private const val TYPE_USER = 2   // Layout user chat
    }

    fun submitList(newList: List<ChatMessage>) {
        messages = newList.toMutableList()
        notifyDataSetChanged()
    }

    fun updateGoalStatus(newStatus: List<Boolean>) {
        goalStatus = newStatus
        // Chỉ cập nhật lại item đầu tiên (Header) khi trạng thái mục tiêu thay đổi
        notifyItemChanged(0)
    }

    override fun getItemViewType(position: Int): Int {
        // Vị trí đầu tiên luôn là Header mô tả tình huống
        if (position == 0) return TYPE_HEADER

        // Các vị trí sau là tin nhắn (lấy từ list messages, index = position - 1)
        val message = messages[position - 1]
        return if (message.sender == "USER") TYPE_USER else TYPE_AI
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(inflater.inflate(R.layout.item_description, parent, false))
            TYPE_AI -> AIViewHolder(inflater.inflate(R.layout.item_bot_mess, parent, false))
            else -> UserViewHolder(inflater.inflate(R.layout.item_user_mess, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.bind(config, goalStatus)
        } else {
            // Lấy tin nhắn thực tế từ list
            val message = messages[position - 1]
            when (holder) {
                is AIViewHolder -> holder.bind(message,onSpeakClick)
                is UserViewHolder -> holder.bind(message)
            }
        }
    }

    // Tổng số item = List tin nhắn + 1 cái Header mô tả ở đầu
    override fun getItemCount(): Int = messages.size + 1

    /**
     * ViewHolder cho phần Mô tả tình huống + Mục tiêu cần đạt
     */
    inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvDescription = view.findViewById<TextView>(R.id.tvHeaderDescription)
        private val rvObjectives = view.findViewById<RecyclerView>(R.id.rvobjectives)

        fun bind(config: ChatConfig, status: List<Boolean>) {
            android.util.Log.d("ChatAdapter", "Description nhận được: ${config.description}")
            // Hiển thị nội dung bối cảnh
            tvDescription.text = config.description

            // Thiết lập RecyclerView con cho mục tiêu
            rvObjectives.layoutManager = LinearLayoutManager(itemView.context)
            // Đảm bảo bạn truyền list goals và status vào đúng GoalsAdapter
            rvObjectives.adapter = GoalsAdapter(config.goals, status)
        }
    }

    inner class AIViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvBotMessage = view.findViewById<TextView>(R.id.tvBotMessage)
        private val imgSpeaker = view.findViewById<ImageView>(R.id.imgSpeaker)
        fun bind(message: ChatMessage, onSpeakClick: (String) -> Unit) {
            tvBotMessage.text = message.text
            imgSpeaker.setOnClickListener {
                onSpeakClick(message.text)
            }
        }
    }

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvUserMessage = view.findViewById<TextView>(R.id.tvUserMessage)
        fun bind(message: ChatMessage) {
            tvUserMessage.text = message.text
        }
    }
}