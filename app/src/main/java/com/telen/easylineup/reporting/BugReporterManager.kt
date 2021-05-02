package com.telen.easylineup.reporting

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import timber.log.Timber

class BugReporterManager(private val context: Context): Observer<Intent> {

    override fun onSubscribe(d: Disposable?) {

    }

    override fun onNext(t: Intent?) {
        t?.let {  intent ->
            intent.component = ComponentName(context, SendReportActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onError(e: Throwable?) {
        e?.printStackTrace()
        Timber.e(e)
    }

    override fun onComplete() {

    }
}