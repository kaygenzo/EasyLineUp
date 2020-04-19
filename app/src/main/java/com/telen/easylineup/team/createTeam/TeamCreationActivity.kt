package com.telen.easylineup.team.createTeam

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.telen.easylineup.R
import com.telen.easylineup.domain.TeamCreationStep
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

            val isNext = stepLayout.currentStep < it.nextStep.id

            when(it.nextStep) {
                TeamCreationStep.TYPE -> {
                    if(isNext)
                        navController.navigate(R.id.teamTypeFragment, null, NavigationUtils().getOptions())
                    else {
                        navController.popBackStack(R.id.teamTypeFragment, false)
                    }
                }
                TeamCreationStep.TEAM -> {
                    if(!isNext) {
                        navController.popBackStack(R.id.teamCreationName, false)
                    }
                }
                TeamCreationStep.FINISH -> {
                    viewModel.saveTeam()
                            .subscribe({
                                setResult(Activity.RESULT_OK)
                                finish()
                            }, {  error ->
                                Timber.e(error)
                            })
                }
                TeamCreationStep.CANCEL -> {
                    AlertDialog.Builder(this)
                            .setMessage(R.string.team_creation_cancel_message)
                            .setPositiveButton(android.R.string.ok) { dialog, which ->
                                finish()
                            }
                            .setNegativeButton(android.R.string.cancel) { dialog, which ->
                                dialog.dismiss()
                            }
                            .create()
                            .show()
                    return@Observer
                }
                else -> {}
            }

            buttonNext.visibility = it.nextButtonVisibility
            buttonNext.isEnabled = it.nextButtonEnabled

            buttonPrevious.visibility = it.previousButtonVisibility
            buttonPrevious.isEnabled = it.previousButtonEnabled

            if(it.nextButtonLabel > 0)
                buttonNext.setText(it.nextButtonLabel)

            if(it.previousButtonLabel > 0)
                buttonPrevious.setText(it.previousButtonLabel)
            
            stepLayout.go(it.nextStep.id, true)
        })

        buttonNext.setOnClickListener { viewModel.nextButtonClicked(stepLayout.currentStep) }
        buttonPrevious.setOnClickListener { viewModel.previousButtonClicked(stepLayout.currentStep) }
    }

    override fun onBackPressed() {
        viewModel.previousButtonClicked(stepLayout.currentStep)
    }
}