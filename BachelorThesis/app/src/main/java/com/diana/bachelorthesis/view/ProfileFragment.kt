package com.diana.bachelorthesis.view

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.FragmentProfileBinding
import com.diana.bachelorthesis.databinding.FragmentRecommendationsBinding
import com.diana.bachelorthesis.model.User
import com.diana.bachelorthesis.utils.BasicFragment
import com.diana.bachelorthesis.viewmodel.RecommendationsViewModel
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
        setAppbar()
        updateUI()
    }

    override fun initListeners() {
        binding.btnSignOut.setOnClickListener {
            userViewModel.signOut()
            (requireActivity() as MainActivity).updateIconAppBar()
            (requireActivity() as MainActivity).updateNavHeader()
            requireView().findNavController().navigate(R.id.nav_home)
        }
    }


    fun updateUI() {
        val currentUser = userViewModel.getCurrentUser()
        binding.profileName.text = currentUser.name
        binding.profileEmail.text = currentUser.email
        if (currentUser.profilePhoto != null) {
            Picasso.get().load(currentUser.profilePhoto).into(binding.profilePhoto)
        }
    }

    override fun setAppbar() {
        requireActivity().findViewById<TextView>(R.id.titleAppBar)?.apply {
            visibility = View.VISIBLE
            text = requireView().findNavController().currentDestination!!.label
        }
        requireActivity().findViewById<ImageView>(R.id.logoApp)?.apply {
            visibility = View.GONE
        }
        requireActivity().findViewById<ImageButton>(R.id.iconAppBar)?.apply {
            visibility = View.INVISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "ProfileFragment is onDestroyView")
        _binding = null
    }
}