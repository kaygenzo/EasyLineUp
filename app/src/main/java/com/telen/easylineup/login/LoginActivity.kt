/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.telen.easylineup.BaseImportActivity
import com.telen.easylineup.databinding.ActivityLoginBinding
import com.telen.easylineup.utils.FirebaseAnalyticsUtils

class LoginActivity : BaseImportActivity() {
    private var binding: ActivityLoginBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater).apply {
            setContentView(root)
            loginCreate.setOnClickListener {
                FirebaseAnalyticsUtils.onClick(this@LoginActivity, "click_login_create")
                launchTeamCreation()
            }

            loginImport.setOnClickListener {
                FirebaseAnalyticsUtils.onClick(this@LoginActivity, "click_login_import")
                importDataFromBrowser()
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
