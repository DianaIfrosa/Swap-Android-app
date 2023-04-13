package com.diana.bachelorthesis.view

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.ui.*
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity()
//    , NavigationView.OnNavigationItemSelectedListener
{
    private val TAG: String = MainActivity::class.java.name

    private lateinit var appBarConfiguration: AppBarConfiguration
    private var _binding: ActivityMainBinding? = null

    private val binding get() = _binding!!

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController
    private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        drawerLayout = binding.drawerLayout
        navView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_main)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            navController.graph, drawerLayout
        )

        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)
        NavigationUI.setupWithNavController(navView, navController)

        // make other menu options inaccessible to user
        navController.addOnDestinationChangedListener { nc: NavController, nd: NavDestination, _: Bundle? ->
            if (nd.id == nc.graph.startDestinationId) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, drawerLayout)
    }

    override fun onBackPressed() {
        Log.d(TAG, "Back pressed")
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            Log.d(TAG, "Drawer was open during back press")
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

//    override fun onNavigationItemSelected(item: MenuItem): Boolean {
//        drawerLayout.close()
//        // TODO change this!!
//
//        // this part checks if current fragment is the same as destination
//        return if (navController.currentDestination?.id != item.itemId) {
//            val builder = NavOptions.Builder()
//                .setLaunchSingleTop(true)
//                .setEnterAnim(R.anim.enter_left_to_right)
//                .setExitAnim(R.anim.exit_right_to_left)
//                .setPopEnterAnim(R.anim.popenter_right_to_left)
//                .setPopExitAnim(R.anim.popexit_left_to_right)
//
//            // this part set proper pop up destination to prevent "looping" fragments
//            // deleted
//
//            val options = builder.build()
//            return try {
//               navController.navigate(
//                    item.itemId,
//                    null,
//                    options
//                )
//                true
//            } catch (e: IllegalArgumentException) // couldn't find destination, do nothing
//            {
//                false
//            }
//        } else {
//            false
//        }
//    }
}