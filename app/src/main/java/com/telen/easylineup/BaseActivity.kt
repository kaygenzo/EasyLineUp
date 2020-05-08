package com.telen.easylineup

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import io.reactivex.disposables.CompositeDisposable

abstract class BaseActivity: AppCompatActivity() {
    protected val disposables by lazy { CompositeDisposable() }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
}