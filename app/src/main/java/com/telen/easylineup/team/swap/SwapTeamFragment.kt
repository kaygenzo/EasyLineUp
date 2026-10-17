/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team.swap

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.utils.FirebaseAnalyticsUtils

interface SwapTeamActions {
    fun onTeamClick(team: Team)
    fun onCreateTeamClick()
}

class SwapTeamFragment : BottomSheetDialogFragment() {
    private var swapTeamActions: SwapTeamActions? = null

    fun setSwapTeamActionsListener(swapTeamActions: SwapTeamActions) {
        this.swapTeamActions = swapTeamActions
    }

    override fun onStart() {
        super.onStart()
        // The Compose content draws its own rounded top corners + surface color; clear the
        // sheet window's own white rounded background so it doesn't show behind/around it.
        (view?.parent as? View)?.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        @Suppress("UNCHECKED_CAST")
        val teams = arguments?.getSerializable(Constants.EXTRA_TEAMS) as? List<Team>
            ?: emptyList()

        return ComposeView(requireContext()).apply {
            setContent {
                DugoutTheme {
                    SwitchTeamSheetContent(
                        teams = teams,
                        onTeamClick = { team ->
                            FirebaseAnalyticsUtils.onClick(activity, "click_team_swap_selection")
                            swapTeamActions?.onTeamClick(team)
                            dismiss()
                        },
                        onCreateTeamClick = {
                            FirebaseAnalyticsUtils.onClick(activity, "click_team_swap_create")
                            swapTeamActions?.onCreateTeamClick()
                            dismiss()
                        }
                    )
                }
            }
        }
    }
}
