package com.example.nutritionlabelapp

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.nutritionlabelapp.databinding.FragmentMainBinding
import com.example.nutritionlabelapp.theme.ThemeManager
import com.example.nutritionlabelapp.theme.ThemeOption
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class MainFragment : Fragment(R.layout.fragment_main) {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMainBinding.bind(view)

        // Apply themed toolbar color
        val colors = ThemeManager.getThemeColors(requireContext())
        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            binding.toolbar.setBackgroundColor(colors.toolbarColor)
            binding.toolbar.setTitleTextColor(colors.userTextColor)
        }

        // Load & display the Excel-based food list (unchanged)
        val data = parseExcelFromAssets("Common_Foods_By_Food_Group_ex.xlsx")
        val container = binding.container
        data.forEach { (category, items) ->
            TextView(requireContext()).apply {
                text = category; textSize = 18f; setPadding(0,16,0,8)
                setTextColor(colors.userTextColor)
                container.addView(this)
            }
            items.forEach { food ->
                TextView(requireContext()).apply {
                    text = "• $food"; setPadding(16,4,0,4)
                    setTextColor(colors.userTextColor)
                    container.addView(this)
                }
            }
        }

        // Buttons
        binding.btnTakeQuiz.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_homeFragment)
        }
        binding.btnMakeRecipes.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_recipesFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun parseExcelFromAssets(filename: String): Map<String, List<String>> {
        val result = mutableMapOf<String, MutableList<String>>()
        requireContext().assets.open(filename).use { input ->
            XSSFWorkbook(input).use { wb ->
                val sheet = wb.getSheetAt(0)
                val headerRow = sheet.getRow(0)
                val numCols = headerRow.lastCellNum.toInt()

                // initialize lists
                for (ci in 0 until numCols) {
                    val header = headerRow.getCell(ci).stringCellValue.trim()
                    result[header] = mutableListOf()
                }

                // read each row, starting at row 1
                for (ri in 1..sheet.lastRowNum) {
                    val row = sheet.getRow(ri) ?: continue
                    for (ci in 0 until numCols) {
                        val cell = row.getCell(ci) ?: continue
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
        return result
    }
}
