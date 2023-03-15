package com.diana.bachelorthesis.view

import android.content.Context
import android.os.Bundle
import android.view.Menu
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.ActivityMainBinding

enum class WindowSizeClass { COMPACT, MEDIUM, EXPANDED }

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_add_item, R.id.nav_recommendations,
                R.id.nav_chat, R.id.nav_favorites, R.id.nav_map,
                R.id.nav_history // TODO add here the rest of the ids
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // computeWindowSizeClasses()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

//    private fun computeWindowSizeClasses() {
//        val metrics = WindowMetricsCalculator.getOrCreate()
//            .computeCurrentWindowMetrics(this)
//
//        val widthDp = metrics.bounds.width() /
//                resources.displayMetrics.density
//        val widthWindowSizeClass = when {
//            widthDp < 600f -> WindowSizeClass.COMPACT
//            widthDp < 840f -> WindowSizeClass.MEDIUM
//            else -> WindowSizeClass.EXPANDED
//        }
//
//        val heightDp = metrics.bounds.height() /
//                resources.displayMetrics.density
//        val heightWindowSizeClass = when {
//            heightDp < 480f -> WindowSizeClass.COMPACT
//            heightDp < 900f -> WindowSizeClass.MEDIUM
//            else -> WindowSizeClass.EXPANDED
//        }
//
//        // Use widthWindowSizeClass and heightWindowSizeClass.
//    }

//    companion object {
//        fun getScreenPixelDensity(): Float {
//            val screenPixelDensity = context.resources.displayMetrics.density
//            val dpValue = pixels / screenPixelDensity
//            return dpValue
//        }
//    }
}