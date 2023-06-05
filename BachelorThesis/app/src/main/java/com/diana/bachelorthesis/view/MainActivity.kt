package com.diana.bachelorthesis.view

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.get
import androidx.navigation.ui.*
import com.bumptech.glide.Glide
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.ActivityMainBinding
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.ItemCategory
import com.diana.bachelorthesis.model.User
import com.diana.bachelorthesis.utils.OneParamCallback
import com.diana.bachelorthesis.utils.SharedPreferencesUtils
import com.diana.bachelorthesis.viewmodel.ChatViewModel
import com.diana.bachelorthesis.viewmodel.ItemsViewModel
import com.diana.bachelorthesis.viewmodel.MainViewModel
import com.diana.bachelorthesis.viewmodel.UserViewModel
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    private val TAG: String = MainActivity::class.java.name

    private lateinit var appBarConfiguration: AppBarConfiguration
    private var _binding: ActivityMainBinding? = null

    private val binding get() = _binding!!

    private var pushNotificationsReceiver: PushNotificationsReceiver? = null

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController
    lateinit var navView: NavigationView
    private lateinit var headerLayout: View

    private lateinit var userViewModel: UserViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var itemsViewModel: ItemsViewModel
    private lateinit var chatViewModel: ChatViewModel

    private lateinit var sharedPref: SharedPreferences
    var returnedHomeFromItemPage: Boolean = false // TODO delete if no longer used
    var itemIdFromNotification: String? = null
    var itemFromNotificationForExchange: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity is onCreate")
        installSplashScreen()

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//         Create channel to show notifications
//        val channelId = getString(R.string.default_notification_channel_id)
//        val channelName = getString(R.string.default_notification_channel_name)
//        val notificationManager = getSystemService(
//            NotificationManager::class.java
//        )
//        notificationManager.createNotificationChannel(
//            NotificationChannel(
//                channelId,
//                channelName, NotificationManager.IMPORTANCE_HIGH
//            )
//        )

        setSupportActionBar(binding.appBarMain.toolbar)
        getViewModels()
//        userViewModel.signOut() // TODO uncomment when problems from shared preferences occur

        drawerLayout = binding.drawerLayout
        navView = binding.navView
        headerLayout = navView.getHeaderView(0)
        val navHostFragment: NavHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val inflater = navHostFragment.navController.navInflater
//        val graph = inflater.inflate(R.navigation.nav_graph)

        navController = navHostFragment.navController

        navController.setGraph(R.navigation.nav_graph) // set the graph programmatically to send argument to start destination (not applicable anymore!!)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_add_item, R.id.nav_recommendations,
                R.id.nav_chat, R.id.nav_favorites, R.id.nav_map,
                R.id.nav_history, R.id.nav_contact_us
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // for the notifications
        if (intent.extras != null) {
            for (key in intent.extras!!.keySet()) {
                val value = intent.extras!![key]
                Log.d(TAG, "Key: $key Value: $value")
            }
            if (intent.extras!!.keySet().contains("title")) { // activity is started from a notification

                if (intent.extras!!.keySet().contains("itemId")) {
                    Log.d(TAG, "Activity started for recommendations notifications")
                    itemIdFromNotification = intent.extras!!.get("itemId") as String?
                    itemFromNotificationForExchange = (intent.extras!!.get("forExchange") as String).toBoolean()
                    mainViewModel.clickedOnRecommendations = true
                    val handled = NavigationUI.onNavDestinationSelected(
                        navView.menu.findItem(R.id.nav_recommendations),
                        navController
                    )
                    if (handled) drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    Log.d(TAG, "Activity started for chat notifications")
                    val handled = NavigationUI.onNavDestinationSelected(
                        navView.menu.findItem(R.id.nav_chat),
                        navController
                    )
                    if (handled) drawerLayout.closeDrawer(GravityCompat.START)
                }
            }
        } else {
            Log.d(TAG, "intent.extras is null")
        }

        navView.setNavigationItemSelectedListener { item: MenuItem ->
            // Block access to certain fragments unless the user is authenticated
            if (!userViewModel.verifyUserLoggedIn() && (item.itemId == R.id.nav_add_item || item.itemId == R.id.nav_map)) {
                navController.navigate(R.id.nav_intro_auth)
                drawerLayout.closeDrawer(GravityCompat.START)
                true

            } else {
                if (item.itemId == R.id.nav_recommendations) {
                    mainViewModel.clickedOnRecommendations = true
                }

                if (item.itemId != R.id.nav_home) {
                    itemsViewModel.restoreDefaultCurrentItemsAndOptions()
                }

                val handled = NavigationUI.onNavDestinationSelected(item, navController)
                if (handled) drawerLayout.closeDrawer(GravityCompat.START)
                handled
            }
        }
        sharedPref = getPreferences(Context.MODE_PRIVATE)

        if (userViewModel.verifyUserLoggedIn()) {
            restoreCurrentUserData()
            chatViewModel.currentUser = getCurrentUser()!!
            itemsViewModel.currentUser = getCurrentUser()!!
            chatViewModel.listenToUserChatChanges(object: OneParamCallback<User> {
                override fun onComplete(value: User?) {
                    updateCurrentUserChatList(value!!.chatIds)
                }

                override fun onError(e: java.lang.Exception?) {}

            })
            chatViewModel.getUserChats()
        }
        updateAuthUIElements()
        setDefaultSharedPreferencesHomeOptions()

        //  auto-generated to handle app links.
        val appLinkIntent: Intent = intent
        val appLinkAction: String? = appLinkIntent.action
        val appLinkData: Uri? = appLinkIntent.data

    }

    private inner class PushNotificationsReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "onReceive from PushNotificationsReceiver")
            Log.d(TAG, intent.action ?: "No intent action")

            if (intent.action == "push_notification") {
                val title = intent.getStringExtra("title")!!
                val body = intent.getStringExtra("body")!!
                val itemId = intent.getStringExtra("itemId")
                val itemForExchange = intent.getBooleanExtra("itemForExchange", false)


                if (itemId == null) {
                    Log.d(TAG, "Received notification for chat!")
                    // chat notification
//                    val pendingIntent = NavDeepLinkBuilder(context)
//                        .setGraph(R.navigation.nav_graph)
//                        .setDestination(R.id.nav_chat)
//                        .createPendingIntent()

                    val channel = NotificationChannel(
                        getString(R.string.chat_notification_channel_id),
                        getString(R.string.chat_notification_channel_name),
                        NotificationManager.IMPORTANCE_HIGH
                    )
                    val myIntent = Intent(this@MainActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("destination", R.id.nav_chat)
                    }
                    val pendingIntent = PendingIntent.getActivity(this@MainActivity, Random.nextInt(), myIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                    getSystemService(NotificationManager::class.java).createNotificationChannel(
                        channel
                    )
                    val notification: Notification.Builder = Notification.Builder(
                        context,
                        getString(R.string.chat_notification_channel_id)
                    )
                        .setContentTitle(title)
                        .setContentText(getString(R.string.sent_message))
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)

                    val notificationObj = notification.build()
//                    notificationObj.flags = Notification.FLAG_ONGOING_EVENT

                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.d(TAG, "Notifications permission NOT granted!")
                        return
                    } else {
                        Log.d(TAG, "Notifications permission granted!")
                    }

                    if (navController.currentDestination?.id != navController.graph[R.id.nav_chat].id &&
                        navController.currentDestination?.id != navController.graph[R.id.nav_chat_page_fragment].id
                    ) {
                        Log.d(TAG, "Notification fired")
                        NotificationManagerCompat.from(context).notify(0, notificationObj)
                    }

                } else {
                    Log.d(TAG, "Received notification for recommendations!")

                    val channel = NotificationChannel(
                        getString(R.string.posts_notification_channel_id),
                        getString(R.string.posts_notification_channel_name),
                        NotificationManager.IMPORTANCE_HIGH
                    )
                    // recommendations notifications
//                    val pendingIntent = NavDeepLinkBuilder(context)
//                        .setGraph(R.navigation.nav_graph)
//                        .setDestination(R.id.nav_recommendations)
//                        .createPendingIntent()

                    val myIntent = Intent(this@MainActivity, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                                putExtra("destination", R.id.nav_recommendations)
                    }
                    val pendingIntent = PendingIntent.getActivity(this@MainActivity, Random.nextInt(), myIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                    getSystemService(NotificationManager::class.java).createNotificationChannel(
                        channel
                    )
                    val notification: Notification.Builder = Notification.Builder(
                        context,
                        getString(R.string.posts_notification_channel_id)
                    )
                        .setContentTitle(title)
                        .setContentText(body)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)

                    val notificationObj = notification.build()
//                     notificationObj.flags = Notification.FLAG_ONGOING_EVENT

                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.d(TAG, "Notifications permission NOT granted!")
                        return
                    } else {
                        Log.d(TAG, "Notifications permission granted!")
                    }

                    if (navController.currentDestination?.id != navController.graph[R.id.nav_recommendations].id) {
                        Log.d(TAG, "Notification fired")
                        mainViewModel.clickedOnRecommendations = true
                        itemIdFromNotification = itemId
                        itemFromNotificationForExchange = itemForExchange
                        NotificationManagerCompat.from(context).notify(1, notificationObj)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "MainActivity is onStart")
        initListeners()
    }

    private fun processIntent() {
        intent.extras?.getInt("destination")?.let {
//            intent.removeExtra("destination")
            val handled = NavigationUI.onNavDestinationSelected(
                navView.menu.findItem(it),
                navController
            )
            if (handled) drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent")
        // called when application was open
        setIntent(intent)
        intent?.let { processIntent() }
    }
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "MainActivity is onResume")
        if (pushNotificationsReceiver == null) {
            pushNotificationsReceiver = PushNotificationsReceiver()
            val intentFilter = IntentFilter("push_notification")
            registerReceiver(pushNotificationsReceiver, intentFilter)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "MainActivity is onPause")
        if (pushNotificationsReceiver != null) {
            unregisterReceiver(pushNotificationsReceiver)
            pushNotificationsReceiver = null
        }
    }

    private fun restoreCurrentUserData() {
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
        itemsViewModel = ViewModelProvider(this)[ItemsViewModel::class.java]
        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
    }

    private fun updateMenuItemsVisibility() {
        val userLogged = userViewModel.verifyUserLoggedIn()
        navView.menu.findItem(R.id.nav_recommendations).isVisible = userLogged
        navView.menu.findItem(R.id.nav_favorites).isVisible = userLogged
        navView.menu.findItem(R.id.nav_chat).isVisible = userLogged
        navView.menu.findItem(R.id.nav_history).isVisible = userLogged
        navView.menu.findItem(R.id.nav_contact_us).isVisible = userLogged
    }

    private fun updateIconAppBar() {
        Log.d(TAG, "Updating icon from app bar")
        if (userViewModel.verifyUserLoggedIn()) {
            binding.appBarMain.iconAppBar.setImageResource(R.drawable.ic_person)
        } else {
            binding.appBarMain.iconAppBar.setImageResource(R.drawable.ic_login)
        }
    }

    private fun updateNavHeader() {
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
            Glide.with(this).load(currentUser.profilePhoto).centerCrop().into(photoImageView)
        } else {
            btnAuth.visibility = View.VISIBLE
            nameTextView.visibility = View.INVISIBLE
            emailTextView.visibility = View.INVISIBLE
            photoImageView.visibility = View.INVISIBLE
        }
    }

    private fun initListeners() {
        binding.appBarMain.iconAppBar.setOnClickListener {
            if (userViewModel.verifyUserLoggedIn()) {
                navController.navigate(R.id.nav_profile)
            } else {
                navController.navigate(R.id.nav_intro_auth)
            }
        }
        headerLayout.findViewById<Button>(R.id.btnAuthNavHeader)?.setOnClickListener {
            if (!userViewModel.verifyUserLoggedIn()) {
                drawerLayout.closeDrawer(GravityCompat.START)
                navController.navigate(R.id.nav_intro_auth)
            }
        }

        headerLayout.findViewById<ImageView>(R.id.photoNavHeader)?.setOnClickListener {
            goToProfileFromMenu()
        }

        headerLayout.findViewById<TextView>(R.id.nameNavHeader)?.setOnClickListener {
            goToProfileFromMenu()
        }

        headerLayout.findViewById<TextView>(R.id.emailNavHeader)?.setOnClickListener {
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
            putString(SharedPreferencesUtils.sharedprefCityFilter, "")
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

    fun getTokenFromSharedPreferences(): String? {
        val sharedPref = this.getSharedPreferences("token", Context.MODE_PRIVATE)
        return sharedPref.getString("token", null)
    }

    fun addCurrentUserToSharedPreferences(user: User) {
        // just logged in
        mainViewModel.currentUser = user
        with(sharedPref.edit()) {
            val gson = Gson()
            val json: String = gson.toJson(user)
            putString(SharedPreferencesUtils.sharedPrefCurrentUser, json)
            commit() // synchronously
        }

        chatViewModel.currentUser = user
        chatViewModel.listenToUserChatChanges(object: OneParamCallback<User> {
            override fun onComplete(value: User?) {
                updateCurrentUserChatList(value!!.chatIds)
            }

            override fun onError(e: java.lang.Exception?) {}

        })
        chatViewModel.getUserChats()


    }

    fun addFavoriteItem(item: Item) {
        mainViewModel.addFavoriteItem(item)
        updateCurrentUserSharedPreferences()
    }

    fun changeNotificationsOption(option: Int) {
        mainViewModel.updateNotificationOption(option)
        updateCurrentUserSharedPreferences()
    }

    fun changeCurrentUserProfilePhoto(photo: String) {
        mainViewModel.updateProfilePhoto(photo)
        updateCurrentUserSharedPreferences()
        try {
            updateNavHeader()
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Some unexpected error occurred during update Auth UI elements, see error below"
            )
            e.message?.let { Log.e(TAG, it) }
        }
    }

    fun changeUserPreferences(
        words: List<String>,
        owners: List<String>,
        cities: List<String>,
        categories: List<ItemCategory>,
        exchangePreferences: List<ItemCategory>
    ) {
        mainViewModel.updateUserPreferences(words, owners, cities, categories, exchangePreferences)
        updateCurrentUserSharedPreferences()
        mainViewModel.modifiedRecommendations = true
    }

    fun itemIsFavorite(item: Item): Boolean = mainViewModel.itemIsFavorite(item)


    fun removeFavoriteItem(item: Item) {
        mainViewModel.removeFavoriteItem(item)
        updateCurrentUserSharedPreferences()
    }

    fun updateCurrentUserChatList(newChatList: ArrayList<Map<String, String>>) {
        mainViewModel.updateChatList(newChatList)
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
        try {
            updateIconAppBar()
            updateNavHeader()
            updateMenuItemsVisibility()
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Some unexpected error occurred during update Auth UI elements, see error below"
            )
            e.message?.let { Log.e(TAG, it) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "MainActivity is onDestroy")
        chatViewModel.detachListeners()
        _binding = null
    }
}