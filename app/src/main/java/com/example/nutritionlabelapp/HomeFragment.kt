package com.example.nutritionlabelapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.nutritionlabelapp.databinding.FragmentHomeBinding
import com.example.nutritionlabelapp.theme.ThemeManager

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        // Apply background color if you’re using themes:
        binding.root.setBackgroundColor(
            ThemeManager.getThemeColors(requireContext()).backgroundColor
        )

        // Wire up the toolbar
        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            // title is already set in XML, but you can override here if you like:
            binding.toolbar.title = "Home"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
