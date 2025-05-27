// com/example/nutritionlabelapp/model/QuizQuestion.kt
package com.example.nutritionlabelapp.model

data class QuizQuestion(
    val id: Int,
    val prompt: String,
    val options: List<String>,
    var selectedIndex: Int? = null
)
