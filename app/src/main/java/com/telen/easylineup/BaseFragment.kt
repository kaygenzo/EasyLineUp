package com.telen.easylineup

import androidx.fragment.app.Fragment
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import io.reactivex.disposables.CompositeDisposable

abstract class BaseFragment(private val fragmentName: String): Fragment() {
    protected val disposables by lazy { CompositeDisposable() }

    override fun onResume() {
        super.onResume()
        activity?.run {
            FirebaseAnalyticsUtils.onScreen(this, fragmentName)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
}