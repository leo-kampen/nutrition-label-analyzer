//
//// File: app/src/main/java/com/example/nutritionlabelapp/CameraFragment.kt
//package com.example.nutritionlabelapp
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.net.Uri
//import android.os.Bundle
//import android.util.Size
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Toast
//import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
//import androidx.core.content.ContextCompat
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//import com.example.nutritionlabelapp.databinding.FragmentCameraBinding
//import androidx.camera.core.CameraSelector
//import androidx.camera.core.ImageCapture
//import androidx.camera.core.ImageCaptureException
//import androidx.camera.core.Preview
//import androidx.camera.lifecycle.ProcessCameraProvider
//import com.google.android.material.snackbar.Snackbar
//import java.io.File
//import java.util.concurrent.ExecutorService
//import java.util.concurrent.Executors
//
//
//class CameraFragment : Fragment() {
//    private var _binding: FragmentCameraBinding? = null
//    private val binding get() = _binding!!
//
//    private var cameraProvider: ProcessCameraProvider? = null
//    private lateinit var cameraExecutor: ExecutorService
//    private var imageCapture: ImageCapture? = null
//    private var imageUri: Uri? = null
//
//    private val requestCameraPermission =
//        registerForActivityResult(RequestPermission()) { granted ->
//            if (granted) startCamera()
//            else {
//                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
//                cleanUpCamera()
//                findNavController().navigateUp()
//            }
//        }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        cameraExecutor = Executors.newSingleThreadExecutor()
//    }
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
//
//        binding.cameraToolbar.setNavigationOnClickListener {
//            cleanUpCamera()
//            findNavController().navigateUp()
//        }
//
//        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
//            == PackageManager.PERMISSION_GRANTED
//        ) startCamera()
//        else requestCameraPermission.launch(Manifest.permission.CAMERA)
//
//        binding.btnTakePhoto.setOnClickListener { takePhoto() }
//        binding.btnRetake.setOnClickListener {
//            imageUri = null
//            binding.ivCapturedPhoto.visibility = View.GONE
//            binding.previewView.visibility = View.VISIBLE
//            binding.btnAnalyze.isEnabled = false
//            binding.btnRetake.visibility = View.GONE
//            binding.btnTakePhoto.visibility = View.VISIBLE
//        }
//        binding.btnAnalyze.setOnClickListener {
//            imageUri?.let { uri ->
//                parentFragmentManager.setFragmentResult(
//                    "nutrition_image_request",
//                    Bundle().apply { putString("imageUri", uri.toString()) }
//                )
//                cleanUpCamera()
//                findNavController().navigateUp()
//            }
//        }
//    }
//
//    private fun startCamera() {
//        ProcessCameraProvider.getInstance(requireContext()).let { future ->
//            future.addListener({
//                cameraProvider = future.get().also { provider ->
//                    provider.unbindAll()
//
//                    val preview = Preview.Builder()
//                        .setTargetResolution(Size(640, 480))
//                        .build()
//                        .also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }
//
//                    imageCapture = ImageCapture.Builder()
//                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
//                        .build()
//
//                    try {
//                        provider.bindToLifecycle(
//                            viewLifecycleOwner,
//                            CameraSelector.DEFAULT_BACK_CAMERA,
//                            preview,
//                            imageCapture
//                        )
//                    } catch (exc: Exception) {
//                        Snackbar.make(binding.root,
//                            "Camera initialization failed",
//                            Snackbar.LENGTH_LONG
//                        ).show()
//                    }
//                }
//            }, ContextCompat.getMainExecutor(requireContext()))
//        }
//    }
//
//    private fun takePhoto() {
//        val file = File(
//            requireContext().externalMediaDirs.first(),
//            "IMG_${System.currentTimeMillis()}.jpg"
//        )
//        val options = ImageCapture.OutputFileOptions.Builder(file).build()
//        imageCapture?.takePicture(
//            options,
//            cameraExecutor,
//            object : ImageCapture.OnImageSavedCallback {
//                override fun onError(exc: ImageCaptureException) {
//                    binding.root.post {
//                        Snackbar.make(binding.root,
//                            "Photo capture failed",
//                            Snackbar.LENGTH_LONG
//                        ).show()
//                    }
//                }
//
//                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
//                    val uri = Uri.fromFile(file)
//                    imageUri = uri
//
//                    binding.root.post {
//                        binding.ivCapturedPhoto.setImageURI(uri)
//                        binding.ivCapturedPhoto.visibility = View.VISIBLE
//                        binding.previewView.visibility = View.GONE
//                        binding.btnAnalyze.isEnabled = true
//                        binding.btnRetake.visibility = View.VISIBLE
//                        binding.btnTakePhoto.visibility = View.GONE
////                        Snackbar.make(binding.root,
////                            "Photo captured",
////                            Snackbar.LENGTH_SHORT
////                        ).show()
//                    }
//                }
//            }
//        )
//    }
//
//
//    private fun cleanUpCamera() {
//        // 1) detach all camera use-cases
//        cameraProvider?.unbindAll()
//
//          // 2) kill your local capture callback executor
//        if (::cameraExecutor.isInitialized && !cameraExecutor.isShutdown) {
//            cameraExecutor.shutdownNow()
//            }
//
//        //(requireActivity().application as MyApp).cameraXExecutor.shutdownNow()
//    }
//
//
//
//
//
//    override fun onPause() {
//        super.onPause()
//        cleanUpCamera()
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        // unbind use-cases so the camera hardware is free
//        cameraProvider?.unbindAll()
//        // shut down your single-thread image-capture executor
//        if (::cameraExecutor.isInitialized && !cameraExecutor.isShutdown) {
//            cameraExecutor.shutdownNow()
//        }
//        _binding = null
//    }
//
//}
//
