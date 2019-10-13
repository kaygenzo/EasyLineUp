package com.telen.easylineup.lineup.attack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.telen.easylineup.R
import com.telen.easylineup.data.PlayerWithPosition
import com.telen.easylineup.lineup.PlayersPositionViewModel
import com.telen.easylineup.views.ItemDecoratorAttackRecycler
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
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

            playerAdapter = BattingOrderAdapter(adapterDataList, this, isEditable)
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

            view.header.setIsEditable(isEditable)

            viewModel.lineupID?.let {
                viewModel.getPlayersWithPositions(it).observe(this, Observer { items ->
                    Timber.d("PlayerWithPositions list changed!")
                    adapterDataList.clear()
                    adapterDataList.addAll(items)
                    playerAdapter.notifyDataSetChanged()
                })
            }
        }

        return view
    }

    override fun onOrderChanged() {
        save().doOnComplete {
            Timber.d("Successfully saved!")
        }.subscribe()
    }

    private fun save(): Completable {
        return viewModel.saveNewBattingOrder(adapterDataList)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
    }
}