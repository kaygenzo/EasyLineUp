/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.application

import android.app.Application

import com.github.kaygenzo.bugreporter.api.BugReporter
import com.telen.easylineup.R
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.reporting.BugReporterManager
import com.telen.easylineup.reporting.getReportMethods
import com.telen.easylineup.utils.SharedPreferencesHelper
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModules = module {
    single { AndroidSchedulers.mainThread() }
    single { BugReporterManager(androidContext()) }
    single {
        val app: Application = get()
        val reporterManager: BugReporterManager = get()
        BugReporter.Builder()
            .setCompressionQuality(Constants.REPORTING_COMPRESSION_QUALITY)
            .setImagePreviewScale(Constants.REPORTING_IMAGE_PREVIEW_SCALE)
            .setFields(null)
            .setEmail("developer@telen.fr")
            .setReportMethods(app.getReportMethods())
            .setReportFloatingImage(R.drawable.image_baseball_ball)
            .observeResult(reporterManager)
            .build(app)
    }
    single { SharedPreferencesHelper(get()) }
}
