package com.telen.easylineup.newLineup

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.telen.easylineup.R
import com.telen.easylineup.newLineup.defense.DefenseFragment
import com.telen.easylineup.newLineup.attack.BattingOrderFragment

const val FRAGMENT_DEFENSE_INDEX = 0
const val FRAGMENT_ATTACK_INDEX = 1

class LineUpPagerAdapter(val context: Context, fm: FragmentManager): FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return when(position) {
            FRAGMENT_DEFENSE_INDEX -> DefenseFragment()
            FRAGMENT_ATTACK_INDEX -> BattingOrderFragment()
            else -> Fragment()
        }
    }

    override fun getCount(): Int {
       return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position) {
            FRAGMENT_DEFENSE_INDEX -> context.getString(R.string.new_lineup_tab_field_defense)
            FRAGMENT_ATTACK_INDEX -> context.getString(R.string.new_lineup_tab_field_attack)
            else -> ""
        }
    }
}