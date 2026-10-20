/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.splashscreen

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import com.telen.easylineup.BaseImportActivity
import com.telen.easylineup.R
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.utils.DialogFactory
import timber.log.Timber

class ImportDataActivity : BaseImportActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DugoutTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    ImportSheets(onCancelled = ::finish)
                }
            }
        }

        val uri = intent.data
        if (uri != null) {
            viewModel.peekImportFile(uri)
        } else {
            Timber.d("No data found, it's not expected")
            finish()
        }
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
