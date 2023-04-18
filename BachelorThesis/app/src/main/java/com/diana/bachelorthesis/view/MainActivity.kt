package com.diana.bachelorthesis.view

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.ui.*
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.ActivityMainBinding
import com.diana.bachelorthesis.repository.PhotoRepository
import com.diana.bachelorthesis.viewmodel.UserViewModel
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {
    private val TAG: String = MainActivity::class.java.name

    private lateinit var appBarConfiguration: AppBarConfiguration
    private var _binding: ActivityMainBinding? = null

    private val binding get() = _binding!!

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController
    private lateinit var navView: NavigationView
    private lateinit var headerLayout: View
    lateinit var userViewModel: UserViewModel

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
    }

    override fun onStart() {
        super.onStart()
        initListeners()
        updateNavHeader()
        updateIconAppBar()
    }

     fun updateIconAppBar() {
        if (userViewModel.verifyUserLoggedIn()) {
            binding.appBarMain.iconAppBar.setImageResource(R.drawable.ic_person)
        } else {
            binding.appBarMain.iconAppBar.setImageResource(R.drawable.ic_login)
        }
    }

    fun updateNavHeader() {
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
}