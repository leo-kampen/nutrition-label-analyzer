package com.example.nutritionlabelapp.data

import android.content.Context
import android.content.SharedPreferences
import com.example.nutritionlabelapp.adapter.ChatMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object LocalStorage {
    private const val PREF_CHAT = "chat_history_prefs"
    private const val KEY_HISTORY = "history"

    private const val PREF_RECIPES = "recipe_prefs"
    private const val KEY_RECIPES = "recipes"

    private val gson = Gson()

    private fun chatPrefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREF_CHAT, Context.MODE_PRIVATE)

    private fun recipePrefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREF_RECIPES, Context.MODE_PRIVATE)

    fun saveChat(ctx: Context, msgs: List<ChatMessage>) {
        val json = gson.toJson(msgs)
        chatPrefs(ctx).edit().putString(KEY_HISTORY, json).apply()
    }

    fun loadChat(ctx: Context): MutableList<ChatMessage> {
        val json = chatPrefs(ctx).getString(KEY_HISTORY, null) ?: return mutableListOf()
        val type = object : TypeToken<List<ChatMessage>>() {}.type
        return gson.fromJson<List<ChatMessage>>(json, type)?.toMutableList() ?: mutableListOf()
    }

    fun saveRecipes(ctx: Context, recs: List<String>) {
        val json = gson.toJson(recs)
        recipePrefs(ctx).edit().putString(KEY_RECIPES, json).apply()
    }

    fun loadRecipes(ctx: Context): MutableList<String> {
        val json = recipePrefs(ctx).getString(KEY_RECIPES, null) ?: return mutableListOf()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson<List<String>>(json, type)?.toMutableList() ?: mutableListOf()
    }
}