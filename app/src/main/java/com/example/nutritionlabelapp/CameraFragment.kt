//package com.example.nutritionlabelapp
//
//import android.content.Intent
//import android.net.Uri
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import com.example.nutritionlabelapp.databinding.FragmentCameraBinding
//
//class CameraFragment : Fragment() {
//    private var _binding: FragmentCameraBinding? = null
//    private val binding get() = _binding!!
//    private var imageUri: Uri? = null
//    // ... (your ImageCapture, startCamera(), takePhoto(), etc.)
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentCameraBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        // copy your MainActivity onCreate logic: request permission, startCamera(), set up btnTakePhoto & btnAnalyze
//        binding.btnAnalyze.setOnClickListener {
//            imageUri?.let {
//                startActivity(
//                    Intent(requireContext(), AnalyzeActivity::class.java)
//                        .putExtra("imageUri", it.toString())
//                )
//            }
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}

// File: app/src/main/java/com/example/nutritionlabelapp/CameraFragment.kt
package com.example.nutritionlabelapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.nutritionlabelapp.databinding.FragmentCameraBinding
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import com.google.android.material.snackbar.Snackbar
import java.io.File

class CameraFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    private var imageCapture: ImageCapture? = null
    private var imageUri: Uri? = null

    // Permission launcher
    private val requestCameraPermission = registerForActivityResult(RequestPermission()) { granted ->
        if (granted) startCamera() else
            Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentCameraBinding.inflate(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back arrow in toolbar
        binding.cameraToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Request permission / start camera
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }

        // Take photo
        binding.btnTakePhoto.setOnClickListener { takePhoto() }

        // Analyze button
        binding.btnAnalyze.setOnClickListener {
            imageUri?.let {
                startActivity(
                    Intent(requireContext(), AnalyzeActivity::class.java)
                        .putExtra("imageUri", it.toString())
                )
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            // ImageCapture
            imageCapture = ImageCapture.Builder().build()

            // Select back camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Snackbar.make(binding.root, "Camera initialization failed", Snackbar.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val photoFile = File(
            requireContext().externalMediaDirs.first(),
            "IMG_${System.currentTimeMillis()}.jpg"
        )
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Snackbar.make(binding.root, "Photo capture failed", Snackbar.LENGTH_LONG).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    imageUri = Uri.fromFile(photoFile)
                    binding.btnAnalyze.isEnabled = true
                    Snackbar.make(binding.root, "Photo captured", Snackbar.LENGTH_SHORT).show()
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

