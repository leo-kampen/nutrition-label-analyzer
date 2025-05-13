package com.example.nutritionlabelapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nutritionlabelapp.databinding.ItemChatBotBinding
import com.example.nutritionlabelapp.databinding.ItemChatUserBinding

// Data model
data class ChatMessage(val text: String, val isUser: Boolean)

class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = mutableListOf<ChatMessage>()

    fun addMessage(msg: ChatMessage) {
        items.add(msg)
        notifyItemInserted(items.size - 1)
    }

    fun removeLastBotMessage() {
        if (items.isNotEmpty() && !items.last().isUser) {
            items.removeAt(items.size - 1)
            notifyItemRemoved(items.size)
        }
    }


    override fun getItemViewType(position: Int): Int {
        return if (items[position].isUser) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 1) {
            val binding = ItemChatUserBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
            UserViewHolder(binding)
        } else {
            val binding = ItemChatBotBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
            BotViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = items[position]
        if (holder is UserViewHolder) holder.bind(msg)
        else if (holder is BotViewHolder) holder.bind(msg)
    }

    override fun getItemCount() = items.size

    class UserViewHolder(private val binding: ItemChatUserBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: ChatMessage) {
            binding.tvUserMessage.text = msg.text
        }
    }

    class BotViewHolder(private val binding: ItemChatBotBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: ChatMessage) {
            binding.tvBotMessage.text = msg.text
        }
    }
}