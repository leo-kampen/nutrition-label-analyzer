package com.example.nutritionlabelapp

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.example.nutritionlabelapp.databinding.FragmentExampleBinding
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class ExampleFragment : Fragment(R.layout.fragment_example) {

    private var _binding: FragmentExampleBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentExampleBinding.bind(view)

        // Parse the Excel & get map of Category → List<Items>
        val data = parseExcelFromAssets("Common_Foods_By_Food_Group_ex.xlsx")

        // Inflate into the container
        val container = binding.container
        data.forEach { (category, items) ->
            // Category header
            val headerView = TextView(requireContext()).apply {
                text = category
                textSize = 18f
                setPadding(0, 16, 0, 8)
            }
            container.addView(headerView)

            // Each item as bullet point
            items.forEach { item ->
                val itemView = TextView(requireContext()).apply {
                    text = "• $item"
                    setPadding(16, 4, 0, 4)
                }
                container.addView(itemView)
            }
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
