package com.diana.bachelorthesis.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.apachat.loadingbutton.core.customViews.CircularProgressButton
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.FragmentReportPostBinding
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.ItemExchange
import com.diana.bachelorthesis.model.Mail
import com.diana.bachelorthesis.model.PostReport
import com.diana.bachelorthesis.utils.BasicFragment
import com.diana.bachelorthesis.utils.MailBodyConst
import com.diana.bachelorthesis.utils.NoParamCallback
import com.diana.bachelorthesis.viewmodel.ReportPostViewModel
import com.google.firebase.Timestamp
import java.lang.Exception
import java.util.*

class ReportPostFragment : Fragment(), BasicFragment {
    private val TAG: String = ReportPostFragment::class.java.name
    private var _binding: FragmentReportPostBinding? = null
    private lateinit var reportPostViewModel: ReportPostViewModel
    private val binding get() = _binding!!

    private lateinit var item: Item

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "ReportPostFragment is onCreateView")
        _binding = FragmentReportPostBinding.inflate(inflater, container, false)
        val root = binding.root
        binding.reportSentText.visibility = View.GONE
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "ReportPostFragment is onViewCreated")
        setSubPageAppbar(requireActivity(), getString(R.string.report_post))
        reportPostViewModel = ViewModelProvider(this)[ReportPostViewModel::class.java]
        item = ReportPostFragmentArgs.fromBundle(requireArguments()).item
        initListeners()
    }

    override fun initListeners() {
        binding.sendReport.setOnClickListener {
            it as CircularProgressButton

            val text = binding.editTextReport.text.toString()
            if (text.isEmpty()) {
                Toast.makeText(
                    requireActivity(),
                    getString(R.string.explanation_required),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                it.startAnimation()
                val id = UUID.randomUUID().toString()
                val report = PostReport(
                    id,
                    (requireActivity() as MainActivity).getCurrentUser()!!.email,
                    Timestamp.now(),
                    item.itemId,
                    item is ItemExchange,
                    text
                )

                val language = Locale.getDefault().language
                val postType =
                    if (item is ItemExchange) getString(R.string.yes) else getString(R.string.no)
                val mail = Mail(
                    to = MailBodyConst.supportEmail,
                    message = mapOf(
                        "subject" to getString(R.string.post_report_email_subject) + id,
                        "html" to
                                if (language == "en")
                                    MailBodyConst.getReportBodyEng(
                                        text,
                                        (requireActivity() as MainActivity).getCurrentUser()!!.email,
                                        item.itemId,
                                        postType
                                    )
                                else
                                    MailBodyConst.getReportBodyRo(
                                        text,
                                        (requireActivity() as MainActivity).getCurrentUser()!!.email,
                                        item.itemId,
                                        postType
                                    )
                    )
                )

                reportPostViewModel.addReport(report, mail, object : NoParamCallback {
                    override fun onComplete() {
                        it.doneLoadingAnimation(
                            R.color.purple_medium,
                            ContextCompat.getDrawable(
                                requireActivity(),
                                R.drawable.ic_done
                            )!!.toBitmap()
                        )
                        binding.reportSentText.visibility = View.VISIBLE
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
        Log.d(TAG, "ReportPostFragment is onDestroyView")
        _binding = null
    }

}