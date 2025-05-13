//package com.example.nutritionlabelapp.adapter
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import com.example.nutritionlabelapp.databinding.ItemChatBotBinding
//import com.example.nutritionlabelapp.databinding.ItemChatUserBinding
//
//// Data model
//data class ChatMessage(val text: String, val isUser: Boolean)
//
//class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//    private val items = mutableListOf<ChatMessage>()
//
//    fun addMessage(msg: ChatMessage) {
//        items.add(msg)
//        notifyItemInserted(items.size - 1)
//    }
//
//    fun removeLastBotMessage() {
//        if (items.isNotEmpty() && !items.last().isUser) {
//            items.removeAt(items.size - 1)
//            notifyItemRemoved(items.size)
//        }
//    }
//
//
//
//    override fun getItemViewType(position: Int): Int {
//        return if (items[position].isUser) 1 else 0
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        return if (viewType == 1) {
//            val binding = ItemChatUserBinding.inflate(
//                LayoutInflater.from(parent.context), parent, false)
//            UserViewHolder(binding)
//        } else {
//            val binding = ItemChatBotBinding.inflate(
//                LayoutInflater.from(parent.context), parent, false)
//            BotViewHolder(binding)
//        }
//    }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        val msg = items[position]
//        if (holder is UserViewHolder) holder.bind(msg)
//        else if (holder is BotViewHolder) holder.bind(msg)
//    }
//
//    override fun getItemCount() = items.size
//
//    class UserViewHolder(private val binding: ItemChatUserBinding)
//        : RecyclerView.ViewHolder(binding.root) {
//        fun bind(msg: ChatMessage) {
//            binding.tvUserMessage.text = msg.text
//        }
//    }
//
//    class BotViewHolder(private val binding: ItemChatBotBinding)
//        : RecyclerView.ViewHolder(binding.root) {
//        fun bind(msg: ChatMessage) {
//            binding.tvBotMessage.text = msg.text
//        }
//    }
//}

package com.example.nutritionlabelapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nutritionlabelapp.R

/** Simple data class for a chat message. */
data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

class ChatAdapter : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(ChatDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_USER = 0
        private const val VIEW_TYPE_BOT  = 1
    }

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).isUser) VIEW_TYPE_USER else VIEW_TYPE_BOT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_USER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_user, parent, false)
            UserViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_bot, parent, false)
            BotViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = getItem(position)
        if (holder is UserViewHolder) {
            holder.bind(msg)
        } else if (holder is BotViewHolder) {
            holder.bind(msg)
        }
    }

    /** Appends a message and submits a new list for diffing. */
    fun addMessage(message: ChatMessage) {
        val updated = currentList.toMutableList().apply { add(message) }
        submitList(updated)
    }

    /** Removes the last bot‐sent message (e.g. the “typing…” placeholder). */
    fun removeLastBotMessage() {
        val updated = currentList.toMutableList().apply {
            if (isNotEmpty() && !last().isUser) removeAt(size - 1)
        }
        submitList(updated)
    }

    /** Diff callback to efficiently update only changed items. */
    private class ChatDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage) =
            oldItem === newItem

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage) =
            oldItem == newItem
    }

    /** ViewHolder for user‐side messages. */
    private class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txt: TextView = itemView.findViewById(R.id.tvUserMessage)
        fun bind(msg: ChatMessage) { txt.text = msg.text }
    }

    /** ViewHolder for bot‐side messages. */
    private class BotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txt: TextView = itemView.findViewById(R.id.tvBotMessage)
        fun bind(msg: ChatMessage) { txt.text = msg.text }
    }
}
