package com.example.nutritionlabelapp

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.nutritionlabelapp.databinding.FragmentMoreBinding
import com.example.nutritionlabelapp.theme.ThemeManager
import com.example.nutritionlabelapp.theme.ThemeOption

class MoreFragment : Fragment(R.layout.fragment_more) {
    private var _binding: FragmentMoreBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMoreBinding.bind(view)

        // 1) background
        binding.root.setBackgroundColor(
            ThemeManager.getThemeColors(requireContext()).backgroundColor
        )

        // 2) toolbar
        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            binding.toolbar.title = "Settings"
        }

        // 3) switch
        val switch = binding.switchDarkMode
        switch.isChecked = (ThemeManager.getThemeOption(requireContext()) == ThemeOption.DARK)
        switch.setTextColor(ThemeManager.getThemeColors(requireContext()).userTextColor)
        switch.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            val opt = if (isChecked) ThemeOption.DARK else ThemeOption.LIGHT
            ThemeManager.setThemeOption(requireContext(), opt)
            requireActivity().recreate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
