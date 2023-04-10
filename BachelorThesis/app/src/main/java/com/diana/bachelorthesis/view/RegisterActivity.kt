package com.diana.bachelorthesis.view

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.ActivityRegisterBinding
import java.util.regex.Pattern


class RegisterActivity : AppCompatActivity() {
    private val TAG: String = RegisterActivity::class.java.name
    private var _binding: ActivityRegisterBinding? = null
    private val binding get() = _binding!!
    private var emailOk: Boolean = false
    private var nameOk: Boolean = false
    private var passOk: Boolean = false
    private var passConfirmedOk: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "RegisterActivity is onCreate")
        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        setUIDetails()
        initListeners()
    }

    private fun initListeners() {
        binding.editextEmail.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                showStatusEmail(binding.editextEmail.text.toString())
                Log.d(TAG, "Verify email")
            }
        }

        binding.editextName.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                //TODO verify if email is ok
                Log.d(TAG, "Verify name")
            }
        }
        binding.editextPass.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                //TODO verify if email is ok
                Log.d(TAG, "Verify pass")
            }
        }

        binding.editextPassConfirm.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                //TODO verify if email is ok
                Log.d(TAG, "Verify pass confirmed")
            }
        }

        binding.btnRegister.setOnClickListener {
            val email = binding.editextEmail.text.toString()
            val name = binding.editextName.text.toString()
            val password = binding.editextPass.text.toString()
            val passwordConfirmed = binding.editextPassConfirm.text.toString()

            // val dataOk = verifyData(email, name, password, passwordConfirmed)
            // emailOk && nameOk && passOk && passConfirmedOk

        }
    }

    private fun setUIDetails() {
        binding.btnRegisterFacebook.setBackgroundColor(
            ContextCompat.getColor(
                applicationContext,
                R.color.facebook_blue
            )
        )
        binding.btnRegisterGoogle.setBackgroundColor(
            ContextCompat.getColor(
                applicationContext,
                R.color.google_red
            )
        )
    }

    private fun showStatusEmail(email: String) {
        binding.iconEmailStatus.visibility = View.VISIBLE
        if (validEmail(email)) {
            val icon: Drawable =
                DrawableCompat.wrap(
                    AppCompatResources.getDrawable(this, com.diana.bachelorthesis.R.drawable.ic_check)!!)
            binding.iconEmailStatus.setBackgroundDrawable(icon)
            binding.emailInputLayout.isHelperTextEnabled = false
            binding.emailInputLayout.helperText = ""
        } else {
            val icon: Drawable =
                DrawableCompat.wrap(
                    AppCompatResources.getDrawable(this, com.diana.bachelorthesis.R.drawable.ic_cancel)!!)
            DrawableCompat.setTint(icon, resources.getColor(com.diana.bachelorthesis.R.color.red_light))
            binding.iconEmailStatus.setBackgroundDrawable(icon)
            binding.emailInputLayout.isHelperTextEnabled = true
            if (email.isEmpty())
                binding.emailInputLayout.helperText = "Email is required" // TODO make string res
            else
                binding.emailInputLayout.helperText = "Email is invalid" // TODO make string res
        }
    }

    private fun validEmail(email: String): Boolean {
        val regex = "^[A-Za-z0-9+_.-]+@(.+)$"
        val pattern = Pattern.compile(regex)
        return pattern.matcher(email).matches()
    }
}