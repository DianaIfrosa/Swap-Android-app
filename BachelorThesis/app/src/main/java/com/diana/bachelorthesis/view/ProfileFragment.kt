package com.diana.bachelorthesis.view

import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.FragmentProfileBinding
import com.diana.bachelorthesis.model.ItemCategory
import com.diana.bachelorthesis.utils.*
import com.diana.bachelorthesis.viewmodel.ProfileViewModel
import com.diana.bachelorthesis.viewmodel.UserViewModel
import com.squareup.picasso.Picasso
import java.lang.Exception

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
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "ProfileFragment is onActivityCreated")
        setMainPageAppbar(
            requireActivity(),
            requireView().findNavController().currentDestination!!.label.toString()
        )
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

            profileViewModel.preferredOwners =
                user.notifications.preferredOwners as MutableList<String>
            profileViewModel.preferredCities =
                user.notifications.preferredCities as MutableList<String>
            profileViewModel.preferredWords =
                user.notifications.preferredWords as MutableList<String>
            profileViewModel.preferredCategories =
                user.notifications.preferredCategories as MutableList<ItemCategory>
            profileViewModel.preferredExchangePreferences =
                user.notifications.preferredExchangePreferences as MutableList<ItemCategory>

            editPreferencesFragment.show(childFragmentManager, "EditPreferencesDialogFragment")
        }

        binding.editNotifications.setOnClickListener {
            val optionSelected: Int =
                (requireActivity() as MainActivity).getCurrentUser()!!.notifications.notificationsOption
            val editNotificationsFragment = EditNotificationsDialogFragment()
            editNotificationsFragment.isCancelable = true

            profileViewModel.notificationsOptionSelected = optionSelected
            editNotificationsFragment.show(childFragmentManager, "EditNotificationsDialogFragment")
        }

        binding.editProfile.setOnClickListener {
            val editProfileFragment = EditProfileDialogFragment()
            editProfileFragment.isCancelable = true

            editProfileFragment.show(childFragmentManager, "EditProfileDialogFragment")
        }
    }

    private fun updateUI() {
        val currentUser = (requireActivity() as MainActivity).getCurrentUser()!!
        binding.profileName.text = currentUser.name
        binding.profileEmail.text = currentUser.email
        if (currentUser.profilePhoto != null) {
            profileViewModel.profilePhoto = currentUser.profilePhoto
            Picasso.get().load(currentUser.profilePhoto).into(binding.profilePhoto)
        }
    }

    override fun saveNotificationOption(option: Int) {
        (requireActivity() as MainActivity).changeNotificationsOption(option)
        userViewModel.changeUserNotificationsOption(userViewModel.getCurrentUserEmail(), option)
    }

    override fun saveProfileChanges(photoUri: Uri?, newPass: String) {
        if (photoUri != null) {
            profileViewModel.saveNewProfilePhoto((requireActivity() as MainActivity).getCurrentUser()!!.email,
                photoUri, object : OneParamCallback<String> {
                    override fun onComplete(value: String?) {
                        profileViewModel.profilePhoto = value!!
                        Picasso.get().load(value).into(binding.profilePhoto)
                        (requireActivity() as MainActivity).changeCurrentUserProfilePhoto(value)
                    }

                    override fun onError(e: Exception?) {
                        Toast.makeText(requireActivity(), "Photo couldn't be uploaded. Please try again!", Toast.LENGTH_LONG).show()
                    }
                })
        }

        userViewModel.updatePassword(newPass, object: NoParamCallback {
            override fun onComplete() {}

            override fun onError(e: Exception?) {
               Toast.makeText(requireActivity(), "Password could not be updated. Please try again!", Toast.LENGTH_LONG).show()
            }
        })
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
            exchangePreferences
        )
        userViewModel.changeUserPreferences(
            userViewModel.getCurrentUserEmail(),
            words,
            owners,
            cities,
            categories,
            exchangePreferences
        )
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