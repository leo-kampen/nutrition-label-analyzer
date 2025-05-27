// com/example/nutritionlabelapp/adapter/QuizAdapter.kt
package com.example.nutritionlabelapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import com.example.nutritionlabelapp.databinding.ItemQuizQuestionBinding
import com.example.nutritionlabelapp.model.QuizQuestion

class QuizAdapter(
    private val questions: List<QuizQuestion>,
    private val onChoiceSelected: (QuizQuestion) -> Unit
) : RecyclerView.Adapter<QuizAdapter.QuestionVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionVH {
        val binding = ItemQuizQuestionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return QuestionVH(binding)
    }

    override fun onBindViewHolder(holder: QuestionVH, position: Int) =
        holder.bind(questions[position])

    override fun getItemCount() = questions.size

    inner class QuestionVH(
        private val binding: ItemQuizQuestionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: QuizQuestion) {
            binding.textQuestion.text = item.prompt

            // Clear any recycled radio buttons before adding new ones
            binding.radioGroup.removeAllViews()

            item.options.forEachIndexed { index, label ->
                val rb = RadioButton(binding.root.context).apply {
                    text = label
                    id = index
                    isChecked = item.selectedIndex == index
                }
                binding.radioGroup.addView(rb)
            }

            binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
                item.selectedIndex = checkedId
                onChoiceSelected(item)
            }
        }
    }
}
