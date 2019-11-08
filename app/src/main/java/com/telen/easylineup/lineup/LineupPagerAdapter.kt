package com.telen.easylineup.lineup

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.telen.easylineup.R
import com.telen.easylineup.lineup.defense.DefenseFragmentFixed
import com.telen.easylineup.lineup.defense.DefenseFragmentEditable
import com.telen.easylineup.lineup.attack.AttackFragment
import com.telen.easylineup.utils.Constants

const val FRAGMENT_DEFENSE_INDEX = 0
const val FRAGMENT_ATTACK_INDEX = 1

class LineupPagerAdapter(val context: Context, fm: FragmentManager, private val editable: Boolean): FragmentPagerAdapter(fm) {

    private val map = mutableMapOf<Int, Fragment>()

    override fun getItem(position: Int): Fragment {
        val fragment =  when(position) {
            FRAGMENT_DEFENSE_INDEX -> {
                if(editable)
                    DefenseFragmentEditable()
                else
                    DefenseFragmentFixed()
            }
            FRAGMENT_ATTACK_INDEX -> {
                val arguments = Bundle()
                arguments.putBoolean(Constants.EXTRA_EDITABLE, editable)
                val fragment = AttackFragment()
                fragment.arguments = arguments
                fragment
            }
            else -> Fragment()
        }
        map[position] = fragment
        return fragment
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

    fun getMapFragment() : MutableMap<Int, Fragment> {
        return map
    }
}