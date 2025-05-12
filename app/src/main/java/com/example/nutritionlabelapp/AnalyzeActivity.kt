package com.example.nutritionlabelapp

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.nutritionlabelapp.databinding.ActivityAnalyzeBinding

class AnalyzeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnalyzeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyzeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Load the image URI passed from MainActivity
        val uriString = intent.getStringExtra("imageUri")
        uriString?.let {
            val uri = Uri.parse(it)
            binding.imgLabel.setImageURI(uri)
        }

        // 2) (TODO) Upload to Firebase, call ChatGPT API, and show result:
        binding.tvResult.text = "Analyzing… (implementation pending)"
    }
}
