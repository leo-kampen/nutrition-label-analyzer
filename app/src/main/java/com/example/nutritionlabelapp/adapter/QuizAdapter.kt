package com.example.nutritionlabelapp.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nutritionlabelapp.databinding.ItemQuizQuestionBinding
import com.example.nutritionlabelapp.model.QuizQuestion

class QuizAdapter(
    private val questions: List<QuizQuestion>,
    private val onChoiceChanged: (QuizQuestion) -> Unit
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

        fun bind(q: QuizQuestion) {
            binding.textQuestion.text = q.prompt

            // Clear previous UI
            binding.radioGroup.removeAllViews()
            binding.etOther.apply {
                setText(q.otherText ?: "")
                visibility = View.GONE
                // remove old watcher
                (tag as? TextWatcher)?.let { removeTextChangedListener(it) }
            }

            // Dynamically add options
            if (q.multiSelect) {
                q.options.forEachIndexed { idx, label ->
                    val cb = CheckBox(binding.root.context).apply {
                        text = label
                        isChecked = q.selectedIndices.contains(idx)
                    }
                    cb.setOnCheckedChangeListener { _, checked ->
                        if (checked) q.selectedIndices.add(idx)
                        else q.selectedIndices.remove(idx)
                        onChoiceChanged(q)
                    }
                    binding.radioGroup.addView(cb)
                }
                // “Other” as a CheckBox
                val cbOther = CheckBox(binding.root.context).apply {
                    text = "Other"
                    isChecked = q.selectedIndices.contains(q.options.size)
                }
                cbOther.setOnCheckedChangeListener { _, checked ->
                    if (checked) {
                        q.selectedIndices.add(q.options.size)
                        binding.etOther.visibility = View.VISIBLE
                    } else {
                        q.selectedIndices.remove(q.options.size)
                        q.otherText = null
                        binding.etOther.visibility = View.GONE
                    }
                    onChoiceChanged(q)
                }
                binding.radioGroup.addView(cbOther)
            } else {
                // single-select
                q.options.forEachIndexed { idx, label ->
                    val rb = RadioButton(binding.root.context).apply {
                        text = label
                        id = idx
                        isChecked = (q.selectedIndex == idx)
                    }
                    binding.radioGroup.addView(rb)
                }
                // “Other” radio button
                val rbOther = RadioButton(binding.root.context).apply {
                    text = "Other"
                    id = q.options.size
                    isChecked = (q.selectedIndex == q.options.size)
                }
                binding.radioGroup.addView(rbOther)

                binding.radioGroup.setOnCheckedChangeListener { _: RadioGroup, checkedId: Int ->
                    q.selectedIndex = checkedId
                    // show/hide EditText
                    binding.etOther.visibility =
                        if (checkedId == q.options.size) View.VISIBLE else View.GONE
                    onChoiceChanged(q)
                }
            }

            // Watch for free-form text
            val tw = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    q.otherText = s?.toString()
                    onChoiceChanged(q)
                }
                override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
                override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            }
            binding.etOther.addTextChangedListener(tw)
            binding.etOther.tag = tw
        }
    }
}
