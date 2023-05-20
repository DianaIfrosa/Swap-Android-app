package com.diana.bachelorthesis.viewmodel

import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.PostReport
import com.diana.bachelorthesis.repository.ReportsRepository
import com.diana.bachelorthesis.utils.NoParamCallback

class ReportPostViewModel: ViewModel() {
    private val TAG: String = ReportPostViewModel::class.java.name
    private val postReportsRepository = ReportsRepository.getInstance()

    fun addReport(report: PostReport, callback: NoParamCallback) {
        postReportsRepository.addReport(report, callback)
    }

}
