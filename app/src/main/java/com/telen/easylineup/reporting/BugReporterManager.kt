package com.telen.easylineup.reporting

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import timber.log.Timber

class BugReporterManager(private val context: Context): Observer<Intent> {

    override fun onSubscribe(d: Disposable?) {

    }

    override fun onNext(t: Intent?) {
        t?.let {  intent ->
            val remoteConfig = Firebase.remoteConfig
            val sendByFireStore = remoteConfig.getBoolean("report_by_firestore")
            if(sendByFireStore) {
                intent.component = ComponentName(context, SendReportActivity::class.java)
            }
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