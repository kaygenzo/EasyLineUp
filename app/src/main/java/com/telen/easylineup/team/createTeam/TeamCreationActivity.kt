/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team.createTeam

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.telen.easylineup.BaseActivity
import com.telen.easylineup.R
import com.telen.easylineup.databinding.ActivityTeamCreationBinding
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import timber.log.Timber

class TeamCreationActivity : BaseActivity() {
    val viewModel by viewModels<SetupViewModel>()
    private var binding: ActivityTeamCreationBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeamCreationBinding.inflate(layoutInflater).apply {
            setContentView(root)

            viewModel.setTeam(intent.extras?.get(Constants.EXTRA_TEAM) as? Team)

            teamCreationActionButtons.saveClickListener = View.OnClickListener {
                disposables.add(viewModel.onSaveClicked().subscribe({
                    FirebaseAnalyticsUtils.endTutorial(this@TeamCreationActivity)
                    setResult(Activity.RESULT_OK)
                    finish()
                }, {
                    Timber.d(it)
                }))
            }

            teamCreationActionButtons.cancelClickListener = View.OnClickListener {
                cancel()
            }
        }
    }

    override fun onBackPressed() {
        FirebaseAnalyticsUtils.onClick(this, "click_team_creation_back_clicked")
        cancel()
    }

    private fun cancel() {
        DialogFactory.getWarningDialog(
            context = this@TeamCreationActivity,
            title = R.string.discard_title,
            message = R.string.discard_message,
            confirmClick = { _, _ -> finish() },
            confirmText = R.string.generic_discard
        ).show()
    }
}
