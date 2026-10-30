/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup

import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import com.telen.easylineup.lineup.attack.AttackFragment
import com.telen.easylineup.lineup.defense.DefenseFragment

const val FRAGMENT_DEFENSE_INDEX = 0
const val FRAGMENT_ATTACK_INDEX = 1

class LineupPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    val map: MutableMap<Int, View> = mutableMapOf()

    override fun createFragment(position: Int): Fragment {
        val result = when (position) {
            FRAGMENT_DEFENSE_INDEX -> DefenseFragment()

            FRAGMENT_ATTACK_INDEX -> AttackFragment()

            else -> Fragment()
        }

        return result
    }

    override fun getItemCount(): Int {
        return 2
    }

    override fun onBindViewHolder(
        holder: FragmentViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)
        map[position] = holder.itemView
    }
}
