package com.diana.bachelorthesis.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.ActivityMainBinding
import com.diana.bachelorthesis.utils.SharedPreferencesUtils
import com.diana.bachelorthesis.viewmodel.UserViewModel
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {
    private val TAG: String = MainActivity::class.java.name

    private lateinit var appBarConfiguration: AppBarConfiguration
    private var _binding: ActivityMainBinding? = null

    private val binding get() = _binding!!

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController
    lateinit var navView: NavigationView
    private lateinit var headerLayout: View
    lateinit var userViewModel: UserViewModel
    lateinit var sharedPref: SharedPreferences
    var returnedHomeFromItemPage: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        drawerLayout = binding.drawerLayout
        navView = binding.navView
        headerLayout = navView.getHeaderView(0)
        navController = findNavController(R.id.nav_host_fragment_content_main)

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

        navView.setNavigationItemSelectedListener { item: MenuItem ->
            // Block access to certain fragments unless the user is authenticated
            if (!userViewModel.verifyUserLoggedIn() && (item.itemId == R.id.nav_add_item || item.itemId == R.id.nav_map)) {
                navController.navigate(R.id.nav_intro_auth)
                drawerLayout.closeDrawer(GravityCompat.START)
                true

            } else {
                // Treat other normal cases
                val handled = NavigationUI.onNavDestinationSelected(item, navController)
                if (handled) drawerLayout.closeDrawer(GravityCompat.START)
                handled
            }
        }
        sharedPref = getPreferences(Context.MODE_PRIVATE)
        setDefaultSharedPreferencesHomeOptions()
    }

    override fun onStart() {
        super.onStart()
        initListeners()
        updateNavHeader()
        updateIconAppBar()
        updateMenuItemsVisibility()
    }

     fun updateMenuItemsVisibility() {
        val userLogged = userViewModel.verifyUserLoggedIn()
        navView.menu.findItem(R.id.nav_recommendations).isVisible = userLogged
        navView.menu.findItem(R.id.nav_favorites).isVisible = userLogged
        navView.menu.findItem(R.id.nav_chat).isVisible = userLogged
        navView.menu.findItem(R.id.nav_history).isVisible = userLogged
    }

    fun updateIconAppBar() {
        Log.d(TAG, "Updating icon from app bar")
        if (userViewModel.verifyUserLoggedIn()) {
            binding.appBarMain.iconAppBar.setImageResource(R.drawable.ic_person)
        } else {
            binding.appBarMain.iconAppBar.setImageResource(R.drawable.ic_login)
        }
    }

    fun updateNavHeader() {
        Log.d(TAG, "Updating navigation header")
        val btnAuth = headerLayout.findViewById<Button>(R.id.btnAuthNavHeader)
        val nameTextView = headerLayout.findViewById<TextView>(R.id.nameNavHeader)
        val emailTextView = headerLayout.findViewById<TextView>(R.id.emailNavHeader)
        val photoImageView = headerLayout.findViewById<ImageView>(R.id.photoNavHeader)

        if (userViewModel.verifyUserLoggedIn()) {
            val currentUser = userViewModel.getCurrentUser()
            btnAuth.visibility = View.GONE

            nameTextView.apply {
                visibility = View.VISIBLE
                text = currentUser.name
            }
            emailTextView.apply {
                visibility = View.VISIBLE
                text = currentUser.email
            }
            photoImageView.visibility = View.VISIBLE
            Picasso.get().load(currentUser.profilePhoto).into(photoImageView)
        } else {
            btnAuth.visibility = View.VISIBLE
            nameTextView.visibility = View.INVISIBLE
            emailTextView.visibility = View.INVISIBLE
            photoImageView.visibility = View.INVISIBLE
        }
    }

    private fun initListeners() {
        binding.appBarMain.iconAppBar.setOnClickListener { view ->
            if (userViewModel.verifyUserLoggedIn()) {
                navController.navigate(R.id.nav_profile)
            } else {
                navController.navigate(R.id.nav_intro_auth)
            }
        }
        headerLayout.findViewById<Button>(R.id.btnAuthNavHeader)?.setOnClickListener { view ->
            if (!userViewModel.verifyUserLoggedIn()) {
                drawerLayout.closeDrawer(GravityCompat.START)
                navController.navigate(R.id.nav_intro_auth)
            }
        }

        headerLayout.findViewById<ImageView>(R.id.photoNavHeader)?.setOnClickListener { view ->
            goToProfileFromMenu()
        }

        headerLayout.findViewById<TextView>(R.id.nameNavHeader)?.setOnClickListener { view ->
            goToProfileFromMenu()
        }

        headerLayout.findViewById<TextView>(R.id.emailNavHeader)?.setOnClickListener { view ->
            goToProfileFromMenu()
        }
    }

    private fun goToProfileFromMenu() {
        if (userViewModel.verifyUserLoggedIn()) {
            drawerLayout.closeDrawer(GravityCompat.START)
            navController.navigate(R.id.nav_profile)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        Log.d(TAG, "Back pressed")
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            Log.d(TAG, "Drawer was open during back press")
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            if (navController.currentBackStackEntry != null && navController.previousBackStackEntry != null) {
                if (navController.currentBackStackEntry!!.destination.id == R.id.nav_item &&
                    navController.previousBackStackEntry!!.destination.id == R.id.nav_home
                ) {
                    returnedHomeFromItemPage = true
                }
            }
            super.onBackPressed()
        }
    }

    private fun setDefaultSharedPreferencesHomeOptions() {
        Log.d(TAG, "Set Shared Preferences values for home options to default.")
        with(sharedPref.edit()) {
            putString(SharedPreferencesUtils.sharedprefSearch, "")
            putInt(SharedPreferencesUtils.sharedprefSortOption, 0)
            putString(SharedPreferencesUtils.sharedprefCityFilter,"")
            putString(SharedPreferencesUtils.sharedPrefCategoriesFilter, "")
            apply() // asynchronously
        }
    }

    fun updateAuthUIElements() {
        updateIconAppBar()
        updateNavHeader()
        updateMenuItemsVisibility()
    }

}