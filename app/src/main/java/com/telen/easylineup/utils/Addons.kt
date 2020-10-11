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

infix fun View?.drawn(block: () -> Unit) {
    this?.viewTreeObserver?.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {

        override fun onPreDraw(): Boolean {
            if(this@drawn.width != 0 && this@drawn.height !=0) {
                this@drawn.viewTreeObserver.removeOnPreDrawListener(this)
                block()
                return true
            }
            return false
        }
    })
}