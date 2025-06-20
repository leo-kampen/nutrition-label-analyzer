package com.example.nutritionlabelapp

import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.nutritionlabelapp.databinding.ActivityMainBinding
import com.example.nutritionlabelapp.theme.ThemeManager
import com.example.nutritionlabelapp.theme.ThemeOption

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // ─── 0) Drive the AppCompat night mode based on your saved option ───────
        when (ThemeManager.getThemeOption(this)) {
            ThemeOption.DARK  ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            ThemeOption.LIGHT ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)

        // ─── 1) Inflate & bind ────────────────────────────────────────────────
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ─── 2) Grab your colours for the current theme ───────────────────────
        val colors  = ThemeManager.getThemeColors(this)
        val isLight = ThemeManager.getThemeOption(this) == ThemeOption.LIGHT

        // ─── 3) Window & status/nav bar backgrounds ──────────────────────────
        window.setBackgroundDrawable(ColorDrawable(colors.backgroundColor))
        window.statusBarColor     = colors.toolbarColor
        window.navigationBarColor = colors.toolbarColor

        // ─── 4) Light/dark icons on status + nav bars ────────────────────────
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars     = isLight
            isAppearanceLightNavigationBars = isLight
        }

        // ─── 5) ActionBar (toolbar) background ───────────────────────────────
        supportActionBar?.setBackgroundDrawable(ColorDrawable(colors.toolbarColor))

        // ─── 6) Set up Navigation with BottomNavView ─────────────────────────
        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController

        binding.bottomNav.apply {
            // wire bottom nav to navController
            setupWithNavController(navController)

            // tint the bar itself + icons/text
            setBackgroundColor(colors.toolbarColor)
            itemIconTintList = ColorStateList.valueOf(colors.userTextColor)
            itemTextColor    = ColorStateList.valueOf(colors.userTextColor)
        }

        // ─── 7) Hide the BottomNav when on the camera screen ───────────────────
        navController.addOnDestinationChangedListener { _, dest, _ ->
            binding.bottomNav.visibility =
                if (dest.id == R.id.cameraFragment) View.GONE else View.VISIBLE
        }
    }
}
