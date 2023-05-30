package com.diana.bachelorthesis.viewmodel

import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.Mail
import com.diana.bachelorthesis.model.PostReport
import com.diana.bachelorthesis.repository.MailRepository
import com.diana.bachelorthesis.repository.ReportsRepository
import com.diana.bachelorthesis.utils.NoParamCallback
import java.lang.Exception

class ReportPostViewModel: ViewModel() {
    private val TAG: String = ReportPostViewModel::class.java.name
    private val postReportsRepository = ReportsRepository.getInstance()
    private val mailRepository = MailRepository.getInstance()

    fun addReport(report: PostReport, mail: Mail, callback: NoParamCallback) {
        postReportsRepository.addReport(report, object: NoParamCallback{
            override fun onComplete() {
               mailRepository.addMailEntry(mail)
                callback.onComplete()
            }

            override fun onError(e: Exception?) {
               callback.onError(e)
            }
        })
    }

}
