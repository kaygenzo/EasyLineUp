/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.splashscreen

import android.net.Uri
import android.os.Bundle
import com.telen.easylineup.BaseImportActivity
import com.telen.easylineup.R
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import timber.log.Timber

class ImportDataActivity : BaseImportActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.data?.let {
            checkDataUri(it)
        } ?: let {
            Timber.d("No data found, it's not expected")
            finish()
        }
    }

    private fun checkDataUri(uri: Uri) {
        DialogFactory.getSimpleDialog(
            context = this,
            message = R.string.app_deeplink_file_import_title,
            confirmClick = { _, _ ->
                FirebaseAnalyticsUtils.onClick(this, "click_splashscreen_import")
                importData(uri)
            },
            cancelClick = { dialogInterface, _ ->
                dialogInterface.dismiss()
                finish()
            },
            cancelable = false
        ).show()
    }

    override fun displayImportFailure() {
        DialogFactory.getErrorDialog(
            context = this,
            title = R.string.settings_import_error_title,
            message = R.string.settings_import_error_message,
            confirmClick = { dialogInterface, _ ->
                dialogInterface.dismiss()
                finish()
            }).show()
    }
}
