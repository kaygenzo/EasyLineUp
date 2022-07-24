package com.telen.easylineup.team.createTeam.teamType

import android.os.Build
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.telen.easylineup.views.TeamCardView

class CardPagerAdapter(private val mData: MutableList<TeamTypeCardItem> = mutableListOf()) :
    RecyclerView.Adapter<CardPagerAdapter.CardViewHolder>() {

    data class CardViewHolder(private val view: TeamCardView) : RecyclerView.ViewHolder(view) {
        fun bind(item: TeamTypeCardItem) {
            view.setTeamName(view.context.getString(item.title))
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                view.setImage(item.ballResourceId)
            } else {
                view.setImage(item.compatBallResourceId)
            }
            view.setTeamType(item.type)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = TeamCardView(parent.context)
        view.setDragEnabled(false)
        view.setDragState(BottomSheetBehavior.STATE_COLLAPSED)
        return CardViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(mData[position])
    }
}