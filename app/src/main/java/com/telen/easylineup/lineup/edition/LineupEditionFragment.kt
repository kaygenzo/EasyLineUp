/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.edition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.databinding.FragmentLineupEditionBinding
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.RosterItem
import com.telen.easylineup.launch
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils

class LineupEditionFragment : BaseFragment("LineupEditionFragment"), RosterAdapterCallback,
    MenuProvider {
    private val viewModel: LineupEditionViewModel by viewModels()
    private var binding: FragmentLineupEditionBinding? = null
    private val rosterItems: MutableList<RosterItem> = mutableListOf()
    private lateinit var rosterAdapter: RosterAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.lineupId = arguments?.getLong(Constants.LINEUP_ID) ?: 0
        rosterAdapter = RosterAdapter(rosterItems, this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentLineupEditionBinding.inflate(inflater, container, false).apply {
            this@LineupEditionFragment.binding = this

            activity?.addMenuProvider(this@LineupEditionFragment, viewLifecycleOwner, Lifecycle.State.RESUMED)

            activity?.onBackPressedDispatcher?.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        showDiscardDialog("back_pressed")
                    }
                })

            rosterListItems.apply {
                adapter = rosterAdapter
                layoutManager = LinearLayoutManager(context)
            }

            containerActions.saveClickListener = View.OnClickListener {
                launch(viewModel.saveClicked(), {
                    findNavController().popBackStack()
                })
            }

            containerActions.cancelClickListener = View.OnClickListener {
                showDiscardDialog("cancel")
            }

            viewModel.observeRosterItems().observe(viewLifecycleOwner) {
                rosterItems.clear()
                rosterItems.addAll(it)
                rosterAdapter.notifyDataSetChanged()
            }

            viewModel.observeLineup().observe(viewLifecycleOwner) { lineup ->
                lineupNameEditText.setText(lineup.name)
                lineupNameEditText.addTextChangedListener {
                    viewModel.onLineupNameChanged(it.toString())
                }
                launch(viewModel.getTournaments(), { tournaments ->
                    val index = tournaments.indexOfFirst { it.id == lineup.tournamentId }
                    val adapter = ArrayAdapter(
                        requireContext(),
                        R.layout.item_auto_completion,
                        tournaments.map { it.name })
                    with(tournamentChoice) {
                        setAdapter(adapter)
                        setText(tournaments[index].name, false)
                        onItemClickListener = OnItemClickListener { _, _, position, _ ->
                            viewModel.onTournamentChanged(tournaments[position])
                        }
                    }
                })
            }
        }.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onNumberChanged(player: Player, number: Int) {
        viewModel.numberChanged(player, number)
    }

    override fun onPlayerSelectedChanged(player: Player, selected: Boolean) {
        viewModel.playerSelectStatusChanged(player, selected)
    }

    companion object {
        fun getBundle(lineupId: Long): Bundle {
            return Bundle().apply { putLong(Constants.LINEUP_ID, lineupId) }
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        showDiscardDialog("navigation_up")
        return true
    }

    private fun showDiscardDialog(trigger: String) {
        FirebaseAnalyticsUtils.onClick(activity, "click_lineup_edit_cancel_$trigger")
        DialogFactory.getDiscardDialog(requireContext()) { _, _ ->
            findNavController().popBackStack()
        }.show()
    }
}
