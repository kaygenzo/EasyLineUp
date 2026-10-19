/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerSide
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.NavigationUtils
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class PlayersFragment : BaseFragment("TeamFragment") {
    private val viewModel: TeamViewModel by viewModel()

    private var teamName by mutableStateOf("")
    private var players by mutableStateOf<List<RosterRowState>>(emptyList())
    private var sortType by mutableStateOf(TeamViewModel.SortType.ALPHA)
    private var showMessageSheet by mutableStateOf(false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                DugoutTheme {
                    RosterScreen(
                        state = RosterState(
                            teamName = teamName,
                            players = players,
                            sortType = sortType
                        ),
                        onBackClick = { findNavController().navigateUp() },
                        onSortSelected = ::onSortSelected,
                        onPlayerClick = ::onPlayerSelected,
                        onAddPlayerClick = ::onAddPlayerClicked,
                        onMessageTeamClick = {
                            FirebaseAnalyticsUtils.onClick(activity, "click_team_players_message")
                            showMessageSheet = true
                        }
                    )

                    if (showMessageSheet) {
                        MessageTeamSheet(
                            teamName = teamName,
                            players = players,
                            onDismiss = { showMessageSheet = false },
                            onTextMessageClick = ::onMessageTextClicked,
                            onEmailClick = ::onMessageEmailClicked,
                            onShareAnotherWayClick = ::onMessageShareClicked
                        )
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.observePlayers()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { playerList -> players = playerList.map { it.toRosterRowState() } }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observeCurrentTeamName()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { teamName = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clear()
    }

    private fun onSortSelected(newSortType: TeamViewModel.SortType) {
        sortType = newSortType
        viewModel.setSortType(newSortType)
    }

    private fun onAddPlayerClicked() {
        FirebaseAnalyticsUtils.onClick(activity, "click_team_players_create")
        findNavController().navigate(R.id.playerEditFragment, null, NavigationUtils().getOptions())
    }

    private fun onPlayerSelected(playerId: Long) {
        FirebaseAnalyticsUtils.onClick(activity, "click_team_players_selected")
        val extras = Bundle()
        extras.putLong(Constants.PLAYER_ID, playerId)
        findNavController().navigate(
            R.id.playerDetailsFragment,
            extras,
            NavigationUtils().getOptions()
        )
    }

    private fun onMessageTextClicked() {
        FirebaseAnalyticsUtils.onClick(activity, "click_team_players_message_text")
        val numbers = players.mapNotNull { it.phone?.takeIf(String::isNotBlank) }
        if (numbers.isEmpty()) return
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + numbers.joinToString(";")))
        launchOrNotify(intent)
    }

    private fun onMessageEmailClicked() {
        FirebaseAnalyticsUtils.onClick(activity, "click_team_players_message_email")
        val emails = players.mapNotNull { it.email?.takeIf(String::isNotBlank) }
        if (emails.isEmpty()) return
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
            putExtra(Intent.EXTRA_EMAIL, emails.toTypedArray())
        }
        launchOrNotify(intent)
    }

    private fun onMessageShareClicked() {
        FirebaseAnalyticsUtils.onClick(activity, "click_team_players_message_share")
        val text = players.joinToString("\n") { row ->
            listOfNotNull(row.name, row.phone, row.email).joinToString(" · ")
        }
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE)
            as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(teamName, text))
        Toast.makeText(requireContext(), R.string.roster_message_share_copied, Toast.LENGTH_SHORT)
            .show()
    }

    private fun launchOrNotify(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Timber.e(e)
            Toast.makeText(requireContext(), R.string.roster_message_no_app, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun Player.toRosterRowState(): RosterRowState = RosterRowState(
        id = id,
        name = name,
        shirtNumber = shirtNumber,
        imageUri = image?.let { Uri.parse(it) },
        battingSide = PlayerSide.getSideByValue(batting),
        pitchingSide = PlayerSide.getSideByValue(pitching),
        phone = phone,
        email = email
    )
}
