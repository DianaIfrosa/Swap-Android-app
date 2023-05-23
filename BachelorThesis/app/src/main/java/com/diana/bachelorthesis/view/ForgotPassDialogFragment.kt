package com.diana.bachelorthesis.view

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.apachat.loadingbutton.core.customViews.CircularProgressButton
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.FragmentForgotPassDialogBinding
import com.diana.bachelorthesis.utils.NoParamCallback
import com.diana.bachelorthesis.utils.OneParamCallback
import com.diana.bachelorthesis.viewmodel.ForgotPassViewModel
import java.util.regex.Pattern


class ForgotPassDialogFragment : DialogFragment() {
    private val TAG: String = ForgotPassDialogFragment::class.java.name
    private var _binding: FragmentForgotPassDialogBinding? = null
    private lateinit var forgotPassViewModel: ForgotPassViewModel
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "ForgotPassDialogFragment is onCreateView")
        _binding = FragmentForgotPassDialogBinding.inflate(inflater, container, false)
        val root = binding.root
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        forgotPassViewModel = ViewModelProvider(this)[ForgotPassViewModel::class.java]
        initListeners()
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "ForgotPassDialogFragment is onViewCreated")
        val window: Window? = dialog!!.window
        if (window != null) {
            val lp = WindowManager.LayoutParams()
            lp.copyFrom(window.attributes)
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.attributes = lp
        }
    }

    private fun initListeners() {
        binding.btnSendEmail.setOnClickListener{
            it as CircularProgressButton
            val email = binding.editextEmail.text.toString()
            if (emailValid(email)) {
                it.startAnimation()
                forgotPassViewModel.verifyEmailIsInDb(email, object: OneParamCallback<Boolean> {
                    override fun onComplete(value: Boolean?) {
                       if (value != null) {
                           if (value) {
                               forgotPassViewModel.sendResetPassEmail(email, object: NoParamCallback {
                                   override fun onComplete() {
                                       it.doneLoadingAnimation(
                                           R.color.green_light,
                                           ContextCompat.getDrawable(
                                               requireActivity(),
                                               R.drawable.ic_done
                                           )!!.toBitmap()
                                       )
                                       dialog!!.dismiss()
                                   }

                                   override fun onError(e: Exception?) {
                                       it.revertAnimation()
                                       displayToastError()
                                   }

                               })
                           } else {
                               it.revertAnimation()
                               binding.emailInputLayout.apply {
                                   isHelperTextEnabled = true
                                   helperText = getString(R.string.email_not_in_db)
                               }
                           }
                        } else {
                           it.revertAnimation()
                            displayToastError()
                       }
                    }

                    override fun onError(e: Exception?) {
                        it.revertAnimation()
                        displayToastError()
                    }

                })
            }
        }
    }

    private fun displayToastError() {
        Toast.makeText(requireActivity(), getString(R.string.something_failed), Toast.LENGTH_SHORT).show()
    }

    private fun emailValid(email: String): Boolean {
        return if (email.isEmpty()) {
            binding.emailInputLayout.apply {
                isHelperTextEnabled = true
                helperText = getString(R.string.email_required)
            }
            false
        } else if (!validEmailStructure(email)) {
            binding.emailInputLayout.apply {
                isHelperTextEnabled = true
                helperText = getString(R.string.email_invalid)
            }
            false
        } else {
            binding.emailInputLayout.apply {
                isHelperTextEnabled = false
                helperText = ""
            }
            true
        }
    }

    private fun validEmailStructure(email: String): Boolean {
        val regex = "^[A-Za-z0-9+_.-]+@(.+)$"
        val pattern = Pattern.compile(regex)
        return pattern.matcher(email).matches()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "ForgotPassDialogFragment is onDestroyView")
        _binding = null
    }
}