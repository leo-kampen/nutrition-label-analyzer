package com.example.nutritionlabelapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutritionlabelapp.adapter.QuizAdapter
import com.example.nutritionlabelapp.databinding.FragmentHomeBinding
import com.example.nutritionlabelapp.model.QuizQuestion
import com.example.nutritionlabelapp.theme.ThemeManager
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val firestore = FirebaseFirestore.getInstance()
    private val questions = mutableListOf<QuizQuestion>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        // Theme & toolbar
        binding.root.setBackgroundColor(
            ThemeManager.getThemeColors(requireContext()).backgroundColor
        )
        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            binding.toolbar.title = "Home"
        }

        // Dummy questions
        questions.addAll(listOf(
            QuizQuestion(1, "🍎 What’s your primary dietary goal?", listOf(
                "Lose weight", "Build muscle", "Maintain weight", "Improve energy"
            )),
            QuizQuestion(2, "🥑 Food preference?", listOf(
                "Omnivore", "Vegetarian", "Vegan", "Keto", "Pescatarian"
            )),
            QuizQuestion(3, "🥛 Any allergies?", listOf(
                "Dairy", "Gluten", "Nuts", "Soy", "None"
            )),
            QuizQuestion(4, "🏃‍♂️ Typical activity level?", listOf(
                "Sedentary", "Lightly active", "Active", "Very active"
            )),
            QuizQuestion(5, "⏱️ Preferred meal prep time?", listOf(
                "<15 min", "15–30 min", "30–45 min", ">45 min"
            ))
        ))

        // RecyclerView + adapter
        binding.recyclerQuiz.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerQuiz.adapter = QuizAdapter(questions) {
            binding.btnSubmit.isEnabled = questions.all { it.selectedIndex != null }
        }

        // Submit → Firestore + navigate
        binding.btnSubmit.setOnClickListener {
            val payload = questions.associate { q ->
                "q${q.id}" to q.options[q.selectedIndex!!]
            }

            firestore.collection("quiz_responses")
                .add(payload)
                .addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        "Your answers have been saved!",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Navigate once saved
                    findNavController().navigate(
                        R.id.action_homeFragment_to_exampleFragment
                    )
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    Toast.makeText(
                        requireContext(),
                        "Failed to save answers. Please try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
