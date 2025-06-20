package com.example.nutritionlabelapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nutritionlabelapp.databinding.ItemRecipeBinding

class RecipesAdapter(
    private var items: List<String> = emptyList()
) : RecyclerView.Adapter<RecipesAdapter.RecipeVH>() {

    fun submitList(newItems: List<String>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeVH {
        val binding = ItemRecipeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RecipeVH(binding)
    }

    override fun onBindViewHolder(holder: RecipeVH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class RecipeVH(private val binding: ItemRecipeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(text: String) {
            binding.textRecipe.text = text
        }
    }
}
