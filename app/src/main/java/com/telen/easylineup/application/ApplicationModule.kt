package com.telen.easylineup.application

import com.telen.easylineup.reporting.BugReporterManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModules = module {
    single { AndroidSchedulers.mainThread() }
    single { BugReporterManager(androidContext()) }
}