package com.telen.easylineup.lineup.attack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.lineup.PlayersPositionViewModel
import com.telen.easylineup.lineup.SaveBattingOrderSuccess
import com.telen.easylineup.views.ItemDecoratorAttackRecycler
import com.telen.easylineup.views.LineupTypeface
import kotlinx.android.synthetic.main.fragment_list_batter.view.*
import timber.log.Timber

class AttackFragment: BaseFragment("AttackFragment"), OnDataChangedListener {

    private lateinit var playerAdapter: BattingOrderAdapter
    private val adapterDataList = mutableListOf<PlayerWithPosition>()

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
                }
            }, {
                Timber.e(it)
            })

            disposables.add(eventsDisposable)
        }

        val preferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val lineupValue = preferences.getString(getString(R.string.key_lineup_style), getString(R.string.lineup_style_default_value))
        lineupTypeface = LineupTypeface.getByValue(lineupValue)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_list_batter, container, false)

        val linearLayoutManager = LinearLayoutManager(activity)

        val isEditable = viewModel.editable

        playerAdapter = BattingOrderAdapter(adapterDataList, this, isEditable, TeamType.BASEBALL.id, lineupTypeface, viewModel.strategy)

        itemTouchedCallback = AttackItemTouchCallback(playerAdapter, TeamStrategy.STANDARD.batterSize, TeamStrategy.STANDARD.extraHitterSize)
        itemTouchedHelper = ItemTouchHelper(itemTouchedCallback)

        view.recyclerView.apply {
            layoutManager = linearLayoutManager
            adapter = playerAdapter
        }

        isEditable.takeIf { it }?.let {
            itemTouchedHelper.attachToRecyclerView(view.recyclerView)
        }

        val disposable = viewModel.getTeamType()
                .subscribe({
                    val batterSize = viewModel.strategy.batterSize
                    val extraHitterSize = 0
                    val dividerItemDecoration = ItemDecoratorAttackRecycler(context, linearLayoutManager.orientation, batterSize, extraHitterSize)
                    view.recyclerView.addItemDecoration(dividerItemDecoration)
                    playerAdapter.apply {
                        this.teamType = viewModel.teamType
                        itemTouchedCallback.batterSize = batterSize
                        itemTouchedCallback.extraHitterSize = extraHitterSize
                        notifyDataSetChanged()
                    }
                }, {
                    Timber.e(it)
                })
        disposables.add(disposable)

        view.header.setIsEditable(isEditable)

        viewModel.lineupID?.let {
            viewModel.registerLineupBatters().observe(viewLifecycleOwner, Observer { items ->
                Timber.d("PlayerWithPositions list changed!")

                val diffCallback = BattersDiffCallback(adapterDataList, items)
                val diffResult = DiffUtil.calculateDiff(diffCallback)

                adapterDataList.clear()
                adapterDataList.addAll(items)

                diffResult.dispatchUpdatesTo(playerAdapter)
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
        save()
    }

    private fun save() {
        viewModel.saveNewBattingOrder(adapterDataList)
    }
}