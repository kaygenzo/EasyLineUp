package com.telen.easylineup.player

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Player

class PlayersDetailsPagerAdapter(
    val fragment: Fragment,
    private val playersIds: MutableList<Long> = mutableListOf()
) : FragmentStateAdapter(fragment) {

    fun setPlayerIDs(players: List<Player>) {
        this.playersIds.clear()
        this.playersIds.addAll(players.map { it.id })
        notifyDataSetChanged()
    }

    fun getPlayerIndex(playerID: Long): Int {
        return playersIds.indexOf(playerID).takeIf { it >= 0 } ?: 0 // not supposed to happen...
    }

    fun getPlayerID(index: Int): Long {
        return playersIds[index]
    }

    fun getPlayersSize(): Int {
        return playersIds.size
    }

    override fun getItemCount(): Int {
        return playersIds.size
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = PlayerDetailsFragment()
        val extras = Bundle()
        extras.putSerializable(Constants.PLAYER_ID, playersIds[position])
        fragment.arguments = extras
        return fragment
    }
}