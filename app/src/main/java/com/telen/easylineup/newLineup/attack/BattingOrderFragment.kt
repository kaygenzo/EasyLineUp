package com.telen.easylineup.newLineup.attack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.telen.easylineup.R
import com.telen.easylineup.data.PlayerWithPosition
import com.telen.easylineup.newLineup.NewLineUpActivity
import com.telen.easylineup.newLineup.PlayersPositionViewModel
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_list_batter.view.*
import kotlinx.android.synthetic.main.new_lineup_activity.view.*
import timber.log.Timber

class BattingOrderFragment: Fragment(), OnDataChangedListener {

    private lateinit var playerAdapter: BattingOrderAdapter
    private val adapterDataList = mutableListOf<PlayerWithPosition>()

    private lateinit var viewModel: PlayersPositionViewModel

    private lateinit var itemTouchedCallback: BattingOrderItemTouchCallback
    private lateinit var itemTouchedHelper: ItemTouchHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playerAdapter = BattingOrderAdapter(adapterDataList, this)
        itemTouchedCallback = BattingOrderItemTouchCallback(playerAdapter)
        itemTouchedHelper = ItemTouchHelper(itemTouchedCallback)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_list_batter, container, false)

        view.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = playerAdapter
        }

        itemTouchedHelper.attachToRecyclerView(view.recyclerView)

        viewModel = ViewModelProviders.of(activity as NewLineUpActivity).get(PlayersPositionViewModel::class.java)
        viewModel.getPlayersWithPositions().observe(this, Observer { items ->
            Timber.d("PlayerFieldPosition list changed!")
            adapterDataList.clear()
            adapterDataList.addAll(items)
            playerAdapter.notifyDataSetChanged()
        })

        return view
    }

    override fun onOrderChanged() {
        save().doOnComplete {
            Timber.d("Successfully saved!")
        }.subscribe()
    }

    private fun save(): Completable {
        val playerMap : MutableMap<Long, Int> = mutableMapOf()
        adapterDataList.forEach {
            playerMap[it.fieldPositionID] = it.order
        }
        return viewModel.saveNewBattingOrder(playerMap)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
    }
}