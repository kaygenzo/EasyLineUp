package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import com.telen.easylineup.R
import com.telen.easylineup.repository.model.Lineup
import com.telen.easylineup.lineup.list.LineupsAdapter
import com.telen.easylineup.lineup.list.OnItemClickedListener
import kotlinx.android.synthetic.main.item_adapter_tournaments.view.*

class TournamentItemView : ConstraintLayout {

    lateinit var lineupsAdapter: LineupsAdapter
    private val lineups = mutableListOf<Lineup>()

    constructor(context: Context, itemClickedListener: OnItemClickedListener?) : super(context) {initView(context, itemClickedListener)}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {initView(context, null)}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {initView(context, null)}

    private fun initView(context: Context, itemClickedListener: OnItemClickedListener?) {
        LayoutInflater.from(context).inflate(R.layout.item_adapter_tournaments, this)
        lineupsAdapter = LineupsAdapter(lineups, itemClickedListener)

        val layoutMgr = GridLayoutManager(context, 1)
        val dividerItemDecoration = DividerItemDecoration(context, layoutMgr.orientation)

        lineupsOfTournamentRecycler.apply {
            layoutManager = layoutMgr
            addItemDecoration(dividerItemDecoration)
            adapter = lineupsAdapter
        }
    }

    fun setLineups(lineups: List<Lineup>) {
        this.lineups.clear()
        this.lineups.addAll(lineups)
        this.lineupsAdapter.notifyDataSetChanged()
    }

    fun setTimeLinePosition(position: Int) {
        material_timeline_view.position = position
    }
}