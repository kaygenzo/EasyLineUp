package com.telen.easylineup.reporting

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.github.kaygenzo.bugreporter.api.ReportMethod
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.telen.easylineup.R
import com.telen.easylineup.utils.SharedPreferencesUtils
import com.telen.easylineup.utils.UIConstants
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import timber.log.Timber

class BugReporterManager(private val context: Context) : Observer<Intent> {

    override fun onSubscribe(d: Disposable) {

    }

    override fun onNext(t: Intent) {
        val remoteConfig = Firebase.remoteConfig
        val sendByFireStore = remoteConfig.getBoolean("report_by_firestore")
        if (sendByFireStore) {
            t.component = ComponentName(context, SendReportActivity::class.java)
        }
        context.startActivity(t)
    }

    override fun onError(e: Throwable) {
        e.printStackTrace()
        Timber.e(e)
    }

    override fun onComplete() {

    }
}

fun Context.getReportMethods(): List<ReportMethod> {
    return SharedPreferencesUtils.getStringSetting(
        this,
        R.string.key_bug_report_trigger,
        UIConstants.BUG_REPORT_TRIGGER_MANUAL_VALUE.toString()
    ).toInt().run {
        when (this) {
            UIConstants.BUG_REPORT_TRIGGER_SHAKE_VALUE -> listOf(ReportMethod.SHAKE)
            UIConstants.BUG_REPORT_TRIGGER_FLOATING_BUTTON_VALUE ->
                listOf(ReportMethod.FLOATING_BUTTON)

            else -> listOf()
        }
    }
}