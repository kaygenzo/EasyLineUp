package com.telen.easylineup.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.view.View
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
                    val intent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    callback.launch(
                        Intent.createChooser(
                            intent,
                            context.getString(R.string.permission_request_pick_image_title)
                        )
                    )
                }
            }
            val compositePermissionListener =
                CompositePermissionListener(snackBarPermissionListener, permissionListener)

            Dexter.withContext(context)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(compositePermissionListener)
                .check()
        }
    }
}