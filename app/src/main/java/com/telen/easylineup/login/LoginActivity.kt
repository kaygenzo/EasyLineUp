package com.telen.easylineup.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.splashscreen.SplashScreenActivity
import com.telen.easylineup.team.createTeam.TeamCreationActivity
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        login_create.setOnClickListener {
            launchTeamCreation()
        }

        login_import.setOnClickListener {

        }
    }

    private fun launchTeamCreation() {
        val intent = Intent(this, TeamCreationActivity::class.java)
        startActivityForResult(intent, SplashScreenActivity.REQUEST_CREATE_TEAM)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == SplashScreenActivity.REQUEST_CREATE_TEAM && resultCode == Activity.RESULT_OK) {
            launchHome()
        }
    }

    private fun launchHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}