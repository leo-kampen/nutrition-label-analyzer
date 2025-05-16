package com.example.nutritionlabelapp

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutritionlabelapp.adapter.ChatAdapter
import com.example.nutritionlabelapp.adapter.ChatMessage
import com.example.nutritionlabelapp.databinding.FragmentChatBinding
import com.example.nutritionlabelapp.network.GenerateRequest
import com.example.nutritionlabelapp.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import android.util.Base64

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ChatAdapter

    // our full history
    private val conversation = mutableListOf<ChatMessage>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Toolbar + camera icon
        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            binding.toolbar.title = "Nutrition Chat"
            binding.toolbar.inflateMenu(R.menu.chat_toolbar_menu)
            binding.toolbar.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.action_camera) {
                    findNavController().navigate(R.id.action_chatFragment_to_cameraFragment)
                    true
                } else false
            }
        }

        // RecyclerView & adapter
        adapter = ChatAdapter()
        binding.rvChat.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.rvChat.adapter = adapter

        // Camera → image result listener
        parentFragmentManager.setFragmentResultListener(
            "nutrition_image_request",
            viewLifecycleOwner
        ) { _, bundle ->
            val uriStr = bundle.getString("imageUri") ?: return@setFragmentResultListener
            val imageUri = Uri.parse(uriStr)

            // show the system prompt as if the user typed it
            val systemPrompt = "You are a nutrition expert. Summarize this food label."
            conversation += ChatMessage(systemPrompt, isUser = true)
            adapter.submitList(conversation.toList())
            binding.rvChat.scrollToPosition(conversation.size - 1)

            // encode image + send
            lifecycleScope.launch {
                val b64 = withContext(Dispatchers.IO) {
                    requireContext().contentResolver.openInputStream(imageUri).use { input ->
                        val bmp = android.graphics.BitmapFactory.decodeStream(input)
                        ByteArrayOutputStream().apply {
                            bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, this)
                        }.toByteArray()
                    }.let { bytes ->
                        Base64.encodeToString(bytes, Base64.NO_WRAP)
                    }
                }
                callGenerate(systemPrompt, images = listOf(b64))
            }
        }

        // Send button
        binding.fabSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            conversation += ChatMessage(text, isUser = true)
            adapter.submitList(conversation.toList())
            binding.etMessage.text?.clear()
            binding.rvChat.scrollToPosition(conversation.size - 1)

            callGenerate(text, images = null)
        }

        // Camera FAB at bottom
        binding.fabCamera.setOnClickListener {
            findNavController().navigate(R.id.action_chatFragment_to_cameraFragment)
        }
    }

    private fun callGenerate(
        newUserText: String,
        images: List<String>? = null
    ) {
        lifecycleScope.launch {
            // 1) add placeholder
            conversation += ChatMessage("Analyzing…", isUser = false)
            adapter.submitList(conversation.toList())
            binding.rvChat.scrollToPosition(conversation.size - 1)

            // 2) build prompt
            val prompt = conversation.joinToString("\n") { msg ->
                if (msg.isUser) "User: ${msg.text}" else "Assistant: ${msg.text}"
            }
            val req = GenerateRequest(
                model  = "llama4",
                prompt = prompt,
                images = images,
                stream = false
            )

            // 3) network + parsing on IO
            val (success, resultText) = withContext(Dispatchers.IO) {
                try {
                    val resp = RetrofitClient.ollamaService.generate(req)
                    if (resp.isSuccessful) {
                        true to (resp.body()?.response ?: "No response from model.")
                    } else {
                        false to (resp.errorBody()?.string() ?: resp.message())
                    }
                } catch (e: Exception) {
                    false to "Request failed: ${e.localizedMessage}"
                }
            }

            // 4) remove exactly one placeholder if still there
            if (conversation.isNotEmpty()
                && !conversation.last().isUser
                && conversation.last().text == "Analyzing…"
            ) {
                conversation.removeAt(conversation.lastIndex)
            }

            // 5) append the real reply or error
            conversation += ChatMessage(
                text   = if (success) resultText else "Error: $resultText",
                isUser = false
            )

            // 6) update UI & scroll
            adapter.submitList(conversation.toList())
            binding.rvChat.scrollToPosition(conversation.size - 1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
