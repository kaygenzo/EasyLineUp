package com.telen.easylineup.team.createTeam

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.telen.easylineup.R
import com.telen.easylineup.domain.GetTeamCreationNextStep
import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.repository.model.Team
import com.telen.easylineup.utils.NavigationUtils
import kotlinx.android.synthetic.main.activity_team_creation.*
import timber.log.Timber

class TeamCreationActivity: AppCompatActivity() {

    lateinit var viewModel: SetupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_creation)

        val navController = findNavController(R.id.nav_host_fragment)

        viewModel = ViewModelProviders.of(this).get(SetupViewModel::class.java)

        intent.extras?.get(Constants.EXTRA_TEAM)?.let {
            viewModel.team = it as Team
        }

        viewModel.stepLiveData.observe(this, Observer {

            stepLayout.go(it.nextStep.id, true)

            buttonNext.visibility = it.nextButtonVisibility
            buttonNext.isEnabled = it.nextButtonEnabled
            if(it.nextButtonLabel > 0)
                buttonNext.setText(it.nextButtonLabel)

            when(it.nextStep) {
                GetTeamCreationNextStep.TeamCreationStep.TYPE -> {
                    navController.navigate(R.id.teamTypeFragment, null, NavigationUtils().getOptions())
                }
                GetTeamCreationNextStep.TeamCreationStep.PLAYERS -> {
                    val arguments = Bundle()
                    arguments.putBoolean(Constants.EXTRA_CLICKABLE, false)
                    navController.navigate(R.id.navigation_team, arguments, NavigationUtils().getOptions())
                }
                GetTeamCreationNextStep.TeamCreationStep.FINISH -> {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                else -> {}
            }
        })

        buttonNext.setOnClickListener {
            viewModel.nextButtonClicked(stepLayout.currentStep)
        }
    }

    override fun onBackPressed() {
        viewModel.backPressed(intent.extras)
                .subscribe({
                    finish()
                }, {
                    Timber.d("Cannot quit")
                })
    }
}