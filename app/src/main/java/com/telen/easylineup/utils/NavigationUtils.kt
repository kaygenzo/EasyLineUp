/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import androidx.navigation.NavOptions
import androidx.navigation.navOptions
import com.telen.easylineup.R

class NavigationUtils {
    fun getOptions(): NavOptions {
        return navOptions {
            anim {
                enter = R.anim.slide_in_right
                exit = R.anim.slide_out_left
                popEnter = R.anim.slide_in_left
                popExit = R.anim.slide_out_right
            }
        }
    }

    fun getOptionsWithPopDestination(popDestination: Int, isInclusive: Boolean): NavOptions {
        return navOptions {
            anim {
                enter = R.anim.slide_in_right
                exit = R.anim.slide_out_left
                popEnter = R.anim.slide_in_left
                popExit = R.anim.slide_out_right
            }

            popUpTo(popDestination) {
                inclusive = isInclusive
            }
        }
    }
}
