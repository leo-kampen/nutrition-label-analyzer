package com.example.nutritionlabelapp

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutritionlabelapp.adapter.RecipesAdapter
import com.example.nutritionlabelapp.databinding.FragmentRecipesBinding
import com.example.nutritionlabelapp.network.GenerateRequest
import com.example.nutritionlabelapp.network.RetrofitClient
import com.example.nutritionlabelapp.theme.ThemeManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import com.example.nutritionlabelapp.data.LocalStorage


class RecipesFragment : Fragment(R.layout.fragment_recipes) {

    private var _binding: FragmentRecipesBinding? = null
    private val binding get() = _binding!!

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var recipesAdapter: RecipesAdapter
    private val recipes = mutableListOf<String>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRecipesBinding.bind(view)

        // Toolbar + back arrow
        val colors = ThemeManager.getThemeColors(requireContext())
        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            binding.toolbar.setBackgroundColor(colors.toolbarColor)
            binding.toolbar.setTitleTextColor(colors.userTextColor)
            binding.toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }

        // Keep Create button above BottomNav
        val bottomNav = requireActivity()
            .findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    bottomNav.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val navBarHeight = bottomNav.height
                    val extraPx = (16 * resources.displayMetrics.density).toInt()
                    val params = binding.btnCreate.layoutParams as ConstraintLayout.LayoutParams
                    params.bottomMargin = navBarHeight + extraPx
                    binding.btnCreate.layoutParams = params
                }
            }
        )

        // RecyclerView + adapter
        recipesAdapter = RecipesAdapter()
        binding.rvRecipes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecipes.adapter = recipesAdapter

        // Restore any saved recipes
        recipes.addAll(LocalStorage.loadRecipes(requireContext()))
        if (recipes.isNotEmpty()) {
            recipesAdapter.submitList(recipes.toList())
            binding.rvRecipes.scrollToPosition(0)
        }


        // “Create Recipes” button
        binding.btnCreate.setOnClickListener {
            generateRecipes()
        }
    }

    private fun generateRecipes() {
        recipes.clear()
        // 1) placeholder
        recipesAdapter.submitList(listOf("Creating recipes…"))
        binding.rvRecipes.scrollToPosition(0)

        // 2) fetch most recent quiz
        firestore.collection("quiz_responses")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { snap ->
                val doc = snap.documents.firstOrNull()
                val quizMap = doc
                    ?.data
                    ?.filterKeys { it.startsWith("q") }
                    ?.mapValues { it.value.toString() }
                    ?: emptyMap()

                // 3) parse Excel
                val foodData = parseExcelFromAssets("Common_Foods_By_Food_Group_ex.xlsx")
                // 4) build and send prompt
                val prompt = buildPrompt(quizMap, foodData)
                callGenerate(prompt)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Failed to load quiz data",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            }
    }

    private fun buildPrompt(
        quiz: Map<String, String>,
        foods: Map<String, List<String>>
    ): String {
        val questionTitles = mapOf(
            "q1" to "How would you describe your eating preferences?",
            "q2" to "Do you follow any dietary restrictions?",
            "q3" to "Do you follow any cultural or religious dietary guidelines?",
            "q4" to "What kitchen equipment do you regularly have access to?",
            "q5" to "How much time do you typically want to spend preparing meals?",
            "q6" to "What kind of recipes or food ideas interest you most?",
            "q7" to "What is your weekly budget",
            "q8" to "How many people do you usually cook for?",
            "q9" to "What state do you live in?",
            "q10" to "Anything else we should know?"
        )

        return buildString {
            append("User quiz answers:\n")
            quiz.forEach { (key, answer) ->
                val questionText = questionTitles[key] ?: key
                append("$questionText: $answer\n")
            }
            append("\nAvailable food items:\n")
            foods.forEach { (category, items) ->
                append("$category: ${items.joinToString(", ")}\n")
            }
            append(
                "\nPlease suggest exactly seven recipes using these ingredients." +
                        " For each recipe, provide a title, list of ingredients, and step-by-step instructions." +
                        " Include an estimated total price right below the title." +
                        " Base the total cost of the meal on these answers: 'How many people do you usually cook for?' and 'What state do you live in?'." +
                        " Also use this website to help generate the cost for each meal: https://www.budgetbytes.com/how-to-calculate-recipe-costs/\n"            )


        }
    }

    private fun callGenerate(prompt: String) {
        lifecycleScope.launch {
            val (success, responseText) = withContext(Dispatchers.IO) {
                try {
                    val req = GenerateRequest(
                        model  = "llama4",
                        prompt = prompt,
                        images = null,
                        stream = false
                    )
                    val resp = RetrofitClient.ollamaService.generate(req)
                    if (resp.isSuccessful) {
                        true to (resp.body()?.response ?: "No response.")
                    } else {
                        false to (resp.errorBody()?.string() ?: resp.message())
                    }
                } catch (e: Exception) {
                    false to "Request failed: ${e.localizedMessage}"
                }
            }

            val finalText = if (success) responseText else "Error: $responseText"
            //recipesAdapter.submitList(listOf(finalText))
            recipes.clear()
            recipes.add(finalText)
            recipesAdapter.submitList(recipes.toList())
            binding.rvRecipes.scrollToPosition(0)
        }
    }

    private fun parseExcelFromAssets(filename: String): Map<String, List<String>> {
        val result = mutableMapOf<String, MutableList<String>>()
        requireContext().assets.open(filename).use { input ->
            XSSFWorkbook(input).use { wb ->
                val sheet     = wb.getSheetAt(0)
                val headerRow = sheet.getRow(0)
                val cols      = headerRow.lastCellNum.toInt()
                for (ci in 0 until cols) {
                    val header = headerRow.getCell(ci).stringCellValue.trim()
                    result[header] = mutableListOf()
                }
                for (ri in 1..sheet.lastRowNum) {
                    sheet.getRow(ri)?.let { row ->
                        for (ci in 0 until cols) {
                            row.getCell(ci)?.let { cell ->
                                val text = when (cell.cellType) {
                                    CellType.STRING  -> cell.stringCellValue.trim()
                                    CellType.NUMERIC -> cell.numericCellValue.toString()
                                    else             -> cell.toString().trim()
                                }
                                if (text.isNotEmpty()) {
                                    val key = headerRow.getCell(ci).stringCellValue.trim()
                                    result[key]?.add(text)
                                }
                            }
                        }
                    }
                }
            }
        }
        return result
    }

    override fun onPause() {
        super.onPause()
        LocalStorage.saveRecipes(requireContext(), recipes)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
