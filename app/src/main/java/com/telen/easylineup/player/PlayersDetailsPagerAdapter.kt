package com.telen.easylineup.player

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.repository.model.Player

class PlayersDetailsPagerAdapter(fm: FragmentManager, private var playersIds: MutableList<Long> = mutableListOf()): FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    fun setPlayerIDs(players: List<Player>) {
        this.playersIds.clear()
        this.playersIds.addAll(players.map { it.id })
        notifyDataSetChanged()
    }

    fun getPlayerIndex(playerID: Long): Int {
        return if(playersIds.indexOf(playerID) >= 0) {
            playersIds.indexOf(playerID)
        } else  0 // not supposed to happen...
    }

    fun getPlayerID(index: Int): Long {
        return playersIds[index]
    }

    fun getPlayersSize(): Int {
        return playersIds.size
    }

    override fun getItem(position: Int): Fragment {
        val fragment = PlayerDetailsFragment()
        val extras = Bundle()
        extras.putSerializable(Constants.PLAYER_ID, playersIds[position])
        fragment.arguments = extras
        return fragment
    }

    override fun getCount(): Int {
       return playersIds.size
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }
}