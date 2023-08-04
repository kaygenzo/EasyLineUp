package com.telen.easylineup.lineup.edition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.databinding.FragmentLineupEditionBinding
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.RosterItem
import com.telen.easylineup.launch
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
            launch(viewModel.saveClicked(), {
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

        viewModel.observeLineup().observe(viewLifecycleOwner) {
            binding.lineupNameEditText.setText(it.name)
            binding.lineupNameEditText.addTextChangedListener {
                viewModel.onLineupNameChanged(it.toString())
            }
        }

        return binding.root
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
}