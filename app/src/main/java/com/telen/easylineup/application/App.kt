/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.application

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.initialize
import com.telen.easylineup.BuildConfig
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.ThemeMode
import com.telen.easylineup.utils.SharedPreferencesUtils
import com.telen.easylineup.utils.applyAsNightMode
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

open class App : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree(this))
        }

        val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        Firebase.initialize(context = this)

        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance(),
        )

        val koinApp = startKoin {
            androidContext(this@App)
            modules(ModuleProvider.modules)
        }

        ThemeMode.fromPreference(
            SharedPreferencesUtils.getStringSetting(
                this,
                R.string.key_day_night_theme,
                ThemeMode.FOLLOW_SYSTEM.preferenceValue
            )
        ).applyAsNightMode()
    }

    class ReleaseTree(private val context: Context) : Timber.DebugTree() {
        override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {
            val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.setCustomKey("installer", getInstaller() ?: "unknown")
            when (priority) {
                Log.ERROR -> throwable?.let { crashlytics.recordException(it) }
                Log.WARN -> crashlytics.log("W/${tag ?: ""}: $message")
                else -> {}
            }
        }

        private fun getInstaller(): String? {
            return context.packageManager.getInstallerPackageName(context.packageName)
        }
    }
}
