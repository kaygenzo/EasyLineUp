package com.telen.easylineup.application

import android.util.Log
import androidx.multidex.MultiDexApplication
import com.github.kaygenzo.bugreporter.BugReporter
import com.github.kaygenzo.bugreporter.ReportMethod
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.telen.easylineup.BuildConfig
import com.telen.easylineup.reporting.BugReporterManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

open class App: MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

//        StrictMode.setVmPolicy(VmPolicy.Builder()
//                .detectAll()
//                .penaltyLog()
//                .penaltyDeath()
//                .build())

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        else {
            Timber.plant(ReleaseTree())
        }

        val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        val koinApp = startKoin {
            androidContext(this@App)
            modules(ModuleProvider.modules)
        }

        BugReporter.Builder()
                .setCompressionQuality(75)
                .setImagePreviewScale(0.3f)
                .setFields(null)
                .setEmail("developer@telen.fr")
                .setReportMethods(
                        if(!BuildConfig.UseBetaTool) {
                            listOf()
                        } else {
                            listOf(ReportMethod.SHAKE, ReportMethod.FLOATING_BUTTON)
                        }
                )
                .observeResult(koinApp.koin.get<BugReporterManager>())
                .build()
                .init(this)
    }

    class ReleaseTree: Timber.DebugTree() {

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()
            when(priority) {
                Log.ERROR -> {
                    t?.let { crashlytics.recordException(t) }
                }
                Log.WARN -> {
                    crashlytics.log("W/${tag ?: "TAG"}: $message")
                }
                else -> {}
            }

        }
    }
}