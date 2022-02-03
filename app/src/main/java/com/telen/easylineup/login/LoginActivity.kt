package com.telen.easylineup.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.telen.easylineup.BaseImportActivity
import com.telen.easylineup.R
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity: BaseImportActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_create.setOnClickListener {
            FirebaseAnalyticsUtils.onClick(this, "click_login_create")
            launchTeamCreation()
        }

        login_import.setOnClickListener {
            FirebaseAnalyticsUtils.onClick(this, "click_login_import")
            importDataFromBrowser()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CREATE_TEAM && resultCode == Activity.RESULT_OK) {
            launchHome()
        }
    }
}