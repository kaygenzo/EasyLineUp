package com.telen.easylineup.player

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.repository.model.TrainingCard

class PlayersDetailsPagerAdapter(private val playersIds: List<Long>, fm: FragmentManager): FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

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