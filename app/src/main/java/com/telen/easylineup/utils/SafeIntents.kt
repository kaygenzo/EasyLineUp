/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.telen.easylineup.R
import timber.log.Timber

/**
 * Starts [intent], or tells the user no installed app can handle it instead of crashing: a
 * `geo:` link on a device without a maps app, a share target that was uninstalled. Returns
 * whether an activity was started.
 */
fun Context.startActivityOrNotify(intent: Intent): Boolean {
    return try {
        startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        Timber.e(e)
        Toast.makeText(this, R.string.error_no_app_for_action, Toast.LENGTH_SHORT).show()
        false
    }
}
