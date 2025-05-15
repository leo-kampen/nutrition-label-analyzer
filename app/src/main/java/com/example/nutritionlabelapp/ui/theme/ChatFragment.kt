//// File: app/src/main/java/com/example/nutritionlabelapp/ChatFragment.kt
//package com.example.nutritionlabelapp
//
//import android.net.Uri
//import android.os.Bundle
//import android.util.Base64
//import android.util.Log
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
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.io.ByteArrayOutputStream
//
//class ChatFragment : Fragment() {
//
//    private var _binding: FragmentChatBinding? = null
//    private val binding get() = _binding!!
//    private lateinit var adapter: ChatAdapter
//
//    // holds every turn for display & context
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
//        // ─── Toolbar + camera icon ───────────────────────────────
//        (activity as? AppCompatActivity)?.apply {
//            setSupportActionBar(binding.toolbar)
//            supportActionBar?.setDisplayHomeAsUpEnabled(false)
//            binding.toolbar.title = "Nutrition Chat"
//            binding.toolbar.inflateMenu(R.menu.chat_toolbar_menu)
//            binding.toolbar.setOnMenuItemClickListener { item ->
//                if (item.itemId == R.id.action_camera) {
//                    findNavController().navigate(
//                        R.id.action_chatFragment_to_cameraFragment
//                    )
//                    true
//                } else false
//            }
//        }
//
//        // ─── RecyclerView + adapter ─────────────────────────────
//        adapter = ChatAdapter()
//        binding.rvChat.layoutManager = LinearLayoutManager(requireContext()).apply {
//            stackFromEnd = true
//        }
//        binding.rvChat.adapter = adapter
//
//        // ─── Listen for the image result from CameraFragment ─────
//        parentFragmentManager.setFragmentResultListener(
//            "nutrition_image_request",
//            viewLifecycleOwner
//        ) { _, bundle ->
//            val uriStr = bundle.getString("imageUri") ?: return@setFragmentResultListener
//            val imageUri = Uri.parse(uriStr)
//
//            // 1️⃣ Insert the system prompt as if the user spoke it
//            val systemPrompt = "You are a nutrition expert. Summarize this food label."
//            conversation += ChatMessage(systemPrompt, isUser = true)
//            adapter.submitList(conversation.toList())
//            binding.rvChat.scrollToPosition(conversation.size - 1)
//
//            // 2️⃣ Encode the image and call the model
//            lifecycleScope.launch {
//                val b64 = withContext(Dispatchers.IO) {
//                    encodeImageToBase64(imageUri)
//                }
//                callGenerate(systemPrompt, images = listOf(b64))
//            }
//        }
//
//        // ─── Send FAB ────────────────────────────────────────────
//        binding.fabSend.setOnClickListener {
//            val text = binding.etMessage.text.toString().trim()
//            if (text.isEmpty()) return@setOnClickListener
//
//            conversation += ChatMessage(text, isUser = true)
//            adapter.submitList(conversation.toList())
//            binding.etMessage.text?.clear()
//            binding.rvChat.scrollToPosition(conversation.size - 1)
//
//            callGenerate(text, images = null)
//        }
//
//        // ─── (Optional) Camera FAB ───────────────────────────────
//        binding.fabCamera.setOnClickListener {
//            findNavController().navigate(R.id.action_chatFragment_to_cameraFragment)
//        }
//    }
//
//    private fun callGenerate(
//        newUserText: String,
//        images: List<String>? = null
//    ) {
//        lifecycleScope.launch {
//            // show placeholder
//            conversation += ChatMessage("Analyzing…", isUser = false)
//            adapter.submitList(conversation.toList())
//            binding.rvChat.scrollToPosition(conversation.size - 1)
//
//            // build full prompt/context
//            val prompt = conversation.joinToString("\n") { msg ->
//                if (msg.isUser) "User: ${msg.text}" else "Assistant: ${msg.text}"
//            }
//
//            val req = GenerateRequest(
//                model  = "llama4",
//                prompt = prompt,
//                images = images,
//                stream = false
//            )
//
//            try {
//                val resp = RetrofitClient.ollamaService.generate(req)
//                conversation.removeLast()
//                if (resp.isSuccessful) {
//                    val reply = resp.body()?.response ?: "No response from model."
//                    conversation += ChatMessage(reply, isUser = false)
//                } else {
//                    val err = resp.errorBody()?.string() ?: resp.message()
//                    conversation += ChatMessage("Error: $err", isUser = false)
//                }
//            } catch (e: Exception) {
//                conversation.removeLast()
//                conversation += ChatMessage("Request failed: ${e.localizedMessage}", isUser = false)
//            }
//
//            adapter.submitList(conversation.toList())
//            binding.rvChat.scrollToPosition(conversation.size - 1)
//        }
//    }
//
//    private fun encodeImageToBase64(uri: Uri): String {
//        requireContext().contentResolver.openInputStream(uri).use { input ->
//            val bmp = android.graphics.BitmapFactory.decodeStream(input)
//            val baos = ByteArrayOutputStream()
//            bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, baos)
//            return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
//        }
//    }
//
//    private fun logLiveThreads() {
//        val names = Thread.getAllStackTraces().keys.map { it.name }
//        val leaks = names.filter { it.contains("pool-") || it.contains("camera") }
//        Log.d("PerfCheck", "Total threads=${names.size}, leaked=${leaks.joinToString()}")
//    }
//
//    // then call it just after you navigate back from CameraFragment:
//    override fun onResume() {
//        super.onResume()
//        logLiveThreads()
//    }
//
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}
//

// File: app/src/main/java/com/example/nutritionlabelapp/ChatFragment.kt
package com.example.nutritionlabelapp

import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.nutritionlabelapp.adapter.ChatAdapter
import com.example.nutritionlabelapp.adapter.ChatMessage
import com.example.nutritionlabelapp.databinding.FragmentChatBinding
import com.example.nutritionlabelapp.network.GenerateRequest
import com.example.nutritionlabelapp.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ChatAdapter
    private val conversation = mutableListOf<ChatMessage>()

    // 1) Hold the destination URI for the photo
    private lateinit var photoUri: Uri

    // 2) Launcher for full-res picture
    private val takePictureLauncher =
        registerForActivityResult(TakePicture()) { success ->
            if (!success) return@registerForActivityResult
            onPhotoCaptured(photoUri)
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Toolbar + (optional) camera icon
        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            binding.toolbar.title = "Nutrition Chat"
            // if you kept your chat_toolbar_menu with a camera icon:
            binding.toolbar.inflateMenu(R.menu.chat_toolbar_menu)
            binding.toolbar.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.action_camera) {
                    launchSystemCamera()
                    true
                } else false
            }
        }

        // RecyclerView
        adapter = ChatAdapter()
        binding.rvChat.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.rvChat.adapter = adapter

        // 🔧 QUICK FIX #2: disable change animations & optimize size
        binding.rvChat.setHasFixedSize(true)
        (binding.rvChat.itemAnimator as? SimpleItemAnimator)
            ?.supportsChangeAnimations = false


        // Send button
        binding.fabSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isBlank()) return@setOnClickListener
            appendUserMessage(text)
            callGenerate(text, images = null)
        }

        // If you added a camera FAB next to send FAB:
        binding.fabCamera.setOnClickListener {
            launchSystemCamera()
        }
    }

    private fun launchSystemCamera() {
        // a) Make a file in our externalMediaDirs
        val photoFile = File(
            requireContext().externalMediaDirs.first(),
            "IMG_${System.currentTimeMillis()}.jpg"
        )
        // b) Wrap it in a FileProvider URI
        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            photoFile
        )
        // c) Launch the camera app
        takePictureLauncher.launch(photoUri)
    }

    private fun onPhotoCaptured(uri: Uri) {
        // 1) Show system prompt as a user message
        val systemPrompt = "You are a nutrition expert. Summarize this food label."
        conversation += ChatMessage(systemPrompt, isUser = true)
        adapter.submitList(conversation.toList())
        binding.rvChat.scrollToPosition(conversation.size - 1)

        // 2) Read + Base64-encode + send to LLM
        lifecycleScope.launch {
            val b64 = withContext(Dispatchers.IO) {
                requireContext().contentResolver.openInputStream(uri).use { input ->
                    val bmp = android.graphics.BitmapFactory.decodeStream(input)
                    ByteArrayOutputStream().also { baos ->
                        bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, baos)
                    }.toByteArray()
                }.let { data ->
                    Base64.encodeToString(data, Base64.NO_WRAP)
                }
            }
            callGenerate(systemPrompt, images = listOf(b64))
        }
    }

    private fun appendUserMessage(text: String) {
        conversation += ChatMessage(text, isUser = true)
        adapter.submitList(conversation.toList())
        binding.etMessage.text?.clear()
        binding.rvChat.scrollToPosition(conversation.size - 1)
    }

    private fun callGenerate(
        newUserText: String,
        images: List<String>? = null
    ) {
        lifecycleScope.launch {
            // show placeholder
            conversation += ChatMessage("Analyzing…", isUser = false)
            adapter.submitList(conversation.toList())
            binding.rvChat.scrollToPosition(conversation.size - 1)

            // build prompt
            val prompt = conversation.joinToString("\n") { msg ->
                if (msg.isUser) "User: ${msg.text}" else "Assistant: ${msg.text}"
            }

            val req = GenerateRequest(
                model  = "llama4",
                prompt = prompt,
                images = images,
                stream = false
            )

            try {
                val resp = RetrofitClient.ollamaService.generate(req)
                conversation.removeLast()
                val reply = resp.body()?.response
                    ?: "No response from model."
                conversation += ChatMessage(reply, isUser = false)
            } catch (e: Exception) {
                conversation.removeLast()
                conversation += ChatMessage("Error: ${e.localizedMessage}", isUser = false)
            }

            adapter.submitList(conversation.toList())
            binding.rvChat.scrollToPosition(conversation.size - 1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

