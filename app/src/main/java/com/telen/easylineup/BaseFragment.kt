package com.telen.easylineup

import androidx.fragment.app.Fragment
import io.reactivex.disposables.CompositeDisposable

abstract class BaseFragment: Fragment() {
    protected val disposables by lazy { CompositeDisposable() }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
}