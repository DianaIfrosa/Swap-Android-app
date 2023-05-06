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
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.User
import com.diana.bachelorthesis.utils.SharedPreferencesUtils
import com.diana.bachelorthesis.viewmodel.MainViewModel
import com.diana.bachelorthesis.viewmodel.UserViewModel
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
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
    lateinit var mainViewModel: MainViewModel

    lateinit var sharedPref: SharedPreferences
    var returnedHomeFromItemPage: Boolean = false //TODO delete if no longer used

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)
        getViewModels()

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
        restoreCurrentUserData()
        updateAuthUIElements()
        setDefaultSharedPreferencesHomeOptions()
    }

    override fun onStart() {
        super.onStart()
        initListeners()
    }

    fun restoreCurrentUserData() {
        val gson = Gson()
        val json: String? = sharedPref.getString(SharedPreferencesUtils.sharedPrefCurrentUser, "")
        if (!json.isNullOrEmpty()) {
            val user: User = gson.fromJson(json, User::class.java)
            mainViewModel.currentUser = user
        }
    }

    private fun getViewModels() {
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
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
            val currentUser = getCurrentUser()!!
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

    fun deleteCurrentUserFromSharedPreferences() {
        mainViewModel.currentUser = null
        with(sharedPref.edit()) {
            remove(SharedPreferencesUtils.sharedPrefCurrentUser)
            commit() // synchronously
        }
    }

    fun addCurrentUserToSharedPreferences(user: User) {
        mainViewModel.currentUser = user
        with(sharedPref.edit()) {
            val gson = Gson()
            val json: String = gson.toJson(user)
            putString(SharedPreferencesUtils.sharedPrefCurrentUser, json)
            commit() // synchronously
        }
    }

    fun addFavoriteItem(item: Item) {
       mainViewModel.addFavoriteItem(item)
        updateCurrentUserSharedPreferences()
    }

    fun itemIsFavorite(item: Item): Boolean = mainViewModel.itemIsFavorite(item)


    fun removeFavoriteItem(item: Item) {
        mainViewModel.removeFavoriteItem(item)
        updateCurrentUserSharedPreferences()
    }

    fun getCurrentUser() = mainViewModel.currentUser

    private fun updateCurrentUserSharedPreferences() {
        with(sharedPref.edit()) {
            val gson = Gson()
            val json: String = gson.toJson(mainViewModel.currentUser)
            putString(SharedPreferencesUtils.sharedPrefCurrentUser, json)
            commit() // synchronously
        }
    }

    fun updateAuthUIElements() {
        updateIconAppBar()
        updateNavHeader()
        updateMenuItemsVisibility()
    }

    override fun onDestroy() {
        super.onDestroy()

    }

}