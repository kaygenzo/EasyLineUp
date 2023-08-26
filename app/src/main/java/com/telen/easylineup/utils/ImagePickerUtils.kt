package com.telen.easylineup.utils

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.BasePermissionListener
import com.karumi.dexter.listener.single.CompositePermissionListener
import com.karumi.dexter.listener.single.PermissionListener
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener
import com.telen.easylineup.R

class ImagePickerUtils {
    companion object {
        fun launchPicker(context: Context, view: View?, callback: ActivityResultLauncher<Intent>) {

            val snackBarPermissionListener: PermissionListener =
                SnackbarOnDeniedPermissionListener.Builder
                    .with(view, R.string.permission_request_read_external_storage)
                    .withOpenSettingsButton(
                        context.getString(R.string.permission_request_settings_action)
                    )
                    .build()

            val permissionListener = object : BasePermissionListener() {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).applyImageTypes(arrayOf())
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    try {
                        callback.launch(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, context.getString(R.string.file_explorer_not_found), Toast.LENGTH_LONG).show()
                    }
                }
            }
            val compositePermissionListener =
                CompositePermissionListener(snackBarPermissionListener, permissionListener)

            Dexter.withContext(context)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(compositePermissionListener)
                .check()
        }

        fun persistImage(resolver: ContentResolver, uri: Uri) {
            resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        private fun Intent.applyImageTypes(mimeTypes: Array<String>): Intent {
            // Apply filter to show image only in intent
            type = "image/*"
            if (mimeTypes.isNotEmpty()) {
                putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            }
            return this
        }
    }
}