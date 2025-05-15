// File: app/src/main/java/com/example/nutritionlabelapp/CameraFragment.kt
package com.example.nutritionlabelapp

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.nutritionlabelapp.databinding.FragmentCameraBinding
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import com.google.android.material.snackbar.Snackbar
import java.io.File

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var imageUri: Uri? = null

    private val requestPermission =
        registerForActivityResult(RequestPermission()) { granted ->
            if (granted) startCamera()
            else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back arrow
        binding.cameraToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Request permission / start camera
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermission.launch(Manifest.permission.CAMERA)
        }

        // Take Photo
        binding.btnTakePhoto.setOnClickListener { takePhoto() }

        // Retake
        binding.btnRetake.setOnClickListener {
            imageUri = null
            binding.ivCapturedPhoto.visibility = View.GONE
            binding.previewView.visibility = View.VISIBLE
            binding.btnAnalyze.isEnabled = false
            binding.btnRetake.visibility = View.GONE
            binding.btnTakePhoto.visibility = View.VISIBLE
        }

        // Analyze → back to Chat
        binding.btnAnalyze.setOnClickListener {
            imageUri?.let { uri ->
                parentFragmentManager.setFragmentResult(
                    "nutrition_image_request",
                    Bundle().apply { putString("imageUri", uri.toString()) }
                )
                findNavController().navigateUp()
            }
        }
    }

    private fun startCamera() {
        ProcessCameraProvider.getInstance(requireContext()).let { future ->
            future.addListener({
                cameraProvider = future.get().also { provider ->
                    provider.unbindAll()

                    // lower‐res preview for performance
                    val preview = Preview.Builder()
                        .setTargetResolution(Size(640, 480))
                        .build()
                        .also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }

                    imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                    try {
                        provider.bindToLifecycle(
                            viewLifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        Snackbar.make(binding.root,
                            "Camera init failed",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }, ContextCompat.getMainExecutor(requireContext()))
        }
    }

    private fun takePhoto() {
        val photoFile = File(
            requireContext().externalMediaDirs.first(),
            "IMG_${System.currentTimeMillis()}.jpg"
        )
        val options = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // **Use the main‐thread executor** so we never reject tasks
        imageCapture?.takePicture(
            options,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Snackbar.make(binding.root,
                        "Photo capture failed",
                        Snackbar.LENGTH_LONG
                    ).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    imageUri = Uri.fromFile(photoFile)
                    // Update UI on main thread
                    binding.ivCapturedPhoto.setImageURI(imageUri)
                    binding.ivCapturedPhoto.visibility = View.VISIBLE
                    binding.previewView.visibility = View.GONE
                    binding.btnAnalyze.isEnabled = true
                    binding.btnRetake.visibility = View.VISIBLE
                    binding.btnTakePhoto.visibility = View.GONE
//                    Snackbar.make(binding.root,
//                        "Photo captured",
//                        Snackbar.LENGTH_SHORT
//                    ).show()
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // only unbind — do not shut down any executor here
        cameraProvider?.unbindAll()
        _binding = null
    }
}
