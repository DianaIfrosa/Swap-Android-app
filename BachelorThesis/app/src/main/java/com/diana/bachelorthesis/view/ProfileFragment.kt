package com.diana.bachelorthesis.view

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.FragmentProfileBinding
import com.diana.bachelorthesis.utils.BasicFragment
import com.diana.bachelorthesis.viewmodel.UserViewModel
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment(), BasicFragment {

    private val TAG: String = ProfileFragment::class.java.name
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "ProfileFragment is onCreateView")
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        initListeners()
        return root
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
    }

    fun updateUI() {
        val currentUser = (requireActivity() as MainActivity).getCurrentUser()!!
        binding.profileName.text = currentUser.name
        binding.profileEmail.text = currentUser.email
        if (currentUser.profilePhoto != null) {
            Picasso.get().load(currentUser.profilePhoto).into(binding.profilePhoto)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "ProfileFragment is onDestroyView")
        _binding = null
    }
}