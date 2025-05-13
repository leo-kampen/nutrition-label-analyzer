package com.example.nutritionlabelapp

import android.net.Uri
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutritionlabelapp.adapter.ChatAdapter
import com.example.nutritionlabelapp.databinding.ActivityAnalyzeBinding
import com.example.nutritionlabelapp.adapter.ChatMessage

class AnalyzeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAnalyzeBinding
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyzeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar as the support action bar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Display image
        intent.getStringExtra("imageUri")?.let { uriString ->
            val uri = Uri.parse(uriString)
            binding.imgLabel.setImageURI(uri)
        }

        // Setup chat RecyclerView
        chatAdapter = ChatAdapter()
        binding.rvChat.apply {
            layoutManager = LinearLayoutManager(this@AnalyzeActivity)
            adapter = chatAdapter
        }

        // Send initial prompt
        chatAdapter.addMessage(ChatMessage(
            "Analyzing image... Please wait.",
            isUser = false
        ))

        // Input actions
        binding.etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else false
        }
        binding.btnSend.setOnClickListener { sendMessage() }
    }

    private fun sendMessage() {
        val text = binding.etMessage.text.toString().trim()
        if (text.isEmpty()) return
        chatAdapter.addMessage(ChatMessage(text, isUser = true))
        binding.etMessage.setText("")
        chatAdapter.addMessage(ChatMessage(
            "(Bot response will appear here)",
            isUser = false
        ))
        binding.rvChat.scrollToPosition(chatAdapter.itemCount - 1)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}