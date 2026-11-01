/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.application

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.google.firebase.appcheck.ktx.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.telen.easylineup.BuildConfig
import com.telen.easylineup.R
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.utils.SharedPreferencesUtils
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

open class App : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()

        // StrictMode.setVmPolicy(VmPolicy.Builder()
        // .detectAll()
        // .penaltyLog()
        // .penaltyDeath()
        // .build())

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

        SharedPreferencesUtils.getStringSetting(
            this,
            R.string.key_day_night_theme,
            getString(R.string.lineup_theme_default_value)
        ).let {
            val styleValue = it.toInt()
            AppCompatDelegate.setDefaultNightMode(when (styleValue) {
                Constants.MODE_NIGHT_NO -> AppCompatDelegate.MODE_NIGHT_NO
                Constants.MODE_NIGHT_YES -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            })
        }
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
