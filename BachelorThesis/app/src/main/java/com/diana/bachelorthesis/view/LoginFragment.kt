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
import androidx.navigation.NavOptions
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
        setAuthAppbar(requireActivity(), requireView().findNavController().currentDestination!!.label.toString())
    }

    override fun initListeners() {
        binding.btnLogin.setOnClickListener { view: View ->
            val button = binding.btnLogin
            val email = binding.editextEmail.text.toString()
            val pass = binding.editextPass.text.toString()


            getFieldStatus()
            if (email.isNotEmpty() && pass.isNotEmpty()) {
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
                                    (requireActivity() as MainActivity).updateAuthUIElements()
                                    requireView().findNavController()
                                        .navigate(
                                            R.id.nav_home,
                                            null,
                                            NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build()
                                        )
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

    private fun getFieldStatus() {
        if (binding.editextEmail.text.toString().isEmpty()) {
            binding.emailInputLayout.apply {
                isHelperTextEnabled = true
                helperText = getString(R.string.email_required)
            }
        } else {
            binding.emailInputLayout.apply {
                isHelperTextEnabled = false
                helperText = ""
            }
        }

        if (binding.editextPass.text.toString().isEmpty()) {
            binding.passInputLayout.apply {
                isHelperTextEnabled = true
                helperText = getString(R.string.pass_required)
            }
        } else {
            binding.passInputLayout.apply {
                isHelperTextEnabled = false
                helperText = ""
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "LoginFragment is onDestroyView")
        _binding = null
    }

}