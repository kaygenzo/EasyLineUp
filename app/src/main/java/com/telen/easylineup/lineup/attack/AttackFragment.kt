package com.telen.easylineup.lineup.attack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.lineup.NewBatterOrderAvailable
import com.telen.easylineup.lineup.PlayersPositionViewModel
import com.telen.easylineup.lineup.SaveBattingOrderSuccess
import com.telen.easylineup.views.ItemDecoratorAttackRecycler
import com.telen.easylineup.views.LineupTypeface
import kotlinx.android.synthetic.main.fragment_list_batter.view.*
import timber.log.Timber

class AttackFragment: BaseFragment("AttackFragment"), OnDataChangedListener {

    private lateinit var playerAdapter: BattingOrderAdapter
    private val adapterDataList = mutableListOf<BatterState>()

    private lateinit var viewModel: PlayersPositionViewModel

    private lateinit var itemTouchedCallback: AttackItemTouchCallback
    private lateinit var itemTouchedHelper: ItemTouchHelper

    private lateinit var lineupTypeface: LineupTypeface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parentFragment?.let { parent ->
            viewModel = ViewModelProviders.of(parent).get(PlayersPositionViewModel::class.java)

            val eventsDisposable = viewModel.eventHandler.subscribe({
                when (it) {
                    SaveBattingOrderSuccess -> Timber.d("Successfully saved!")
                    is NewBatterOrderAvailable -> {
                        adapterDataList.clear()
                        adapterDataList.addAll(it.players)
                        playerAdapter.notifyDataSetChanged()
                    }
                }
            }, {
                Timber.e(it)
            })

            disposables.add(eventsDisposable)
        }

        val preferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val lineupValue = preferences.getString(getString(R.string.key_lineup_style), getString(R.string.lineup_style_default_value))
        lineupTypeface = LineupTypeface.getByValue(lineupValue)
        playerAdapter = BattingOrderAdapter(adapterDataList, this, TeamType.BASEBALL.id, lineupTypeface).apply {
            setHasStableIds(true)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_list_batter, container, false)

        val linearLayoutManager = LinearLayoutManager(activity)

        val isEditable = viewModel.editable
        val batterSize = viewModel.strategy.batterSize
        val extraHitterSize = viewModel.strategy.extraHitterSize

        itemTouchedCallback = AttackItemTouchCallback(playerAdapter, batterSize, extraHitterSize)
        itemTouchedHelper = ItemTouchHelper(itemTouchedCallback)

        view.recyclerView.apply {
            layoutManager = linearLayoutManager
            adapter = playerAdapter
            setHasFixedSize(true)
        }

        isEditable.takeIf { it }?.let {
            itemTouchedHelper.attachToRecyclerView(view.recyclerView)
        }

        val disposable = viewModel.getTeamType()
                .subscribe({
                    val dividerItemDecoration = ItemDecoratorAttackRecycler(context, linearLayoutManager.orientation, batterSize, extraHitterSize)
                    view.recyclerView.addItemDecoration(dividerItemDecoration)
                    playerAdapter.apply {
                        this.teamType = viewModel.teamType
                        notifyDataSetChanged()
                    }
                }, {
                    Timber.e(it)
                })
        disposables.add(disposable)

        view.header.setIsEditable(isEditable)

        viewModel.lineupID?.let {

            viewModel.registerLineupAndPositionsChanged().observe(viewLifecycleOwner, Observer { items ->
                viewModel.getBatterStates(items, batterSize, extraHitterSize)
            })

            viewModel.registerLineupChange().observe(viewLifecycleOwner, Observer { lineup ->
                lineup?.let {
                    playerAdapter.lineupMode = lineup.mode
                    playerAdapter.notifyDataSetChanged()
                }
            })

        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        view?.recyclerView?.apply {
            adapter = null
        }
    }

    override fun onOrderChanged() {
        viewModel.saveNewBattingOrder()
    }
}