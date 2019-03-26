package com.telen.easylineup.battingOrder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.R
import com.telen.easylineup.data.Player
import kotlinx.android.synthetic.main.batting_order_list.view.*

class BattingOrderFragment: Fragment() {

    private lateinit var playerAdapter: BattingOrderAdapter
    private lateinit var players: MutableList<Player>
    private lateinit var viewModel: BattingOrderViewModel
    private lateinit var itemTouchedCallback: BattingOrderItemTouchCallback
    private lateinit var itemTouchedHelper: ItemTouchHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        players = mutableListOf()
        playerAdapter = BattingOrderAdapter(players)
        itemTouchedCallback = BattingOrderItemTouchCallback(playerAdapter)
        itemTouchedHelper = ItemTouchHelper(itemTouchedCallback)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.batting_order_list, container, false)

        view.battingOrder.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = playerAdapter
        }

        itemTouchedHelper.attachToRecyclerView(view.battingOrder)

        viewModel = ViewModelProviders.of(this).get(BattingOrderViewModel::class.java)
        viewModel.team.observe(this, Observer {team ->
            players.apply {
                clear()
                addAll(team.players)
            }
            playerAdapter.notifyDataSetChanged()
        })

        return view
    }
}

class BattingOrderItemTouchCallback(val adapter: BattingOrderAdapter): ItemTouchHelper.Callback() {
    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return ItemTouchHelper.Callback.makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        adapter.onMoved(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }



}