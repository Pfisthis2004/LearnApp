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
    private var lastPosition = -1
    companion object {
        private const val TYPE_HEADER = 0 // Layout mô tả + mục tiêu
        private const val TYPE_AI = 1     // Layout bot chat
        private const val TYPE_USER = 2   // Layout user chat
    }
    private fun setAnimation(viewToAnimate: View, position: Int) {
        // Chỉ chạy animation cho những item mới chưa từng hiển thị
        if (position > lastPosition) {
            val animation = android.view.animation.AnimationUtils.loadAnimation(
                viewToAnimate.context,
                R.anim.item_animation_fall_down
            )
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }
    fun submitList(newList: List<ChatMessage>) {
        messages = newList.toMutableList()
        notifyDataSetChanged()
    }

    fun updateGoalStatus(newStatus: List<Boolean>) {
        this.goalStatus = newStatus
        // Chỉ cập nhật lại item đầu tiên (Header) khi trạng thái mục tiêu thay đổi
        notifyDataSetChanged()
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
        when (holder) {
            is HeaderViewHolder -> holder.bind(config, goalStatus)
            is AIViewHolder -> {
                val msgIndex = position - 1
                if (msgIndex in messages.indices) {
                    holder.bind(messages[msgIndex], onSpeakClick)
                    // CHÈN VÀO ĐÂY: Chạy hiệu ứng cho tin nhắn AI
                    setAnimation(holder.itemView, position)
                }
            }
            is UserViewHolder -> {
                val msgIndex = position - 1
                if (msgIndex in messages.indices) {
                    holder.bind(messages[msgIndex])
                    // Nếu muốn tin nhắn User cũng bay lên thì chèn ở đây luôn
                    setAnimation(holder.itemView, position)
                }
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

        // Trong ChatAdapter.kt -> HeaderViewHolder
        fun bind(config: ChatConfig, status: List<Boolean>) {
            // 1. Cập nhật text luôn luôn chạy
            tvDescription.text = config.description

            // 2. Chỉ setup RecyclerView con khi nó chưa có Adapter
            if (rvObjectives.adapter == null) {
                rvObjectives.layoutManager = LinearLayoutManager(itemView.context)
                rvObjectives.setHasFixedSize(true) // Giúp RecyclerView tính toán kích thước nhanh hơn
                rvObjectives.adapter = GoalsAdapter(config.goals, status)
            } else {
                // 3. Nếu đã có, chỉ cần cập nhật trạng thái của mảng Boolean
                (rvObjectives.adapter as GoalsAdapter).updateData(status)
            }
        }
    }

    inner class AIViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvBotMessage = view.findViewById<TextView>(R.id.tvBotMessage)
        private val tvBotTranslation = view.findViewById<TextView>(R.id.tvBotTranslation)
        private val viewDivider = view.findViewById<View>(R.id.viewDivider)
        private val imgTranslate = view.findViewById<ImageView>(R.id.imgTranslate)
        private val imgSpeaker = view.findViewById<ImageView>(R.id.imgSpeaker)
        fun bind(message: ChatMessage, onSpeakClick: (String) -> Unit) {
            val parts = message.text.split("|")
            val englishText = parts[0].trim()
            val vietnameseText = if (parts.size > 1) parts[1].trim() else ""

            tvBotMessage.text =englishText
            tvBotTranslation.text = vietnameseText

            tvBotTranslation.visibility = View.GONE
            viewDivider.visibility = View.GONE
            imgTranslate.alpha = 0.6f

            imgTranslate.setOnClickListener {
                if (tvBotTranslation.visibility == View.GONE) {
                    tvBotTranslation.visibility = View.VISIBLE
                    viewDivider.visibility = View.VISIBLE
                    imgTranslate.alpha = 1.0f // Làm sáng icon khi đang xem dịch
                } else {
                    tvBotTranslation.visibility = View.GONE
                    viewDivider.visibility = View.GONE
                    imgTranslate.alpha = 0.6f
                }
            }

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