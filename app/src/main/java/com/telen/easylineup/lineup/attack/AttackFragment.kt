package com.telen.easylineup.lineup.attack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.telen.easylineup.R
import com.telen.easylineup.lineup.EventCase
import com.telen.easylineup.lineup.PlayersPositionViewModel
import com.telen.easylineup.lineup.SaveBattingOrderSuccess
import com.telen.easylineup.repository.model.PlayerWithPosition
import com.telen.easylineup.views.ItemDecoratorAttackRecycler
import kotlinx.android.synthetic.main.fragment_list_batter.view.*
import timber.log.Timber

class AttackFragment: Fragment(), OnDataChangedListener {

    private lateinit var playerAdapter: BattingOrderAdapter
    private val adapterDataList = mutableListOf<PlayerWithPosition>()

    private lateinit var viewModel: PlayersPositionViewModel

    private lateinit var itemTouchedCallback: AttackItemTouchCallback
    private lateinit var itemTouchedHelper: ItemTouchHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_list_batter, container, false)

        val linearLayoutManager = LinearLayoutManager(activity)
        val dividerItemDecoration = ItemDecoratorAttackRecycler(context, linearLayoutManager.orientation)

        parentFragment?.let { parent ->
            viewModel = ViewModelProviders.of(parent).get(PlayersPositionViewModel::class.java)
            val isEditable = viewModel.editable

            viewModel.getTeamType()
                    .subscribe({ teamType ->
                        playerAdapter = BattingOrderAdapter(adapterDataList, this, isEditable, teamType)
                        itemTouchedCallback = AttackItemTouchCallback(playerAdapter)
                        itemTouchedHelper = ItemTouchHelper(itemTouchedCallback)

                        view.recyclerView.apply {
                            layoutManager = linearLayoutManager
                            addItemDecoration(dividerItemDecoration)
                            adapter = playerAdapter
                        }

                        isEditable.takeIf { it }?.let {
                            itemTouchedHelper.attachToRecyclerView(view.recyclerView)
                        }
                    }, {
                        Timber.e(it)
                    })

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

            // Fix because if i use directly the observer into the live data observe,
            // it will trigger a IllegalArgumentException: Cannot add the same observer with different lifecycles
            val lifecycleObserver = object: Observer<EventCase> {
                override fun onChanged(t: EventCase?) {
                    when(t) {
                        SaveBattingOrderSuccess -> Timber.d("Successfully saved!")
                    }
                }
            }

            viewModel.eventHandler.observe(viewLifecycleOwner, lifecycleObserver)
        }

        return view
    }

    override fun onOrderChanged() {
        save()
    }

    private fun save() {
        viewModel.saveNewBattingOrder(adapterDataList)
    }
}