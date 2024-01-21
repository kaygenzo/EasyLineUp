/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup

import androidx.appcompat.app.AppCompatActivity
import io.reactivex.rxjava3.disposables.CompositeDisposable

abstract class BaseActivity : AppCompatActivity() {
    protected val disposables by lazy { CompositeDisposable() }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
}
