//// File: app/src/main/java/com/example/nutritionlabelapp/ChatFragment.kt
//package com.example.nutritionlabelapp
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.appcompat.app.AppCompatActivity
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.lifecycleScope
//import androidx.navigation.fragment.findNavController
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.example.nutritionlabelapp.adapter.ChatAdapter
//import com.example.nutritionlabelapp.adapter.ChatMessage
//import com.example.nutritionlabelapp.databinding.FragmentChatBinding
//import com.example.nutritionlabelapp.network.GenerateRequest
//import com.example.nutritionlabelapp.network.RetrofitClient
//import kotlinx.coroutines.launch
//
//class ChatFragment : Fragment() {
//
//    private var _binding: FragmentChatBinding? = null
//    private val binding get() = _binding!!
//    private lateinit var adapter: ChatAdapter
//
//    // Holds the full back-and-forth so we can both display it and send it as context
//    private val conversation = mutableListOf<ChatMessage>()
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentChatBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // ─── Toolbar with camera button ──────────────────────────────────────────
//        (activity as? AppCompatActivity)?.apply {
//            setSupportActionBar(binding.toolbar)
//            supportActionBar?.setDisplayHomeAsUpEnabled(false)
//            binding.toolbar.title = "Nutrition Chat"
//            binding.toolbar.inflateMenu(R.menu.chat_toolbar_menu)
//            binding.toolbar.setOnMenuItemClickListener { item ->
//                if (item.itemId == R.id.action_camera) {
//                    findNavController().navigate(R.id.cameraFragment)
//                    true
//                } else false
//            }
//        }
//
//        // ─── RecyclerView + Adapter ─────────────────────────────────────────────
//        adapter = ChatAdapter()
//        binding.rvChat.layoutManager = LinearLayoutManager(requireContext()).apply {
//            stackFromEnd = true
//        }
//        binding.rvChat.adapter = adapter
//
//        // ─── Send button ────────────────────────────────────────────────────────
//        binding.fabSend.isEnabled = true
//        binding.fabSend.setOnClickListener {
//            val text = binding.etMessage.text.toString().trim()
//            if (text.isEmpty()) return@setOnClickListener
//
//            // 1️⃣ Add the user's message
//            conversation += ChatMessage(text, isUser = true)
//            adapter.submitList(conversation.toList())
//            binding.etMessage.text?.clear()
//            binding.rvChat.scrollToPosition(conversation.size - 1)
//
//            // 2️⃣ Send to the model
//            callGenerate()
//        }
//    }
//
//    private fun callGenerate() {
//        lifecycleScope.launch {
//            // 3️⃣ Show a single "Analyzing…" placeholder
//            conversation += ChatMessage("Analyzing…", isUser = false)
//            adapter.submitList(conversation.toList())
//            binding.rvChat.scrollToPosition(conversation.size - 1)
//
//            // 4️⃣ Build the prompt from the entire conversation
//            val prompt = conversation.joinToString("\n") { msg ->
//                if (msg.isUser) "User: ${msg.text}" else "Assistant: ${msg.text}"
//            }
//
//            val req = GenerateRequest(
//                model  = "llama4",
//                prompt = prompt,
//                images = null,
//                stream = false
//            )
//
//            try {
//                val resp = RetrofitClient.ollamaService.generate(req)
//
//                // 5️⃣ Remove placeholder
//                conversation.removeLast()
//
//                if (resp.isSuccessful) {
//                    val reply = resp.body()?.response ?: "No response from model."
//                    conversation += ChatMessage(reply, isUser = false)
//                } else {
//                    val err = resp.errorBody()?.string() ?: resp.message()
//                    conversation += ChatMessage("Error: $err", isUser = false)
//                }
//            } catch (e: Exception) {
//                // 6️⃣ On failure: remove placeholder + show error
//                conversation.removeLast()
//                conversation += ChatMessage("Request failed: ${e.localizedMessage}", isUser = false)
//            }
//
//            // 7️⃣ Finally: update UI and scroll
//            adapter.submitList(conversation.toList())
//            binding.rvChat.scrollToPosition(conversation.size - 1)
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}

// File: app/src/main/java/com/example/nutritionlabelapp/ChatFragment.kt
package com.example.nutritionlabelapp

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
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ChatAdapter

    // Holds the entire back-and-forth for both display and context
    private val conversation = mutableListOf<ChatMessage>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ─── Toolbar with camera icon ───────────────────────────────────────────
        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            binding.toolbar.title = "Nutrition Chat"
            binding.toolbar.inflateMenu(R.menu.chat_toolbar_menu)
            binding.toolbar.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.action_camera) {
                    findNavController().navigate(R.id.cameraFragment)
                    true
                } else false
            }
        }

        // ─── RecyclerView + Adapter ─────────────────────────────────────────────
        adapter = ChatAdapter()
        binding.rvChat.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.rvChat.adapter = adapter

        // ─── Send FAB ───────────────────────────────────────────────────────────
        binding.fabSend.isEnabled = true
        binding.fabSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            // 1️⃣ Add user message
            conversation += ChatMessage(text, isUser = true)
            adapter.submitList(conversation.toList())
            binding.etMessage.text?.clear()
            binding.rvChat.scrollToPosition(conversation.size - 1)

            // 2️⃣ Fire off to model
            callGenerate()
        }

        // ─── Camera FAB (in the chat area) ───────────────────────────────────────
        binding.fabCamera.setOnClickListener {
            findNavController().navigate(
                R.id.action_chatFragment_to_cameraFragment
            )
        }

    }

    private fun callGenerate() {
        lifecycleScope.launch {
            // 3️⃣ Show "Analyzing…" placeholder
            conversation += ChatMessage("Analyzing…", isUser = false)
            adapter.submitList(conversation.toList())
            binding.rvChat.scrollToPosition(conversation.size - 1)

            // 4️⃣ Build the full prompt from all messages
            val prompt = conversation.joinToString("\n") { msg ->
                if (msg.isUser) "User: ${msg.text}" else "Assistant: ${msg.text}"
            }
            val req = GenerateRequest(
                model  = "llama4",
                prompt = prompt,
                images = null,
                stream = false
            )

            try {
                val resp = RetrofitClient.ollamaService.generate(req)

                // 5️⃣ Remove placeholder
                conversation.removeLast()

                if (resp.isSuccessful) {
                    val reply = resp.body()?.response ?: "No response from model."
                    conversation += ChatMessage(reply, isUser = false)
                } else {
                    val err = resp.errorBody()?.string() ?: resp.message()
                    conversation += ChatMessage("Error: $err", isUser = false)
                }
            } catch (e: Exception) {
                // 6️⃣ On failure: remove placeholder + show error
                conversation.removeLast()
                conversation += ChatMessage("Request failed: ${e.localizedMessage}", isUser = false)
            }

            // 7️⃣ Update UI & scroll
            adapter.submitList(conversation.toList())
            binding.rvChat.scrollToPosition(conversation.size - 1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

