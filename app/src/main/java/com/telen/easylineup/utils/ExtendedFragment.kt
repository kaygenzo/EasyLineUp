package com.telen.easylineup.utils

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment

fun Fragment.hideSoftKeyboard() {
    activity?.run {
        val imm= getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        currentFocus?.run {
            imm?.hideSoftInputFromWindow(windowToken, 0)
        }
    }
}