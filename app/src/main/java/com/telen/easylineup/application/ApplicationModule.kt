package com.telen.easylineup.application

import android.app.Application
import com.github.kaygenzo.bugreporter.api.BugReporter
import com.github.kaygenzo.bugreporter.api.ReportMethod
import com.telen.easylineup.reporting.BugReporterManager
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
            .setCompressionQuality(75)
            .setImagePreviewScale(0.3f)
            .setFields(null)
            .setEmail("developer@telen.fr")
            .setReportMethods(listOf(ReportMethod.SHAKE))
            .observeResult(reporterManager)
            .setApplication(app)
            .build()
    }
    single { SharedPreferencesHelper(get()) }
}