package com.diana.bachelorthesis.view

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.apachat.loadingbutton.core.customViews.CircularProgressButton
import com.diana.bachelorthesis.utils.OneParamCallback
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.FragmentRegisterBinding
import com.diana.bachelorthesis.model.Mail
import com.diana.bachelorthesis.utils.BasicFragment
import com.diana.bachelorthesis.utils.MailBodyConst
import com.diana.bachelorthesis.utils.NoParamCallback
import com.diana.bachelorthesis.viewmodel.UserViewModel
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import java.lang.Exception
import java.util.*
import java.util.regex.Pattern


class RegisterFragment : Fragment(), BasicFragment {
    private val TAG: String = RegisterFragment::class.java.name
    lateinit var userViewModel: UserViewModel

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private var emailOk: Boolean = false
    private var nameOk: Boolean = false
    private var passOk: Boolean = false
    private var passConfirmedOk: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "RegisterFragment is onCreateView")
        _binding = FragmentRegisterBinding.inflate(layoutInflater)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "RegisterFragment is onViewCreated")
        setAuthOrProfileAppbar(requireActivity(), requireView().findNavController().currentDestination!!.label.toString())
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        initListeners()
    }

    override fun initListeners() {
        binding.editextEmail.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val value = binding.editextEmail.text.toString().trim()
                emailOk = getFieldStatus(
                    value,
                    ::validEmail,
                    binding.iconEmailStatus,
                    binding.emailInputLayout,
                    getString(R.string.email_required),
                    getString(R.string.email_invalid)
                )
            }
        }

        binding.editextName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val value = binding.editextName.text.toString().trim()
                nameOk = getFieldStatus(
                    value,
                    ::validName,
                    binding.iconNameStatus,
                    binding.nameInputLayout,
                    getString(R.string.name_required),
                    getString(R.string.name_invalid)
                )
            }
        }

        binding.editextPass.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                verifyPasswords()
            }
        }

        binding.editextPassConfirm.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                verifyPasswords()
            }
        }

        initButtonsListeners()
    }

    private fun initButtonsListeners() {
        binding.btnRegister.setOnClickListener {
            clearFocusFields()
            val name = binding.editextName.text.toString().trim()
            val email = binding.editextEmail.text.toString().trim()
            val pass = binding.editextPass.text.toString()

            (it as CircularProgressButton).startAnimation()
            if (emailOk && nameOk && passOk && passConfirmedOk) {

                Log.d(TAG, "Register starting for user ${binding.editextEmail.text.toString()}")

                userViewModel.registerUser(
                    email,
                    pass,
                    object : OneParamCallback<FirebaseUser> {
                        override fun onComplete(value: FirebaseUser?) {
                            userViewModel.addUser(email, name, null, object: NoParamCallback {
                                override fun onComplete() {
                                    val language = Locale.getDefault().language
                                    val mail = Mail(
                                        to = email,
                                        message = mapOf(
                                            "subject" to getString(R.string.subject_email),
                                            "html" to if (language == "en") MailBodyConst.bodyWelcomeEng else MailBodyConst.bodyWelcomeRo
                                        )
                                    )
                                    userViewModel.sendWelcomeEmail(mail, object:NoParamCallback {
                                        override fun onComplete() {
                                            Log.d(TAG, "Returned from sending email successfully.")
                                            userViewModel.signOut(email) // Firebase register automatically signs in
                                            it.doneLoadingAnimation(
                                                R.color.purple_medium,
                                                ContextCompat.getDrawable(
                                                    requireActivity(),
                                                    R.drawable.ic_done
                                                )!!.toBitmap()
                                            )
                                        }

                                        override fun onError(e: Exception?) {
                                            Log.w(TAG, "Returned from sending email with error.")
                                            Log.w(TAG, e?.message.toString())
                                            userViewModel.signOut(email) // Firebase register automatically signs in
                                        }
                                    })
                                }

                                override fun onError(e: Exception?) {
                                    it.revertAnimation()
                                    userViewModel.deleteUser()
                                    userViewModel.signOut(email)
                                    Toast.makeText(
                                        requireActivity(),
                                        R.string.something_failed,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            })
                        }

                        override fun onError(e: Exception?) {
                            it.revertAnimation()

                            if (e is FirebaseAuthUserCollisionException) {
                                Toast.makeText(
                                    requireActivity(),
                                    R.string.account_exists_already,
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
                    }
                )

            } else {
                it.revertAnimation()
                displayRequiredStatus()
            }
        }
    }

    private fun displayRequiredStatus() {

        val email = binding.editextEmail.text.toString().trim()
        if (email.isEmpty()) {
            emailOk = getFieldStatus(
                email,
                ::validEmail,
                binding.iconEmailStatus,
                binding.emailInputLayout,
                getString(R.string.email_required),
                getString(R.string.email_invalid)
            )
        }

        val name = binding.editextName.text.toString().trim()
        if (name.isEmpty()) {
            nameOk = getFieldStatus(
                name,
                ::validName,
                binding.iconNameStatus,
                binding.nameInputLayout,
                getString(R.string.name_required),
                getString(R.string.name_invalid)
            )
        }
        verifyPasswords()
    }

    private fun verifyPasswords() {
        val pass = binding.editextPass.text.toString()
        passOk = getFieldStatus(
            pass,
            ::validPass,
            binding.iconPassStatus,
            binding.passInputLayout,
            getString(R.string.pass_required),
            getString(R.string.pass_invalid)
        )

        val passConfirmed = binding.editextPassConfirm.text.toString()
        passConfirmedOk = getFieldStatus(
            passConfirmed,
            ::validPassConfirmed,
            binding.iconPassconfirmStatus,
            binding.confirmPassInputLayout,
            getString(R.string.pass_confirmed_required),
            getString(R.string.pass_confirmed_invalid)
        )
    }

    private fun clearFocusFields() {
        binding.editextEmail.clearFocus()
        binding.editextName.clearFocus()
        binding.editextPass.clearFocus()
        binding.editextPassConfirm.clearFocus()
    }

    private fun getFieldStatus(
        field: String,
        validField: (_: String) -> Boolean,
        statusImageButton: ImageButton,
        fieldInputLayout: TextInputLayout,
        helperTextForRequired: String,
        helperTextForInvalid: String
    ): Boolean {

        statusImageButton.visibility = View.VISIBLE
        if (validField(field)) {
            val icon: Drawable =
                DrawableCompat.wrap(
                    AppCompatResources.getDrawable(requireActivity(), R.drawable.ic_check)!!
                )
            statusImageButton.background = icon
            fieldInputLayout.apply {
                isHelperTextEnabled = false
                helperText = ""
            }
            return true
        } else {
            val icon: Drawable =
                DrawableCompat.wrap(
                    AppCompatResources.getDrawable(requireActivity(), R.drawable.ic_cancel)!!
                )
            DrawableCompat.setTint(icon, ContextCompat.getColor(requireActivity(), R.color.red_light))
            statusImageButton.background = icon
            fieldInputLayout.apply {
                isHelperTextEnabled = true

                helperText = if (field.isEmpty()) helperTextForRequired
                else helperTextForInvalid
            }
            return false
        }
    }

    private fun validEmail(email: String): Boolean {
        val regex = "^[A-Za-z0-9+_.-]+@(.+)$"
        val pattern = Pattern.compile(regex)
        return pattern.matcher(email).matches()
    }

    private fun validName(name: String): Boolean {
        return name.contains(" ")
    }

    private fun validPass(pass: String): Boolean {
        val regex = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$"
        val pattern = Pattern.compile(regex)
        return pattern.matcher(pass).matches()
    }

    private fun validPassConfirmed(passConfirmed: String): Boolean {
        return passConfirmed == binding.editextPass.text.toString() && passConfirmed.isNotEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "RegisterFragment is onDestroyView")
        _binding = null
    }
}