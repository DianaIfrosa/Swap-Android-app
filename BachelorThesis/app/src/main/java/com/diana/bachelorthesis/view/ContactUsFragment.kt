package com.diana.bachelorthesis.view

import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModelProvider
import com.apachat.loadingbutton.core.customViews.CircularProgressButton
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.FragmentContactUsBinding
import com.diana.bachelorthesis.databinding.FragmentReportPostBinding
import com.diana.bachelorthesis.model.*
import com.diana.bachelorthesis.utils.BasicFragment
import com.diana.bachelorthesis.utils.MailBodyConst
import com.diana.bachelorthesis.utils.NoParamCallback
import com.diana.bachelorthesis.viewmodel.ContactUsViewModel
import com.diana.bachelorthesis.viewmodel.ReportPostViewModel
import com.google.firebase.Timestamp
import java.lang.Exception
import java.util.*

class ContactUsFragment : Fragment(), BasicFragment {
    private val TAG: String = ContactUsFragment::class.java.name
    private var _binding: FragmentContactUsBinding? = null
    private lateinit var contactUsViewModel: ContactUsViewModel
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "ContactUsFragment is onCreateView")
        _binding = FragmentContactUsBinding.inflate(inflater, container, false)
        val root = binding.root
        binding.feedbackSentText.visibility = View.GONE
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "ContactUsFragment is onViewCreated")
        setSubPageAppbar(requireActivity(), getString(R.string.menu_contact_us))
        contactUsViewModel = ViewModelProvider(this)[ContactUsViewModel::class.java]
        initListeners()
    }

    override fun initListeners() {
        binding.sendFeedback.setOnClickListener {
            it as CircularProgressButton

            val text = binding.editTextFeedback.text.toString()
            if (text.isEmpty()) {
                Toast.makeText(requireActivity(), getString(R.string.explanation_required), Toast.LENGTH_SHORT).show()
            } else {
                val id =  UUID.randomUUID().toString()
                it.startAnimation()
                val feedback = Feedback(
                   id,
                    (requireActivity() as MainActivity).getCurrentUser()!!.email,
                    Timestamp.now(),
                    text
                )

                val language = Locale.getDefault().language
                val mail = Mail(
                    to = MailBodyConst.supportEmail,
                    message = mapOf(
                        "subject" to getString(R.string.feedback_email_subject) + id,
                        "html" to
                                if (language == "en")
                                    MailBodyConst.getFeedbackBodyEng(
                                        text,
                                        (requireActivity() as MainActivity).getCurrentUser()!!.email
                                    )
                                else
                                    MailBodyConst.getFeedbackBodyRo(
                                        text,
                                        (requireActivity() as MainActivity).getCurrentUser()!!.email
                                    )
                    )
                )

                contactUsViewModel.addFeedback(feedback, mail, object : NoParamCallback {
                    override fun onComplete() {
                        it.doneLoadingAnimation(
                            R.color.purple_medium,
                            ContextCompat.getDrawable(
                                requireActivity(),
                                R.drawable.ic_done
                            )!!.toBitmap()
                        )
                        binding.feedbackSentText.visibility = View.VISIBLE
//                        binding.editTextReport.setText("")
                    }

                    override fun onError(e: Exception?) {
                        it.revertAnimation()
                        Toast.makeText(
                            requireActivity(),
                            getString(R.string.something_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "ContactUsFragment is onDestroyView")
        _binding = null
    }
}