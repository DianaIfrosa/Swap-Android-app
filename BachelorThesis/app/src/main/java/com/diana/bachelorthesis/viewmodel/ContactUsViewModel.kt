package com.diana.bachelorthesis.viewmodel

import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.Feedback
import com.diana.bachelorthesis.model.Mail
import com.diana.bachelorthesis.repository.FeedbackRepository
import com.diana.bachelorthesis.repository.MailRepository
import com.diana.bachelorthesis.utils.NoParamCallback
import java.lang.Exception

class ContactUsViewModel: ViewModel() {
    private val TAG: String = ContactUsViewModel::class.java.name
    private var feedbackRepository = FeedbackRepository.getInstance()
    private var mailRepository = MailRepository.getInstance()

    fun addFeedback(feedback: Feedback, mail: Mail, callback: NoParamCallback) {
        feedbackRepository.addFeedback(feedback, object: NoParamCallback {
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