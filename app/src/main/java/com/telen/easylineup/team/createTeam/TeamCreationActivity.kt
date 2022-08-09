package com.telen.easylineup.team.createTeam

import android.app.Activity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.findNavController
import com.telen.easylineup.BaseActivity
import com.telen.easylineup.R
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamCreationStep
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.NavigationUtils
import kotlinx.android.synthetic.main.activity_team_creation.*

class TeamCreationActivity : BaseActivity() {

    val viewModel by viewModels<SetupViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_creation)

        val navController = findNavController(R.id.nav_host_fragment)

        viewModel.setTeam(intent.extras?.get(Constants.EXTRA_TEAM) as? Team)

        viewModel.observeStep().observe(this) {
            val isNext = it.first.nextStep.id < it.second.nextStep.id

            when (it.second.nextStep) {
                TeamCreationStep.TYPE -> {
                    if (isNext) {
                        val options = NavigationUtils().getOptions()
                        navController.navigate(R.id.teamTypeFragment, null, options)
                    } else {
                        navController.popBackStack(R.id.teamTypeFragment, false)
                    }
                }
                TeamCreationStep.TEAM -> {
                    if (!isNext) {
                        navController.popBackStack(R.id.teamCreationName, false)
                    }
                }
                TeamCreationStep.FINISH -> {
                    FirebaseAnalyticsUtils.endTutorial(this)
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                TeamCreationStep.CANCEL -> {
                    DialogFactory.getWarningDialog(
                        context = this,
                        title = R.string.discard_title,
                        message = R.string.discard_message,
                        confirmClick = { dialog, which -> finish() },
                        cancelClick = { dialog, which -> viewModel.onCancelClicked() },
                        confirmText = R.string.generic_discard
                    ).show()
                    return@observe
                }
                else -> {}
            }

            buttonNext.visibility = it.second.nextButtonVisibility
            buttonNext.isEnabled = it.second.nextButtonEnabled

            buttonPrevious.visibility = it.second.previousButtonVisibility
            buttonPrevious.isEnabled = it.second.previousButtonEnabled

            if (it.second.nextButtonLabel > 0)
                buttonNext.setText(it.second.nextButtonLabel)

            if (it.second.previousButtonLabel > 0)
                buttonPrevious.setText(it.second.previousButtonLabel)
        }

        buttonNext.setOnClickListener {
            FirebaseAnalyticsUtils.onClick(this, "click_team_creation_next")
            viewModel.nextButtonClicked()
        }

        buttonPrevious.setOnClickListener {
            FirebaseAnalyticsUtils.onClick(this, "click_team_creation_previous")
            viewModel.previousButtonClicked()
        }
    }

    override fun onBackPressed() {
        FirebaseAnalyticsUtils.onClick(this, "click_team_creation_back_clicked")
        viewModel.previousButtonClicked()
    }
}