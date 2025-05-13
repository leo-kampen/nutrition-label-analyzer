package com.example.nutritionlabelapp

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutritionlabelapp.adapter.ChatAdapter
import com.example.nutritionlabelapp.adapter.ChatMessage
import com.example.nutritionlabelapp.databinding.ActivityAnalyzeBinding
import com.example.nutritionlabelapp.network.OllamaChatMessage
import com.example.nutritionlabelapp.network.OllamaChatRequest
import com.example.nutritionlabelapp.network.RetrofitClient
import kotlinx.coroutines.launch

class AnalyzeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnalyzeBinding
    private lateinit var adapter: ChatAdapter
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyzeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Set up the toolbar with Up button
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // 2) Load and display the image
        imageUri = intent.getStringExtra("imageUri")?.let { Uri.parse(it) }
        binding.imgLabel.setImageURI(imageUri)

        // 3) Set up the chat RecyclerView (note: rvChat matches your XML)
        adapter = ChatAdapter()
        binding.rvChat.layoutManager = LinearLayoutManager(this)
        binding.rvChat.adapter = adapter

        // Disable send until after initial analysis
        binding.btnSend.isEnabled = false

        // 4) Kick off the initial prompt
        imageUri?.let { sendInitialPrompt(it) }

        // 5) Handle user‐entered replies
        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                adapter.addMessage(ChatMessage(text, isUser = true))
                binding.etMessage.text?.clear()
                binding.rvChat.scrollToPosition(adapter.itemCount - 1)
                sendUserMessage(text)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun sendInitialPrompt(uri: Uri) {
        val prompt = """
          You are a nutrition expert. Here is a link to a nutrition label image:
          $uri
          Please analyze its calories, macros, and tell me if it's healthy.
        """.trimIndent()

        // Show it as the user's message
        adapter.addMessage(ChatMessage(prompt, isUser = true))
        // And send it off
        callOllama(prompt)
    }

    private fun sendUserMessage(userText: String) {
        callOllama(userText)
    }

    private fun callOllama(content: String) {
        lifecycleScope.launch {
            // 1) Placeholder “Analyzing…”
            adapter.addMessage(ChatMessage("Analyzing…", isUser = false))
            binding.rvChat.scrollToPosition(adapter.itemCount - 1)

            // 2) Build request
            val req = OllamaChatRequest(
                model = "llama3.1:70b",
                messages = listOf(OllamaChatMessage("user", content))
            )

            // 3) Call the service
            val resp = RetrofitClient.ollamaService.chat(req)

            // 4) Remove the placeholder
            adapter.removeLastBotMessage()  // your adapter must implement this

            // 5) Show reply or error
            // New (message-based) parsing
            if (resp.isSuccessful) {
                val reply = resp.body()
                    ?.message
                    ?.content
                    ?: "No response from model."
                adapter.addMessage(ChatMessage(reply, isUser = false))
            }
            else {
                val err = resp.errorBody()?.string() ?: resp.message()
                adapter.addMessage(ChatMessage("Error: $err", isUser = false))
            }

            // 6) Scroll and enable send
            binding.rvChat.scrollToPosition(adapter.itemCount - 1)
            binding.btnSend.isEnabled = true
        }
    }
}
