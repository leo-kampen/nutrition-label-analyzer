package com.example.nutritionlabelapp.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.ContextCompat
import com.example.nutritionlabelapp.R

enum class ThemeOption(val key: String) {
    LIGHT("light"),
    DARK ("dark");

    companion object {
        fun fromKey(key: String?): ThemeOption =
            values().firstOrNull { it.key == key } ?: LIGHT
    }
}

/** Add the three new colors here at the end. */
data class ThemeColors(
    val backgroundColor: Int,
    val toolbarColor:    Int,
    val botBubbleColor:  Int,
    val botTextColor:    Int,
    val userBubbleColor: Int,
    val userTextColor:   Int,

    // NEW ↓↓↓
    val fabIconColor:    Int,
    val inputTextColor:  Int,
    val inputHintColor:  Int
)

object ThemeManager {
    private const val PREFS = "theme_prefs"
    private const val KEY   = "selected_theme"

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun setThemeOption(ctx: Context, option: ThemeOption) {
        prefs(ctx).edit().putString(KEY, option.key).apply()
    }

    fun getThemeOption(ctx: Context): ThemeOption =
        ThemeOption.fromKey(prefs(ctx).getString(KEY, null))

    fun getThemeColors(ctx: Context): ThemeColors {
        val res = ctx.resources
        return when (getThemeOption(ctx)) {
            ThemeOption.DARK -> ThemeColors(
                backgroundColor = ContextCompat.getColor(ctx, R.color.dark_background),
                toolbarColor    = ContextCompat.getColor(ctx, R.color.dark_toolbar),
                botBubbleColor  = ContextCompat.getColor(ctx, R.color.dark_bot_bubble),
                botTextColor    = ContextCompat.getColor(ctx, R.color.dark_bot_text),
                userBubbleColor = ContextCompat.getColor(ctx, R.color.dark_user_bubble),
                userTextColor   = ContextCompat.getColor(ctx, R.color.dark_user_text),

                // NEW ↓↓↓
                fabIconColor    = ContextCompat.getColor(ctx, R.color.dark_fab_icon),
                inputTextColor  = ContextCompat.getColor(ctx, R.color.dark_input_text),
                inputHintColor  = ContextCompat.getColor(ctx, R.color.dark_input_hint)
            )
            ThemeOption.LIGHT -> ThemeColors(
                backgroundColor = ContextCompat.getColor(ctx, R.color.light_background),
                toolbarColor    = ContextCompat.getColor(ctx, R.color.light_toolbar),
                botBubbleColor  = ContextCompat.getColor(ctx, R.color.light_bot_bubble),
                botTextColor    = ContextCompat.getColor(ctx, R.color.light_bot_text),
                userBubbleColor = ContextCompat.getColor(ctx, R.color.light_user_bubble),
                userTextColor   = ContextCompat.getColor(ctx, R.color.light_user_text),

                // NEW ↓↓↓
                fabIconColor    = ContextCompat.getColor(ctx, R.color.light_fab_icon),
                inputTextColor  = ContextCompat.getColor(ctx, R.color.light_input_text),
                inputHintColor  = ContextCompat.getColor(ctx, R.color.light_input_hint)
            )
        }
    }
}
