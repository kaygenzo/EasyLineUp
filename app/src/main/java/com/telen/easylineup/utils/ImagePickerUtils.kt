package com.telen.easylineup.utils

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher

class ImagePickerUtils {
    companion object {
        fun launchPicker(callback: ActivityResultLauncher<Intent>) {
            val intent = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
            }
            callback.launch(Intent.createChooser(intent, "Select Picture"))
        }
    }
}