package com.telen.easylineup.utils

import android.view.View
import android.view.ViewTreeObserver

infix fun View?.ready(block: () -> Unit) {
    this?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if(this@ready.width != 0 && this@ready.height !=0) {
                this@ready.viewTreeObserver.removeOnGlobalLayoutListener(this)
                block()
            }
        }
    })
}
