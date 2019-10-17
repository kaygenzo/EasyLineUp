package com.telen.easylineup.team.createTeam

import androidx.cardview.widget.CardView
import androidx.viewpager.widget.PagerAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.telen.easylineup.R

const val MAX_ELEVATION_FACTOR = 8

data class TeamTypeCardItem(@StringRes val title: Int, @DrawableRes val resourceId: Int)

class CardPagerAdapter : PagerAdapter() {

    val mData: MutableList<TeamTypeCardItem> = mutableListOf()
    val mViews: MutableList<CardView?> = mutableListOf()

    var baseElevation: Float = 0.toFloat()
        private set

    fun addCardItem(item: TeamTypeCardItem) {
        mViews.add(null)
        mData.add(item)
    }

    fun getCardViewAt(position: Int): CardView? {
        return mViews[position]
    }

    override fun getCount(): Int {
        return mData.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(container.context).inflate(R.layout.item_card_team_type, container, false)
        container.addView(view)
        bind(mData[position], view)
        val cardView = view.findViewById(R.id.cardView) as CardView

        if (baseElevation == 0f) {
            baseElevation = cardView.cardElevation
        }

        cardView.maxCardElevation = baseElevation * MAX_ELEVATION_FACTOR
        mViews[position] = cardView
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
        mViews[position] = null
    }

    private fun bind(item: TeamTypeCardItem, view: View) {
        val teamTypeTitle = view.findViewById<View>(R.id.teamTypeTitle) as TextView
        val teamTypeImage = view.findViewById<View>(R.id.teamTypeImage) as ImageView
        teamTypeTitle.setText(item.title)
        teamTypeImage.setImageResource(item.resourceId)
    }

}