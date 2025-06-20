package com.example.nutritionlabelapp

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutritionlabelapp.adapter.QuizAdapter
import com.example.nutritionlabelapp.databinding.FragmentHomeBinding
import com.example.nutritionlabelapp.model.QuizQuestion
import com.example.nutritionlabelapp.theme.ThemeManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val firestore = FirebaseFirestore.getInstance()
    private val questions = mutableListOf<QuizQuestion>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        // Toolbar + back arrow
        binding.root.setBackgroundColor(
            ThemeManager.getThemeColors(requireContext()).backgroundColor
        )
        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            binding.toolbar.title = "Quiz"
            binding.toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }

        // Keep Submit button above BottomNav
        val bottomNav = requireActivity()
            .findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    bottomNav.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val navBarHeight = bottomNav.height
                    val extraPx = (16 * resources.displayMetrics.density).toInt()
                    val params = binding.btnSubmit.layoutParams
                            as ConstraintLayout.LayoutParams
                    params.bottomMargin = navBarHeight + extraPx
                    binding.btnSubmit.layoutParams = params
                }
            }
        )

        // Quiz questions
        questions.addAll(listOf(
            QuizQuestion(1, "How would you describe your eating preferences?", listOf(
                "Omnivore", "Pescatarian", "Vegetarian", "Vegan"
            ), multiSelect = true),
            QuizQuestion(2, "Do you follow any dietary restrictions?", listOf(
                "Dairy-free", "Gluten-free", "Nut-free", "Soy-free",
                "Egg-free", "Shellfish-free", "Low FODMAP", "None"
            ), multiSelect = true),
            QuizQuestion(3, "Do you follow any cultural or religious dietary guidelines?", listOf(
                "Halal", "Kosher", "None"
            ), multiSelect = true),
            QuizQuestion(4, "What kitchen equipment do you regularly have access to?", listOf(
                "Full kitchen (stove, oven, fridge, microwave)",
                "Basic kitchen (stove & fridge)",
                "Microwave & fridge only",
                "No equipment",
                "Shared kitchen"
            ), multiSelect = false),
            QuizQuestion(5, "How much time do you typically want to spend preparing meals?", listOf(
                "Never", "<20 min", "20–30 min", ">30 min"
            ), multiSelect = false),
            QuizQuestion(6, "What kind of recipes or food ideas interest you most?", listOf(
                "Quick & easy", "Budget-friendly", "Healthy & nutritious",
                "Cultural preferences", "Meal prep ideas", "Snacks & treats"
            ), multiSelect = true),
            QuizQuestion(7, "What is your weekly budget?", listOf(
                "I prefer not to answer"
            ), multiSelect = false),
            QuizQuestion(8, "How many people do you usually cook for?", listOf(
                "1", "2", "3-4", "5+", "I prefer not to answer"
            ), multiSelect = false),
            QuizQuestion(9, "What state do you live in?", listOf(
                "I prefer not to answer"
            ), multiSelect = false),
            QuizQuestion(10, "Anything else we should know?", listOf(
                "No"
            ), multiSelect = false)
        ))

        // RecyclerView + adapter
        binding.recyclerQuiz.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerQuiz.adapter = QuizAdapter(questions) {
            val ready = questions.all { q ->
                if (q.multiSelect) q.selectedIndices.isNotEmpty()
                else q.selectedIndex != null
            }
            binding.btnSubmit.isEnabled = ready
        }

        // Submit → save with server timestamp
        binding.btnSubmit.setOnClickListener {
            val payload = mutableMapOf<String, Any>()
            questions.forEach { q ->
                val key = "q${q.id}"
                val answer: Any = if (q.multiSelect) {
                    q.selectedIndices.map { idx ->
                        if (idx < q.options.size) q.options[idx]
                        else q.otherText.orEmpty()
                    }
                } else {
                    val idx = q.selectedIndex!!
                    if (idx < q.options.size) q.options[idx]
                    else q.otherText.orEmpty()
                }
                payload[key] = answer
            }
            // add server timestamp
            payload["createdAt"] = FieldValue.serverTimestamp()

            firestore.collection("quiz_responses")
                .add(payload)
                .addOnSuccessListener {
                    findNavController().navigate(R.id.action_homeFragment_to_mainFragment)
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    Toast.makeText(
                        requireContext(),
                        "Failed to save answers, please try again.",
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

