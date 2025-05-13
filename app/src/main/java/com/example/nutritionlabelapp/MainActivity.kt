package com.example.nutritionlabelapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.nutritionlabelapp.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var imageCapture: ImageCapture? = null
    private var lastImageUri: Uri? = null
    private var photoTaken = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startCamera() else finish()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ask for CAMERA permission
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        binding.btnTakePhoto.setOnClickListener {
            if (!photoTaken) takePhoto() else resetCamera()
        }

        binding.btnAnalyze.setOnClickListener {
            lastImageUri?.let { uri ->
                startActivity(
                    Intent(this, AnalyzeActivity::class.java)
                        .putExtra("imageUri", uri.toString())
                )
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
            Runnable {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }
                imageCapture = ImageCapture.Builder().build()
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            }, ContextCompat.getMainExecutor(this)
        )
    }

    private fun takePhoto() {
        val ic = imageCapture ?: return
        val photoFile = File(
            externalMediaDirs.first(),
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        ic.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    exc.printStackTrace()
                }
                override fun onImageSaved(results: ImageCapture.OutputFileResults) {
                    lastImageUri = Uri.fromFile(photoFile)
                    photoTaken = true
                    // Freeze preview and show photo
                    binding.previewView.visibility = View.GONE
                    binding.imgPreview.apply {
                        setImageURI(lastImageUri)
                        visibility = View.VISIBLE
                    }
                    // Update buttons
                    binding.btnTakePhoto.text = getString(R.string.retake_photo)
                    binding.btnAnalyze.isEnabled = true
                }
            })
    }

    private fun resetCamera() {
        photoTaken = false
        lastImageUri = null
        // Hide photo, show live preview
        binding.imgPreview.visibility = View.GONE
        binding.previewView.visibility = View.VISIBLE
        // Reset buttons
        binding.btnTakePhoto.text = getString(R.string.take_photo)
        binding.btnAnalyze.isEnabled = false
    }
}