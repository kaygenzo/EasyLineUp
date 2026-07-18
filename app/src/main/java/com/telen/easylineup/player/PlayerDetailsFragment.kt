/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Picasso
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.databinding.FragmentPlayerDetailsBinding
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.PlayerSide
import com.telen.easylineup.domain.model.Sex
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.ready
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

const val EMPTY_MARKER = "-"

class PlayerDetailsFragment : BaseFragment("PlayerDetailsFragment"),
AdapterView.OnItemSelectedListener {
    private val viewModel by viewModels<PlayerViewModel>()
    private var binding: FragmentPlayerDetailsBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentPlayerDetailsBinding.inflate(inflater, container, false).apply {
            binding = this
            val playerId = arguments?.getLong(Constants.PLAYER_ID, 0) ?: 0
            viewModel.playerId = playerId

            viewModel.observePlayerName()
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .onEach { playerName.text = it.trim() }
                .launchIn(viewLifecycleOwner.lifecycleScope)

            viewModel.observePlayerShirtNumber()
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .onEach { shirtNumberValue.text = it.toString() }
                .launchIn(viewLifecycleOwner.lifecycleScope)

            viewModel.observePlayerLicenseNumber()
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .onEach { playerLicenseValue.text = it.toString() }
                .launchIn(viewLifecycleOwner.lifecycleScope)

            viewModel.observePlayerImage()
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .onEach {
                    playerImage.ready {
                        try {
                            Picasso.get()
                                .load(it)
                                .resize(playerImage.width, playerImage.height)
                                .centerCrop()
                                .placeholder(R.drawable.ic_unknown_field_player)
                                .error(R.drawable.ic_unknown_field_player)
                                .into(playerImage)
                        } catch (e: IllegalArgumentException) {
                            Timber.e(e)
                        }
                    }
                }
                .launchIn(viewLifecycleOwner.lifecycleScope)

            viewModel.observePlayerPitchingSide()
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .onEach {
                    pitchingSideValue.text = when (PlayerSide.getSideByValue(it)) {
                        PlayerSide.LEFT -> getString(R.string.generic_left)
                        PlayerSide.RIGHT -> getString(R.string.generic_right)
                        PlayerSide.BOTH -> getString(R.string.generic_both)
                        null -> getString(R.string.generic_unknown)
                    }
                }
                .launchIn(viewLifecycleOwner.lifecycleScope)

            viewModel.observePlayerBattingSide()
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .onEach {
                    battingSideValue.text = when (PlayerSide.getSideByValue(it)) {
                        PlayerSide.LEFT -> getString(R.string.generic_left)
                        PlayerSide.RIGHT -> getString(R.string.generic_right)
                        PlayerSide.BOTH -> getString(R.string.generic_both)
                        null -> getString(R.string.generic_unknown)
                    }
                }
                .launchIn(viewLifecycleOwner.lifecycleScope)

            viewModel.observePlayerEmail()
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .onEach { playerEmailValue.text = it.takeIf { !it.isNullOrEmpty() } ?: EMPTY_MARKER }
                .launchIn(viewLifecycleOwner.lifecycleScope)

            viewModel.observePlayerPhoneNumber()
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .onEach { playerPhoneValue.text = it.takeIf { !it.isNullOrEmpty() } ?: EMPTY_MARKER }
                .launchIn(viewLifecycleOwner.lifecycleScope)

            viewModel.observeLineups()
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .onEach {
                    gamesPlayedValue.text = it.values.sum().toString()
                    positionsBarChart.setData(it)
                }
                .launchIn(viewLifecycleOwner.lifecycleScope)

            viewModel.observeTeamType()
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .onEach { positionsBarChart.setTeamType(it) }
                .launchIn(viewLifecycleOwner.lifecycleScope)

            viewModel.observeStrategies(requireContext())
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .onEach {
                    positionsBarChart.binding.teamStrategy.apply {
                        visibility = if (it.size > 1) View.VISIBLE else View.GONE
                        adapter = ArrayAdapter(context, R.layout.item_team_strategy, it)
                        setSelection(0, false)
                        onItemSelectedListener = this@PlayerDetailsFragment
                    }
                }
                .launchIn(viewLifecycleOwner.lifecycleScope)

            viewModel.observeStrategy()
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .onEach { positionsBarChart.apply { setStrategy(it) } }
                .launchIn(viewLifecycleOwner.lifecycleScope)

            viewModel.observePlayerSex()
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .onEach {
                    val sex = Sex.getById(it)
                    sexSymbol.visibility = if (sex != Sex.UNKNOWN) View.VISIBLE else View.GONE
                    when (sex) {
                        Sex.MALE -> sexSymbol.setImageResource(R.drawable.ic_male_black)
                        Sex.FEMALE -> sexSymbol.setImageResource(R.drawable.ic_female_black)
                        else -> {
                            /* sex is not defined for this player */
                        }
                    }
                }
                .launchIn(viewLifecycleOwner.lifecycleScope)
        }.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clear()
        binding = null
    }

    override fun onNothingSelected(adapter: AdapterView<*>?) {}
    override fun onItemSelected(adapter: AdapterView<*>?, view: View?, position: Int, row: Long) {
        FirebaseAnalyticsUtils.onClick(activity, "click_player_details_strategy_selected")
        viewModel.onStrategySelected(position)
    }
}
