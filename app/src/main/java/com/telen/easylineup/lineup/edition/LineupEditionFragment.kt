package com.telen.easylineup.lineup.edition

import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.databinding.FragmentLineupEditionBinding
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.RosterItem
import com.telen.easylineup.launch
import com.telen.easylineup.utils.DialogFactory
import timber.log.Timber

class LineupEditionFragment : BaseFragment("LineupEditionFragment"), RosterAdapterCallback {

    private lateinit var rosterAdapter: RosterAdapter

    private val viewModel: LineupEditionViewModel by viewModels()
    private var binding: FragmentLineupEditionBinding? = null
    private val rosterItems = mutableListOf<RosterItem>()

    companion object {
        fun getBundle(lineupID: Long): Bundle {
            return Bundle().apply { putLong(Constants.LINEUP_ID, lineupID) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel.lineupID = arguments?.getLong(Constants.LINEUP_ID) ?: 0
        rosterAdapter = RosterAdapter(rosterItems, this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentLineupEditionBinding.inflate(inflater, container, false)
        this.binding = binding

        binding.rosterListItems.apply {
            adapter = rosterAdapter
            layoutManager = LinearLayoutManager(context)
        }

        binding.containerActions.saveClickListener = View.OnClickListener {
            launch(viewModel.saveOverlays(), {
                findNavController().popBackStack()
            }, {
                Timber.e(it)
            })
        }

        binding.containerActions.cancelClickListener = View.OnClickListener {
            findNavController().popBackStack()
        }

        viewModel.observeRosterItems().observe(viewLifecycleOwner) {
            this.rosterItems.clear()
            this.rosterItems.addAll(it)
            rosterAdapter.notifyDataSetChanged()
        }

        launch(viewModel.loadRoster(), { }, {
            Timber.e(it)
        })

        viewModel.observeRosterItems().observe(viewLifecycleOwner) {
            this.rosterItems.run {
                clear()
                addAll(it)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onNumberChanged(number: Int, item: RosterItem) {
        viewModel.numberChanged(number, item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_roaster_edition, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        activity?.let { activity ->
            launch(viewModel.getRoster(), { roster ->
                val names = mutableListOf<CharSequence>().apply {
                    addAll(roster.map { it.player.name })
                }
                val checked = mutableListOf<Boolean>().apply {
                    addAll(roster.map { it.status })
                }

                DialogFactory.getMultiChoiceDialog(
                    context = activity,
                    title = R.string.roster_list_player_dialog_title,
                    items = names.toTypedArray(),
                    checkedItems = checked.toBooleanArray(),
                    listener = { _, which, isChecked -> roster[which].status = isChecked },
                    confirmClick = { _, _ ->
                        launch(viewModel.saveUpdatedRoster(roster), {}, { Timber.e(it) })
                    }
                ).show()
            }, {
                Timber.e(it)
            })
        }
        return super.onOptionsItemSelected(item)
    }
}