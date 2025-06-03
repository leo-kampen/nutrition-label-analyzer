package com.example.nutritionlabelapp.model

data class QuizQuestion(
    val id: Int,
    val prompt: String,
    val options: List<String>,
    val multiSelect: Boolean = false,
    var selectedIndex: Int? = null,
    var selectedIndices: MutableList<Int> = mutableListOf(),
    var otherText: String? = null    // NEW: stores free-form “Other” text
)
