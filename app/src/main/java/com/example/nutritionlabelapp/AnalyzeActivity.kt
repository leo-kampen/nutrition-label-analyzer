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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch

class AnalyzeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnalyzeBinding
    private lateinit var adapter: ChatAdapter
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyzeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Toolbar + Up button
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // 2) Grab the image URI from the intent
        imageUri = intent.getStringExtra("imageUri")?.let(Uri::parse)

        // 3) Set up the chat RecyclerView
        adapter = ChatAdapter()
        binding.rvChat.layoutManager = LinearLayoutManager(this)
        binding.rvChat.adapter = adapter

        // 4) Disable the Send button until after the first response
        binding.btnSend.isEnabled = false

        // 5) Kick off the invisible OCR + initial prompt
        imageUri?.let { sendInitialPrompt(it) }
            ?: run {
                // if no image, just enable send so user can type
                binding.btnSend.isEnabled = true
            }

        // 6) Handle user‐entered follow‐up messages
        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                adapter.addMessage(ChatMessage(text, isUser = true))
                binding.etMessage.text?.clear()
                binding.rvChat.scrollToPosition(adapter.itemCount - 1)
                callOllama(text)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    /** Runs OCR on the image, then sends that text to Ollama without showing it in the chat. */
    private fun sendInitialPrompt(uri: Uri) {
        val image = InputImage.fromFilePath(this, uri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                // Flatten and trim the extracted text
                val extracted = visionText.text
                    .replace("\n", " ")
                    .take(2000)
                // Now send it off
                val prompt = "You are a nutrition expert. Summarise the nutrition in a short statement:\n$extracted"
                callOllama(prompt)
                //callOllama(extracted)
            }
            .addOnFailureListener {
                // Fallback prompt if OCR fails
                callOllama("Please analyze this nutrition label.")
            }
    }

    /** Sends any string to your local Llama server and handles the chat UI. */
    private fun callOllama(content: String) {
        lifecycleScope.launch {
            // 1) Show “Analyzing…” in the chat
            adapter.addMessage(ChatMessage("Analyzing…", isUser = false))
            binding.rvChat.scrollToPosition(adapter.itemCount - 1)

            // 2) Build the request
            val req = OllamaChatRequest(
                //model = "llama3.1:70b",
                //model = "smollm2:135m",
                model = "deepseek-r1",
                messages = listOf(OllamaChatMessage("user", content))
            )

            // 3) Call the API
            val resp = RetrofitClient.ollamaService.chat(req)

            // 4) Remove the placeholder
            adapter.removeLastBotMessage()

            // 5) Display the reply or an error
            if (resp.isSuccessful) {
                val reply = resp.body()?.message?.content
                    ?: "No response from model."
                adapter.addMessage(ChatMessage(reply, isUser = false))
            } else {
                val err = resp.errorBody()?.string() ?: resp.message()
                adapter.addMessage(ChatMessage("Error: $err", isUser = false))
            }

            // 6) Scroll to bottom & enable Send
            binding.rvChat.scrollToPosition(adapter.itemCount - 1)
            binding.btnSend.isEnabled = true
        }
    }
}
