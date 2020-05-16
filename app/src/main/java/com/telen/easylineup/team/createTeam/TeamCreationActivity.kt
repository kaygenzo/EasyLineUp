package com.telen.easylineup.team.createTeam

import android.app.Activity
import android.content.DialogInterface
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.telen.easylineup.BaseActivity
import com.telen.easylineup.R
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamCreationStep
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.NavigationUtils
import kotlinx.android.synthetic.main.activity_team_creation.*
import timber.log.Timber

class TeamCreationActivity: BaseActivity() {

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
                    val disposable = viewModel.saveTeam()
                            .subscribe({
                                setResult(Activity.RESULT_OK)
                                finish()
                            }, {  error ->
                                Timber.e(error)
                            })
                    disposables.add(disposable)
                }
                TeamCreationStep.CANCEL -> {
                    DialogFactory.getWarningDialog(context = this,
                            title = R.string.discard_title,
                            message = R.string.discard_message,
                            confirmClick = DialogInterface.OnClickListener { dialog, which ->
                                finish()
                            }, confirmText = R.string.generic_discard).show()
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