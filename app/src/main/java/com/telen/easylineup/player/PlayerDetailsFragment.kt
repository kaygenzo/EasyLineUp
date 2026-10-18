/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.player

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
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerSide
import com.telen.easylineup.domain.model.Sex
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.getStrategies
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.NavigationUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.rx3.rxCompletable
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayerDetailsFragment : BaseFragment("PlayerDetailsFragment") {
    private val viewModel by viewModel<PlayerViewModel>()

    private var name by mutableStateOf("")
    private var shirtNumber by mutableStateOf(0)
    private var imageUri by mutableStateOf<Uri?>(null)
    private var sex by mutableStateOf(Sex.UNKNOWN)
    private var battingSide by mutableStateOf<PlayerSide?>(null)
    private var pitchingSide by mutableStateOf<PlayerSide?>(null)
    private var licenseNumber by mutableStateOf(0L)
    private var email by mutableStateOf("")
    private var phoneNumber by mutableStateOf("")
    private var positionsData by mutableStateOf<Map<FieldPosition, Int>>(emptyMap())
    private var teamTypeId by mutableStateOf(0)
    private var strategy by mutableStateOf(TeamStrategy.STANDARD)
    private var strategyOptions by mutableStateOf<List<String>>(emptyList())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val playerId = arguments?.getLong(Constants.PLAYER_ID, 0) ?: 0
        viewModel.playerId = playerId
        return ComposeView(requireContext()).apply {
            setContent {
                DugoutTheme {
                    val teamType = TeamType.getTypeById(teamTypeId)
                    PlayerProfileScreen(
                        state = PlayerProfileState(
                            name = name,
                            shirtNumber = shirtNumber,
                            imageUri = imageUri,
                            sex = sex,
                            battingSide = battingSide,
                            pitchingSide = pitchingSide,
                            licenseNumber = licenseNumber,
                            email = email,
                            phoneNumber = phoneNumber,
                            positionsData = positionsData,
                            teamType = teamType,
                            strategy = strategy,
                            strategyOptions = strategyOptions,
                            selectedStrategyIndex = teamType.getStrategies().indexOf(strategy)
                                .coerceAtLeast(0)
                        ),
                        onStrategySelected = ::onStrategySelected,
                        onBackClick = { findNavController().navigateUp() },
                        onEditClick = ::onEditClicked,
                        onDeleteClick = ::onDeleteClicked
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.observePlayerName()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { name = it.trim() }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observePlayerShirtNumber()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { shirtNumber = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observePlayerLicenseNumber()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { licenseNumber = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observePlayerImage()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { imageUri = it?.let(Uri::parse) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observePlayerPitchingSide()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { pitchingSide = PlayerSide.getSideByValue(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observePlayerBattingSide()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { battingSide = PlayerSide.getSideByValue(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observePlayerEmail()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { email = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observePlayerPhoneNumber()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { phoneNumber = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observeLineups()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { positionsData = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observeTeamType()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { teamTypeId = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observeStrategies(requireContext())
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { strategyOptions = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observeStrategy()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { strategy = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observePlayerSex()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { sex = Sex.getById(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clear()
    }

    private fun onStrategySelected(index: Int) {
        FirebaseAnalyticsUtils.onClick(activity, "click_player_details_strategy_selected")
        viewModel.onStrategySelected(index)
    }

    private fun onEditClicked() {
        FirebaseAnalyticsUtils.onClick(activity, "click_player_details_edit")
        val extras = Bundle().apply { putLong(Constants.PLAYER_ID, viewModel.playerId) }
        findNavController().navigate(
            R.id.playerEditFragment,
            extras,
            NavigationUtils().getOptions()
        )
    }

    private fun onDeleteClicked() {
        FirebaseAnalyticsUtils.onClick(activity, "click_player_details_delete")
        activity?.let { activity ->
            val task = rxCompletable { viewModel.deletePlayer().getOrThrow() }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete { findNavController().popBackStack(R.id.navigation_team, false) }
                .doOnError {
                    val message = "Something wrong happened: ${it.message}"
                    Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
                }
            DialogFactory.getWarningTaskDialog(
                context = activity,
                title = R.string.dialog_delete_player_title,
                message = R.string.dialog_delete_cannot_undo_message,
                task = task
            ).show()
        }
    }
}
