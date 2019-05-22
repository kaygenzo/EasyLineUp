package com.telen.easylineup.lineup.list

import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.R
import com.telen.easylineup.data.Lineup
import com.telen.easylineup.views.CollegedStyledTextView
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection

interface OnItemClickedListener {
    fun onHeaderClicked()
    fun onLineupClicked(lineup: Lineup)
}

class CategorizedLineupAdapter(val lineups: List<Lineup>, val tournamentName: String, val onItemClickedListener: OnItemClickedListener): StatelessSection(
        SectionParameters.builder()
                .itemResourceId(R.layout.categorized_lineup_item)
                .headerResourceId(R.layout.header_section_tournaments)
                .build()
) {
    private var isExpanded = true

    override fun getContentItemsTotal(): Int {
        return if(isExpanded)
            lineups.size
        else
            0
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val cardHolder = holder as LineupViewHolder
        val lineup = lineups[position]
        cardHolder.field.setSmallPlayerPosition(lineup.playerFieldPosition)
        cardHolder.lineupName.text = lineup.name
        cardHolder.tournamentName.visibility = View.GONE
        cardHolder.rootView.setOnClickListener {
            onItemClickedListener.onLineupClicked(lineup)
        }
    }

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder {
        return LineupViewHolder(view)
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder?) {
        val sectionHolder = holder as HeaderViewHolder
        sectionHolder.title.text = tournamentName

        updateArrow(sectionHolder)

        sectionHolder.rootView.setOnClickListener {
            isExpanded = !isExpanded
            updateArrow(sectionHolder)
            onItemClickedListener.onHeaderClicked()
        }
    }

    private fun updateArrow(header: HeaderViewHolder) {
        header.arrow.setImageResource(
                when(isExpanded) {
                    true -> R.drawable.ic_keyboard_arrow_down_black_24dp
                    false -> R.drawable.ic_keyboard_arrow_up_black_24dp
                }
        )
    }

    override fun getHeaderViewHolder(view: View): RecyclerView.ViewHolder {
        return HeaderViewHolder(view)
    }

    inner class HeaderViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val title = view.findViewById<CollegedStyledTextView>(R.id.sectionHeaderTitle)
        val arrow = view.findViewById<ImageView>(R.id.sectionHeaderArrow)
        val rootView = view.findViewById<ConstraintLayout>(R.id.headerRootView)
    }

}