package com.diana.bachelorthesis.viewmodel

import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.Feedback
import com.diana.bachelorthesis.repository.FeedbackRepository
import com.diana.bachelorthesis.utils.NoParamCallback
import com.diana.bachelorthesis.view.ContactUsFragment

class ContactUsViewModel: ViewModel() {
    private val TAG: String = ContactUsViewModel::class.java.name
    private var feedbackRepository = FeedbackRepository.getInstance()


    fun addFeedback(feedback: Feedback, callback: NoParamCallback) {
        feedbackRepository.addFeedback(feedback, callback)
    }
}