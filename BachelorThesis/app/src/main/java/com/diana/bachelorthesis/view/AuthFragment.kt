package com.diana.bachelorthesis.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.FragmentAuthBinding
import com.diana.bachelorthesis.model.Mail
import com.diana.bachelorthesis.model.User
import com.diana.bachelorthesis.utils.BasicFragment
import com.diana.bachelorthesis.utils.NoParamCallback
import com.diana.bachelorthesis.utils.OneParamCallback
import com.diana.bachelorthesis.viewmodel.UserViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import java.lang.Exception

class AuthFragment : Fragment(), BasicFragment {

    private val TAG: String = AuthFragment::class.java.name
    lateinit var userViewModel: UserViewModel

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "AuthFragment is onCreateView")
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "AuthFragment is onViewCreated")
        setAuthOrProfileAppbar(
            requireActivity(),
            requireView().findNavController().currentDestination!!.label.toString()
        )
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]

        initListeners()
    }

    private fun setGoogleAuth() {
        val options: GoogleSignInOptions = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        userViewModel.setGoogleClient(GoogleSignIn.getClient(requireActivity(), options))
    }

    override fun initListeners() {
        binding.btnLogin.setOnClickListener { view: View ->
            view.findNavController().navigate(R.id.nav_login)
        }

        binding.btnSignup.setOnClickListener { view: View ->
            view.findNavController().navigate(R.id.nav_register)
        }

        binding.btnGoogle.setOnClickListener {
            setGoogleAuth()
            val intent = userViewModel.getSignInIntentGoogle()
            startActivityForResult(intent, 200)
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            200 -> {
                Log.d(TAG, "onActivityResult from RegisterFragment")
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                    val credential: AuthCredential =
                        GoogleAuthProvider.getCredential(account.idToken, null)
                    performGoogleAuth(credential)
                } catch (e: ApiException) {
                    e.printStackTrace()
                    displayErrorToast()
                }
            }
        }
    }

    private fun performGoogleAuth(credential: AuthCredential) {
        Log.d(TAG, "Performing Google auth")
        userViewModel.signUpWithGoogle(credential, object : NoParamCallback {
            override fun onComplete() {
                Log.d(TAG, "Sign up with Google completed")
                val email = userViewModel.getCurrentUserEmail()
                val name = userViewModel.getCurrentUserName()
                val photoUri = userViewModel.getCurrentUserPhoto()

                userViewModel.verifyUserExists(email, object : OneParamCallback<Boolean> {
                    override fun onComplete(value: Boolean?) {
                        if (value != null) {
                            if (value) {
                                // user already exists in database
                                updateCurrentUser(email)
                            } else {
                                // user signs up now
                                addUser(email, name, photoUri)
                            }
                        }
                    }

                    override fun onError(e: Exception?) {
                        userViewModel.signOut()
                        displayErrorToast()
                    }
                })
            }

            override fun onError(e: Exception?) {
                displayErrorToast()
            }
        })
    }

    private fun addUser(email: String, name: String, photoUri: Uri?) {
        userViewModel.addUser(email, name, photoUri, object : NoParamCallback {
            override fun onComplete() {
                Log.d(TAG, "Added user after Google login completed.")
                updateCurrentUser(email)
                val mail = Mail(
                    to = email,
                    message = mapOf(
                        "subject" to "Swap sign up",
                        "html" to getString(R.string.email_welcome_body)
                    )
                )
                userViewModel.sendWelcomeEmail(mail)
            }

            override fun onError(e: Exception?) {
                userViewModel.signOut()
                displayErrorToast()
            }
        })
    }

    private fun updateCurrentUser(email: String) {
        userViewModel.getUserData(email, object : OneParamCallback<User> {
            override fun onComplete(value: User?) {
                if (value != null) {
                    (requireActivity() as MainActivity).addCurrentUserToSharedPreferences(value)
                    (requireActivity() as MainActivity).updateAuthUIElements()
                    requireView().findNavController()
                        .navigate(
                            R.id.nav_home,
                            null,
                            NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build()
                        )

                } else {
                    userViewModel.signOut()
                    displayErrorToast()
                }
            }

            override fun onError(e: Exception?) {
                userViewModel.signOut()
                displayErrorToast()
            }

        })
    }

    private fun displayErrorToast() {
        Toast.makeText(
            requireActivity(),
            R.string.something_failed,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "AuthFragment is onDestroyView")
        _binding = null

    }

}