/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

/**
 * Draws every screen edge to edge, on every Android version. Android 15+ enforces it for
 * targetSdk 35+ anyway; opting in everywhere makes older versions behave the same, and lets each
 * Compose root place itself with WindowInsets instead of relying on the system to pad it.
 */
abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }
}
