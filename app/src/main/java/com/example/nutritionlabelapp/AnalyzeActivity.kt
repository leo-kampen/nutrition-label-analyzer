// File: app/src/main/java/com/example/nutritionlabelapp/AnalyzeActivity.kt
package com.example.nutritionlabelapp

import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutritionlabelapp.adapter.ChatAdapter
import com.example.nutritionlabelapp.adapter.ChatMessage
import com.example.nutritionlabelapp.databinding.ActivityAnalyzeBinding
import com.example.nutritionlabelapp.network.GenerateRequest
import com.example.nutritionlabelapp.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class AnalyzeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnalyzeBinding
    private lateinit var adapter: ChatAdapter
    private var imageUri: Uri? = null
    private var imageBase64: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyzeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Toolbar + back arrow
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // 2) Grab & Base64 encode the image on a background thread
        imageUri = intent.getStringExtra("imageUri")?.let(Uri::parse)
        imageUri?.let { uri ->
            lifecycleScope.launch(Dispatchers.IO) {
                imageBase64 = encodeImageToBase64(uri)
            }
        }

        // 3) RecyclerView + adapter
        adapter = ChatAdapter()
        binding.rvChat.layoutManager = LinearLayoutManager(this)
        binding.rvChat.adapter = adapter

        // 4) Disable send until the first analysis completes
        binding.btnSend.isEnabled = false

        // 5) Kick off the very first, image-only prompt
        imageUri?.let { sendInitialPrompt() }
            ?: run { binding.btnSend.isEnabled = true }

        // 6) Handle all follow-ups
        binding.btnSend.setOnClickListener {
            val userText = binding.etMessage.text.toString().trim()
            if (userText.isNotEmpty()) {
                // a) show the user’s question
                adapter.addMessage(ChatMessage(userText, isUser = true))
                binding.etMessage.text?.clear()
                binding.rvChat.scrollToPosition(adapter.itemCount - 1)
                // b) send it off (no image after the first turn)
                callGenerate(userText)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    /**
     * First turn: shows your fixed prompt, then waits for the Base64 image, then calls LLM with only the image.
     */
    private fun sendInitialPrompt() {
        val prompt = "You are a nutrition expert. Summarize this food label."
        adapter.addMessage(ChatMessage(prompt, isUser = true))
        binding.rvChat.scrollToPosition(adapter.itemCount - 1)

        lifecycleScope.launch {
            val imgB64 = withContext(Dispatchers.IO) {
                while (imageBase64 == null) delay(50)
                imageBase64!!
            }
            callGenerate(prompt, images = listOf(imgB64))
        }
    }

    /**
     * Subsequent turns (and also the first turn if images!=null):
     * 1) Builds the full prompt: system + history + latest user turn
     * 2) Shows “Analyzing…”
     * 3) Fires /api/generate
     * 4) Swaps in the model’s reply
     * 5) Re-enables Send
     *
     * @param newUserText  The most recent user message (text).
     * @param images       Base64 images to attach (only for the first call).
     */
    private fun callGenerate(
        newUserText: String,
        images: List<String>? = null
    ) {
        lifecycleScope.launch {
            // 1) Build the stateless prompt
            val fullPrompt = buildConversationPrompt(newUserText)

            // 2) show placeholder
            adapter.addMessage(ChatMessage("Analyzing…", isUser = false))
            binding.rvChat.scrollToPosition(adapter.itemCount - 1)

            // 3) send it
            val req = GenerateRequest(
                model  = "llama4",
                prompt = fullPrompt,
                images = images,
                stream = false
            )

            try {
                val resp = RetrofitClient.ollamaService.generate(req)

                // 4) remove placeholder
                adapter.removeLastBotMessage()

                // 5) show reply or error
                if (resp.isSuccessful) {
                    val reply = resp.body()?.response ?: "No response from model."
                    adapter.addMessage(ChatMessage(reply, isUser = false))
                } else {
                    val err = resp.errorBody()?.string() ?: resp.message()
                    adapter.addMessage(ChatMessage("Error: $err", isUser = false))
                }
            } catch (e: Exception) {
                adapter.removeLastBotMessage()
                adapter.addMessage(
                    ChatMessage("Request failed: ${e.localizedMessage}", isUser = false)
                )
            }

            // 6) scroll & re-enable
            binding.rvChat.scrollToPosition(adapter.itemCount - 1)
            binding.btnSend.isEnabled = true
        }
    }

    /**
     * System + every prior User/Assistant message + the latest turn.
     * Ending on “Assistant:” cues the model to reply.
     */
    private fun buildConversationPrompt(newUserText: String): String {
        val system = "You are a nutrition expert. Summarize this food label."
        val history = adapter.currentList.joinToString("\n") { msg ->
            if (msg.isUser) "User: ${msg.text}"
            else             "Assistant: ${msg.text}"
        }
        return if (history.isEmpty()) {
            "$system\nUser: $newUserText\nAssistant:"
        } else {
            "$system\n$history\nUser: $newUserText\nAssistant:"
        }
    }

    /** JPEG-compress + Base64 encode. */
    private fun encodeImageToBase64(uri: Uri): String {
        contentResolver.openInputStream(uri).use { input ->
            val bmp = android.graphics.BitmapFactory.decodeStream(input)
            val baos = ByteArrayOutputStream()
            bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, baos)
            return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
        }
    }
}
