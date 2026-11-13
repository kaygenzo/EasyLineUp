/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import timber.log.Timber

class ImagePickerUtils {
    companion object {
        fun launchPicker(callback: ActivityResultLauncher<PickVisualMediaRequest>) {
            callback.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        /**
         * Keeps the right to read the picked image after a restart. A provider may not grant a
         * persistable permission: the image still shows for now, it just may not later, which
         * is no reason to crash the pick.
         */
        fun persistImage(resolver: ContentResolver, uri: Uri) {
            try {
                resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: SecurityException) {
                Timber.w(e, "No persistable permission for the picked image")
            }
        }
    }
}
