package com.example.nutritionlabelapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.nutritionlabelapp.databinding.FragmentCameraBinding

class CameraFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    private var imageUri: Uri? = null
    // ... (your ImageCapture, startCamera(), takePhoto(), etc.)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // copy your MainActivity onCreate logic: request permission, startCamera(), set up btnTakePhoto & btnAnalyze
        binding.btnAnalyze.setOnClickListener {
            imageUri?.let {
                startActivity(
                    Intent(requireContext(), AnalyzeActivity::class.java)
                        .putExtra("imageUri", it.toString())
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
