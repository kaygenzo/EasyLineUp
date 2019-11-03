package com.telen.easylineup.team.createTeam

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.utils.Constants
import com.telen.easylineup.utils.NavigationUtils
import kotlinx.android.synthetic.main.activity_team_creation.*
import kotlinx.android.synthetic.main.view_create_team.*

class TeamCreationActivity: AppCompatActivity() {

    lateinit var viewModel: SetupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_creation)

        val navController = findNavController(R.id.nav_host_fragment)

        viewModel = ViewModelProviders.of(this).get(SetupViewModel::class.java)

        viewModel.bottomBarLiveData.observe(this, Observer {
            when(it) {
                SetupViewModel.BottomBarState.NEXT_ENABLED -> teamCreationNextButton.isEnabled = true
                SetupViewModel.BottomBarState.NEXT_DISABLED -> teamCreationNextButton.isEnabled = false
                SetupViewModel.BottomBarState.NEXT_FINISH -> buttonNext.setText(R.string.team_creation_button_finish)
                else -> {}
            }
        })

        viewModel.stepLiveData.observe(this, Observer {
            when(it) {
                SetupViewModel.NextStep.PLAYERS -> {
                    stepLayout.go(it.id, true)
                    val arguments = Bundle()
                    arguments.putBoolean(Constants.EXTRA_CLICKABLE, false)
                    navController.navigate(R.id.navigation_team, arguments, NavigationUtils().getOptions())
                }
                SetupViewModel.NextStep.TYPE -> {
                    stepLayout.go(it.id, true)
                    navController.navigate(R.id.teamTypeFragment, null, NavigationUtils().getOptions())
                }
                SetupViewModel.NextStep.FINISH -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
                else -> {}
            }
        })

        viewModel.errorLiveData.observe(this, Observer {
            when(it) {
                SetupViewModel.Error.NAME_EMPTY -> {
                    teamNameInputLayout.error = getString(R.string.team_creation_error_name_empty)
                }
                else -> Toast.makeText(this, "Something wrong happened, please try again", Toast.LENGTH_SHORT).show()
            }
        })

        teamCreationNextButton.setOnClickListener {
            viewModel.nextButtonClicked(stepLayout.currentStep)
        }
    }

    override fun onBackPressed() {
        viewModel.backPressed()
    }
}