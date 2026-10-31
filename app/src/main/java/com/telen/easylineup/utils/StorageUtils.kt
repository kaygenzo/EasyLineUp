/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import android.app.Activity
import android.content.Intent
import androidx.core.app.ActivityCompat

class StorageUtils {
    companion object {
        const val REQUEST_CODE_OPEN_DOCUMENT = 0
        const val REQUEST_CODE_CHOOSE_DIRECTORY = 1

        fun openFile(activity: Activity) {
            ActivityCompat.startActivityForResult(
                activity,
                getOpenFileIntent(),
                REQUEST_CODE_OPEN_DOCUMENT,
                null
            )
        }

        private fun getOpenFileIntent(): Intent {
            return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "application/octet-stream"
            }
        }

        fun createFile(activity: Activity) {
            ActivityCompat.startActivityForResult(
                activity,
                getChooseDirectoryIntent(),
                REQUEST_CODE_CHOOSE_DIRECTORY,
                null
            )
        }

        private fun getChooseDirectoryIntent(): Intent {
            return Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        }
    }
}
