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

        // 1) Toolbar + Up arrow
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // 2) Grab & Base64-encode the image off the UI thread
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

        // 4) Disable Send until after the first analysis
        binding.btnSend.isEnabled = false

        // 5) Show fixed prompt & start the first generate call
        imageUri?.let { sendInitialPrompt() }
            ?: run { binding.btnSend.isEnabled = true }

        // 6) Handle follow-up user messages
        binding.btnSend.setOnClickListener {
            val userText = binding.etMessage.text.toString().trim()
            if (userText.isNotEmpty()) {
                adapter.addMessage(ChatMessage(userText, isUser = true))
                binding.etMessage.text?.clear()
                binding.rvChat.scrollToPosition(adapter.itemCount - 1)
                callGenerate(images = null)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    /**
     * 1) Shows the fixed instruction as a user message
     * 2) Waits for Base64 image to be ready
     * 3) Calls the multimodal /api/generate with that image
     */
    private fun sendInitialPrompt() {
        val prompt = "You are a nutrition expert. Summarize this label in a short statement."
        adapter.addMessage(ChatMessage(prompt, isUser = true))
        binding.rvChat.scrollToPosition(adapter.itemCount - 1)

        lifecycleScope.launch {
            // wait until the image is Base64-encoded
            val imgB64 = withContext(Dispatchers.IO) {
                while (imageBase64 == null) delay(50)
                imageBase64!!
            }
            // first call includes the image
            callGenerate(images = listOf(imgB64))
        }
    }

    /**
     * Constructs the full conversational prompt by replaying history,
     * then shows “Analyzing…”, fires /api/generate, replaces the placeholder
     * with the model’s reply (or error), and finally enables the Send button.
     *
     * @param images  If non-null, attaches these Base64 images (only on first call).
     */
    private fun callGenerate(images: List<String>?) {
        lifecycleScope.launch {
            // Build the prompt including all previous messages
            val fullPrompt = buildConversationPrompt()

            // 1) Show placeholder
            adapter.addMessage(ChatMessage("Analyzing…", isUser = false))
            binding.rvChat.scrollToPosition(adapter.itemCount - 1)

            // 2) Fire the multimodal request
            val req = GenerateRequest(
                model  = "gemma3:4b",
                prompt = fullPrompt,
                images = images,
                stream = false
            )
            try {
                val resp = RetrofitClient.ollamaService.generate(req)

                // 3) Remove the placeholder
                adapter.removeLastBotMessage()

                // 4) Display the reply or error
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

            // 5) Scroll to bottom and re-enable sending
            binding.rvChat.scrollToPosition(adapter.itemCount - 1)
            binding.btnSend.isEnabled = true
        }
    }

    /**
     * Reads the entire chat history and formats it for the LLM:
     *
     * You are a nutrition expert...
     * User: first prompt
     * Assistant: first reply
     * User: follow-up
     * Assistant:
     */
    private fun buildConversationPrompt(): String {
        val system = "You are a nutrition expert. Summarize this label in a short statement."
        val history = adapter.currentList.joinToString("\n") { msg ->
            if (msg.isUser) "User: ${msg.text}" else "Assistant: ${msg.text}"
        }
        return "$system\n$history\nAssistant:"
    }

    /** JPEG-compresses the image and returns it as a Base64 string. */
    private fun encodeImageToBase64(uri: Uri): String {
        contentResolver.openInputStream(uri).use { input ->
            val bitmap = android.graphics.BitmapFactory.decodeStream(input)
            val baos = ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, baos)
            return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
        }
    }
}
