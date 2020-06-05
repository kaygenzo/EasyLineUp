package com.telen.easylineup.team.createTeam.teamType

import android.view.View
import androidx.viewpager.widget.ViewPager

class ShadowTransformer(val viewPager: ViewPager, val adapter: CardPagerAdapter): ViewPager.OnPageChangeListener, ViewPager.PageTransformer {

//    private var mLastOffset: Float = 0f
//    private var mScalingEnabled: Boolean = false
//
//    fun enableScaling(enable: Boolean) {
//        val currentCard = adapter.getCardViewAt(viewPager.currentItem)
//        if (mScalingEnabled && !enable) {
//            // shrink main card
//            currentCard?.let {
//                it.animate().scaleY(1f)
//                it.animate().scaleX(1f)
//            }
//        } else if (!mScalingEnabled && enable) {
//            // grow main card
//            currentCard?.let {
//                it.animate().scaleY(1.1f)
//                it.animate().scaleX(1.1f)
//            }
//        }
//
//        mScalingEnabled = enable
//    }
//
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
//        val realCurrentPosition: Int
//        val nextPosition: Int
//        val baseElevation = adapter.baseElevation
//        val realOffset: Float
//        val goingLeft = mLastOffset > positionOffset
//
//        // If we're going backwards, onPageScrolled receives the last position
//        // instead of the current one
//        if (goingLeft) {
//            realCurrentPosition = position + 1
//            nextPosition = position
//            realOffset = 1 - positionOffset
//        } else {
//            nextPosition = position + 1
//            realCurrentPosition = position
//            realOffset = positionOffset
//        }
//
//        // Avoid crash on overscroll
//        if (nextPosition > adapter.count - 1 || realCurrentPosition > adapter.count - 1) {
//            return
//        }
//
//        val currentCard = adapter.getCardViewAt(realCurrentPosition)
//
//        // This might be null if a fragment is being used
//        // and the views weren't created yet
//        currentCard?.let {
//            if (mScalingEnabled) {
//                it.setScaleX((1 + 0.1 * (1 - realOffset)).toFloat())
//                it.setScaleY((1 + 0.1 * (1 - realOffset)).toFloat())
//            }
//            it.cardElevation = baseElevation + (baseElevation * (MAX_ELEVATION_FACTOR - 1) * (1 - realOffset))
//        }
//
//        val nextCard = adapter.getCardViewAt(nextPosition)
//
//        // We might be scrolling fast enough so that the next (or previous) card
//        // was already destroyed or a fragment might not have been created yet
//        nextCard?.let {
//            if (mScalingEnabled) {
//                it.scaleX = (1 + 0.1 * realOffset).toFloat()
//                it.scaleY = (1 + 0.1 * realOffset).toFloat()
//            }
//            it.cardElevation = baseElevation + (baseElevation * (MAX_ELEVATION_FACTOR - 1) * realOffset)
//        }
//
//        mLastOffset = positionOffset
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageSelected(position: Int) {
    }

    override fun transformPage(page: View, position: Float) {
    }
}