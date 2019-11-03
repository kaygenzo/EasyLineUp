package com.telen.easylineup.utils

import android.app.Activity
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker
import com.telen.easylineup.R

class ImagePickerUtils {
    companion object {
        open fun launchPicker(fragment: Fragment) {
            fragment.context?.let {
                ImagePicker.with(fragment)             //  Initialize ImagePicker with activity or fragment context
                        .setToolbarColor(colorToHexString(ContextCompat.getColor(it, R.color.colorPrimary)))         //  Toolbar color
                        .setStatusBarColor(colorToHexString(ContextCompat.getColor(it, R.color.colorPrimaryDark)))       //  StatusBar color (works with SDK >= 21  )
                        .setToolbarTextColor(colorToHexString(ContextCompat.getColor(it, R.color.white)))     //  Toolbar text color (Title and Done button)
                        .setToolbarIconColor(colorToHexString(ContextCompat.getColor(it, R.color.white)))     //  Toolbar icon color (Back and Camera button)
                        .setProgressBarColor("#4CAF50")     //  ProgressBar color
                        .setBackgroundColor(colorToHexString(ContextCompat.getColor(it, R.color.white)))      //  Background color
                        .setCameraOnly(false)               //  Camera mode
                        .setMultipleMode(false)             //  Select multiple images or single image
                        .setFolderMode(true)                //  Folder mode
                        .setShowCamera(true)                //  Show camera button
                        .setFolderTitle(it.getString(R.string.gallery_picker_default_title)) //  Folder title (works with FolderMode = true)
                        .setMaxSize(1)                      //  Max images can be selected
                        .setSavePath(it.getString(R.string.app_name)) //  Image capture folder name
                        .setAlwaysShowDoneButton(true)      //  Set always show done button in multiple mode
                        .setKeepScreenOn(false)             //  Keep screen on when selecting images
                        .start()                            //  Start ImagePicker
            }
        }

        private fun colorToHexString(color: Int): String {
            return String.format("#%06X", (0xFFFFFF and color))
        }
    }
}