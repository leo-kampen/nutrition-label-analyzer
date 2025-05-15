package com.example.nutritionlabelapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.nutritionlabelapp.databinding.ActivityMainBinding
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import android.view.View
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Find NavController
        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                    as NavHostFragment
        val navController = navHost.navController

        // 2) Tell NavigationUI which are our “roots”
        val appBarConfig = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.chatFragment,
                R.id.moreFragment
            )
        )

        // 3) Hook up BottomNav *and* hide it on Camera
        binding.bottomNav.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
//            binding.bottomNav.visibility =
//                if (destination.id == R.id.cameraFragment)
//                    View.GONE
//                else
//                    View.VISIBLE
        }
    }
}
