package com.telen.easylineup.team.createTeam.teamType

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.R

data class TeamTypeCardItem(@StringRes val title: Int, @DrawableRes val resourceId: Int, @DrawableRes val representationId: Int)

class CardPagerAdapter(private val mData: MutableList<TeamTypeCardItem> = mutableListOf()): RecyclerView.Adapter<CardPagerAdapter.CardViewHolder>() {

    data class CardViewHolder(private val view: View):  RecyclerView.ViewHolder(view) {

        private val teamTypeTitle = view.findViewById<TextView>(R.id.teamTypeTitle)
        private val teamTypeImage = view.findViewById<ImageView>(R.id.teamTypeImage)
        private val teamTypeRepresentation = view.findViewById<ImageView>(R.id.teamTypeRepresentation)

        internal fun bind(item: TeamTypeCardItem) {
            teamTypeTitle.setText(item.title)
            teamTypeImage.setImageResource(item.resourceId)
            teamTypeRepresentation.setImageResource(item.representationId)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card_team_type, parent, false)
        return CardViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(mData[position])
    }

}