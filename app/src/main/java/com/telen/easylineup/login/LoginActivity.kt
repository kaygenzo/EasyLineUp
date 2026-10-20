/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import com.telen.easylineup.BaseImportActivity
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.utils.FirebaseAnalyticsUtils

class LoginActivity : BaseImportActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DugoutTheme {
                WelcomeScreen(
                    onCreateTeam = {
                        FirebaseAnalyticsUtils.onClick(this@LoginActivity, "click_login_create")
                        launchTeamCreation()
                    },
                    onImportTeam = {
                        FirebaseAnalyticsUtils.onClick(this@LoginActivity, "click_login_import")
                        importDataFromBrowser()
                    }
                )

                ImportSheets()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CREATE_TEAM && resultCode == Activity.RESULT_OK) {
            launchHome()
        }
    }
}
