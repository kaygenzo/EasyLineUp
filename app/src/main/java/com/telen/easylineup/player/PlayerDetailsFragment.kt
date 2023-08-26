package com.telen.easylineup.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.squareup.picasso.Picasso
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.databinding.FragmentPlayerDetailsBinding
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.PlayerSide
import com.telen.easylineup.domain.model.Sex
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.ready
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
            val playerID = arguments?.getLong(Constants.PLAYER_ID, 0) ?: 0
            viewModel.playerID = playerID

            viewModel.observePlayerName().observe(viewLifecycleOwner) {
                playerName.text = it.trim()
            }

            viewModel.observePlayerShirtNumber().observe(viewLifecycleOwner) {
                shirtNumberValue.text = it.toString()
            }

            viewModel.observePlayerLicenseNumber().observe(viewLifecycleOwner) {
                playerLicenseValue.text = it.toString()
            }

            viewModel.observePlayerImage().observe(viewLifecycleOwner) {
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

            viewModel.observePlayerPitchingSide().observe(viewLifecycleOwner) {
                pitchingSideValue.text = when (PlayerSide.getSideByValue(it)) {
                    PlayerSide.LEFT -> getString(R.string.generic_left)
                    PlayerSide.RIGHT -> getString(R.string.generic_right)
                    PlayerSide.BOTH -> getString(R.string.generic_both)
                    null -> getString(R.string.generic_unknown)
                }
            }

            viewModel.observePlayerBattingSide().observe(viewLifecycleOwner) {
                battingSideValue.text = when (PlayerSide.getSideByValue(it)) {
                    PlayerSide.LEFT -> getString(R.string.generic_left)
                    PlayerSide.RIGHT -> getString(R.string.generic_right)
                    PlayerSide.BOTH -> getString(R.string.generic_both)
                    null -> getString(R.string.generic_unknown)
                }
            }

            viewModel.observePlayerEmail().observe(viewLifecycleOwner) {
                playerEmailValue.text = it.takeIf { !it.isNullOrEmpty() } ?: EMPTY_MARKER
            }

            viewModel.observePlayerPhoneNumber().observe(viewLifecycleOwner) {
                playerPhoneValue.text = it.takeIf { !it.isNullOrEmpty() } ?: EMPTY_MARKER
            }

            viewModel.observeLineups().observe(viewLifecycleOwner) {
                gamesPlayedValue.text = it.values.sum().toString()
                positionsBarChart.setData(it)
            }

            viewModel.observeTeamType().observe(viewLifecycleOwner, Observer {
                positionsBarChart.setTeamType(it)
            })

            viewModel.observeStrategies(requireContext()).observe(viewLifecycleOwner) {
                positionsBarChart.binding.teamStrategy.apply {
                    visibility = if (it.size > 1) View.VISIBLE else View.GONE
                    adapter = ArrayAdapter(context, R.layout.item_team_strategy, it)
                    setSelection(0, false)
                    onItemSelectedListener = this@PlayerDetailsFragment
                }
            }

            viewModel.observeStrategy().observe(viewLifecycleOwner) {
                positionsBarChart.apply { setStrategy(it) }
            }

            viewModel.observePlayerSex().observe(viewLifecycleOwner) {
                val sex = Sex.getById(it)
                sexSymbol.visibility = if (sex != Sex.UNKNOWN) View.VISIBLE else View.GONE
                when (sex) {
                    Sex.MALE -> sexSymbol.setImageResource(R.drawable.ic_male_black)
                    Sex.FEMALE -> sexSymbol.setImageResource(R.drawable.ic_female_black)
                    else -> { /* sex is not defined for this player */
                    }
                }
            }
        }.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clear()
        binding = null
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {}
    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
        FirebaseAnalyticsUtils.onClick(activity, "click_player_details_strategy_selected")
        viewModel.onStrategySelected(position)
    }
}