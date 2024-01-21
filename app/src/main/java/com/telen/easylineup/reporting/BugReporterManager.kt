/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.reporting

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.github.kaygenzo.bugreporter.api.ReportMethod
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.telen.easylineup.R
import com.telen.easylineup.utils.SharedPreferencesUtils
import com.telen.easylineup.utils.UiConstants
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import timber.log.Timber

class BugReporterManager(private val context: Context) : Observer<Intent> {
    override fun onSubscribe(disposable: Disposable) {

    }

    override fun onNext(intent: Intent) {
        val remoteConfig = Firebase.remoteConfig
        val sendByFireStore = remoteConfig.getBoolean("report_by_firestore")
        if (sendByFireStore) {
            intent.component = ComponentName(context, SendReportActivity::class.java)
        }
        context.startActivity(intent)
    }

    override fun onError(ex: Throwable) {
        ex.printStackTrace()
        Timber.e(ex)
    }

    override fun onComplete() {

    }
}

fun Context.getReportMethods(): List<ReportMethod> {
    return SharedPreferencesUtils.getStringSetting(
        this,
        R.string.key_bug_report_trigger,
        UiConstants.BUG_REPORT_TRIGGER_MANUAL_VALUE.toString()
    ).toInt().run {
        when (this) {
            UiConstants.BUG_REPORT_TRIGGER_SHAKE_VALUE -> listOf(ReportMethod.SHAKE)
            UiConstants.BUG_REPORT_TRIGGER_FLOATING_BUTTON_VALUE ->
                listOf(ReportMethod.FLOATING_BUTTON)

            else -> listOf()
        }
    }
}
