package com.diana.bachelorthesis.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.viewpager.widget.PagerAdapter
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.FragmentIntroAuthBinding
import com.diana.bachelorthesis.utils.BasicFragment
import com.diana.bachelorthesis.utils.NoParamCallback
import com.diana.bachelorthesis.viewmodel.UserViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import java.lang.Exception

class IntroAuthFragment : Fragment(), BasicFragment {
    private val TAG: String = IntroAuthFragment::class.java.name
    lateinit var userViewModel: UserViewModel

    private var _binding: FragmentIntroAuthBinding? = null
    private val binding get() = _binding!!

    internal lateinit var layouts: ArrayList<Int>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "IntroAuthFragment is onCreateView")
        _binding = FragmentIntroAuthBinding.inflate(inflater, container, false)
        val root: View = binding.root

        layouts = arrayListOf(R.layout.intro_slide_1, R.layout.intro_slide_2, R.layout.intro_slide_3)

        val introScreensAdapter = IntroScreensAdapter()
        binding.viewPager.adapter = introScreensAdapter
        binding.springDotsIndicator.attachTo(binding.viewPager)
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        initListeners()

        return root
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "IntroAuthFragment is onActivityCreated")
        setAppbar()
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

    private fun setGoogleAuth() {
        val options: GoogleSignInOptions = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        userViewModel.setGoogleClient(GoogleSignIn.getClient(requireActivity(), options))
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            200 -> {
                Log.d(TAG, "onActivityResult from RegisterFragment")
                val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                    val credential: AuthCredential = GoogleAuthProvider.getCredential(account.idToken, null)
                    performGoogleAuth(credential)
                } catch(e: ApiException) {
                    e.printStackTrace()
                    displayErrorToast()
                }
            }
        }
    }

    private fun performGoogleAuth(credential: AuthCredential) {
        Log.d(TAG, "Performing Google auth")
        userViewModel.signUpWithGoogle(credential, object: NoParamCallback {
            override fun onComplete() {
                Log.d(TAG, "Sign up with Google completed")
                val email = userViewModel.getCurrentUserEmail()
                val name = userViewModel.getCurrentUserName()
                val photoUri = userViewModel.getCurrentUserPhoto()

                userViewModel.addUser(email, name, photoUri, object: NoParamCallback {
                    override fun onComplete() {
                        Log.d(TAG, "Add user completed")
                        // After adding the user into the DB, update the currentUser object
                        userViewModel.setUserData(email, object: NoParamCallback {
                            override fun onComplete() {
                                (requireActivity() as MainActivity).updateAuthUIElements()
                                requireView().findNavController()
                                    .navigate(
                                        R.id.nav_home,
                                        null,
                                        NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build()
                                    )
                            }

                            override fun onError(e: Exception?) {
                                displayErrorToast()
                            }
                        })
                    }

                    override fun onError(e: Exception?) {
                        displayErrorToast()
                    }
                })
            }

            override fun onError(e: Exception?) {
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

    override fun setAppbar() {
        requireActivity().findViewById<TextView>(R.id.titleAppBar)?.apply {
            visibility = View.GONE
        }
        requireActivity().findViewById<ImageView>(R.id.logoApp)?.apply {
            visibility = View.VISIBLE
        }
        requireActivity().findViewById<ImageButton>(R.id.iconAppBar)?.apply {
            visibility = View.INVISIBLE
        }
    }


    // make adapter as inner class because I need activity context
    inner class IntroScreensAdapter: PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val layoutInflater = requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater.inflate(layouts[position], container, false)
            container.addView(view)
            return view
        }

        override fun getCount(): Int {
            return layouts.size
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view == obj
        }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            val view = obj as View
            container.removeView(view)
        }
    }
}