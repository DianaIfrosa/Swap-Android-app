package com.diana.bachelorthesis.view

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.ContactsContract.Profile
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.FragmentProfileBinding
import com.diana.bachelorthesis.model.ItemCategory
import com.diana.bachelorthesis.utils.BasicFragment
import com.diana.bachelorthesis.utils.ListParamCallback
import com.diana.bachelorthesis.utils.ProfileOptionsListener
import com.diana.bachelorthesis.utils.SharedPreferencesUtils
import com.diana.bachelorthesis.viewmodel.ItemsViewModel
import com.diana.bachelorthesis.viewmodel.ProfileViewModel
import com.diana.bachelorthesis.viewmodel.UserViewModel
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment(), BasicFragment, ProfileOptionsListener {

    private val TAG: String = ProfileFragment::class.java.name
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    lateinit var userViewModel: UserViewModel
    lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "ProfileFragment is onCreateView")
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root
        getViewModels()
        initListeners()
        return root
    }

    private fun getViewModels() {
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "ProfileFragment is onActivityCreated")
        setMainPageAppbar(requireActivity(), requireView().findNavController().currentDestination!!.label.toString())
        updateUI()
    }

    override fun initListeners() {
        binding.btnSignOut.setOnClickListener {
            // Write current user's data that was stored in shared preferences to cloud database
            userViewModel.updateCurrentUser((requireActivity() as MainActivity).getCurrentUser()!!)

            // sign out from firebase auth system
            userViewModel.signOut()

            (requireActivity() as MainActivity).deleteCurrentUserFromSharedPreferences()

            (requireActivity() as MainActivity).updateAuthUIElements()
            requireView().findNavController()
                .navigate(
                    R.id.nav_home,
                    null,
                    NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build()
                )
        }

        binding.editPreferences.setOnClickListener {
            val user = (requireActivity() as MainActivity).getCurrentUser()!!
            val editPreferencesFragment = EditPreferencesDialogFragment()
            editPreferencesFragment.isCancelable = true

            val bundle = Bundle()
            bundle.putStringArrayList("owners", ArrayList(user.notifications.preferredOwners))
            bundle.putStringArrayList("cities", ArrayList(user.notifications.preferredCities))
            bundle.putStringArrayList("words", ArrayList(user.notifications.preferredWords))
            bundle.putStringArrayList("categories", ArrayList(user.notifications.preferredCategories.map {it.name}))
            bundle.putStringArrayList("exchange_preferences", ArrayList(user.notifications.preferredExchangePreferences.map {it.name}))
            editPreferencesFragment.arguments = bundle

            editPreferencesFragment.show(childFragmentManager, "EditPreferencesDialogFragment")
        }

        binding.editNotifications.setOnClickListener {
            val optionSelected: Int = (requireActivity() as MainActivity).getCurrentUser()!!.notifications.notificationsOption
            val editNotificationsFragment = EditNotificationsDialogFragment()
            editNotificationsFragment.isCancelable = true

            val bundle = Bundle()
            bundle.putInt("notificationsOption", optionSelected)
            editNotificationsFragment.arguments = bundle

            editNotificationsFragment.show(childFragmentManager, "EditNotificationsDialogFragment")
        }
    }

    private fun updateUI() {
        val currentUser = (requireActivity() as MainActivity).getCurrentUser()!!
        binding.profileName.text = currentUser.name
        binding.profileEmail.text = currentUser.email
        if (currentUser.profilePhoto != null) {
            Picasso.get().load(currentUser.profilePhoto).into(binding.profilePhoto)
        }
    }

    override fun saveNotificationOption(option: Int) {
      (requireActivity() as MainActivity).changeNotificationsOption(option)
      userViewModel.changeUserNotificationsOption(userViewModel.getCurrentUserEmail(), option)
    }

    override fun saveProfileChanges() {
        TODO("Not yet implemented")
    }

    override fun savePreferencesForRecommendations(
        words: List<String>,
        owners: List<String>,
        cities: List<String>,
        categories: List<ItemCategory>,
        exchangePreferences: List<ItemCategory>
    ) {
        (requireActivity() as MainActivity).changeUserPreferences(
            words,
            owners,
            cities,
            categories,
            exchangePreferences)
        userViewModel.changeUserPreferences(userViewModel.getCurrentUserEmail(), words, owners, cities, categories, exchangePreferences)
    }

    fun getAllCities(callback: ListParamCallback<String>) {
        profileViewModel.getAllCities(callback)
    }

    fun getAllOwners(callback: ListParamCallback<String>) {
        userViewModel.getAllUsersName(callback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "ProfileFragment is onDestroyView")
        _binding = null
    }
}