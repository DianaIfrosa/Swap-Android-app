package com.diana.bachelorthesis.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.diana.bachelorthesis.utils.OneParamCallback
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.FragmentLoginBinding
import com.diana.bachelorthesis.utils.BasicFragment
import com.diana.bachelorthesis.utils.NoParamCallback
import com.diana.bachelorthesis.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import java.lang.Exception

class LoginFragment : Fragment(), BasicFragment {
    private val TAG: String = LoginFragment::class.java.name
    lateinit var userViewModel: UserViewModel
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "LoginFragment is onCreateView")
        _binding = FragmentLoginBinding.inflate(layoutInflater)
        val root: View = binding.root
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        initListeners()

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "LoginFragment is onActivityCreated")
        setAppbar()
    }

    override fun initListeners() {
        binding.btnLogin.setOnClickListener { view: View ->
            val button = binding.btnLogin
            val email = binding.editextEmail.text.toString()
            val pass = binding.editextPass.text.toString()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(
                    requireActivity(),
                    R.string.required,
                    Toast.LENGTH_LONG
                ).show()
                //TODO display helper text here as well
            } else {
                button.startAnimation()
                userViewModel.logInUser(email, pass,
                    object : OneParamCallback<FirebaseUser> {
                        override fun onComplete(value: FirebaseUser?) {
                            userViewModel.setCurrentUserData(email, object: NoParamCallback{
                                override fun onComplete() {
                                    button.doneLoadingAnimation(
                                        R.color.green_light,
                                        ContextCompat.getDrawable(
                                            requireActivity(),
                                            R.drawable.ic_done
                                        )!!.toBitmap()
                                    )
                                    (requireActivity() as MainActivity).updateIconAppBar()
                                    (requireActivity() as MainActivity).updateNavHeader()
                                    view.findNavController().navigate(R.id.nav_home)
                                }

                                override fun onError(e: Exception?) {
                                    button.revertAnimation()
                                    Toast.makeText(
                                        requireActivity(),
                                        R.string.something_failed,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            })
                        }

                        override fun onError(e: Exception?) {
                            button.revertAnimation()

                            if (e is FirebaseAuthInvalidCredentialsException || e is FirebaseAuthInvalidUserException) {
                                Toast.makeText(
                                    requireActivity(),
                                    R.string.pass_or_email_invalid,
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    requireActivity(),
                                    R.string.something_failed,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    })
            }
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

}