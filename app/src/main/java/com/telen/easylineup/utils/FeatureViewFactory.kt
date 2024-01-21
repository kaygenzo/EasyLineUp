/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import android.graphics.Typeface
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.telen.easylineup.R

class FeatureViewFactory {
    companion object {
        fun apply(
            view: View,
            activity: AppCompatActivity,
            title: String,
            description: String,
            listener: TapTargetView.Listener
        ) {
            apply(TapTarget.forView(view, title, description), activity, listener)
        }

        fun apply(
            toolbar: Toolbar,
            @IdRes itemId: Int,
            activity: AppCompatActivity,
            title: String,
            description: String,
            listener: TapTargetView.Listener
        ) {
            apply(
                TapTarget.forToolbarMenuItem(toolbar, itemId, title, description),
                activity,
                listener
            )
        }

        private fun apply(
            target: TapTarget,
            activity: AppCompatActivity,
            listener: TapTargetView.Listener
        ) {
            TapTargetView.showFor(
                activity,
                target
                    // All options below are optional

                    // Specify a color for the outer circle
                    .outerCircleColor(R.color.baseball_field_color)
                    .outerCircleAlpha(0.93f) // Specify the alpha amount for the outer circle
                    .targetCircleColor(R.color.infield) // Specify a color for the target circle
                    .titleTextDimen(R.dimen.app_max_text_size_biggest) // Specify the size (in sp) of the title text
                    .titleTypeface(Typeface.DEFAULT_BOLD)
                    // Specify the size (in sp) of the description text
                    .descriptionTextDimen(R.dimen.app_max_text_size_big)
                    .descriptionTypeface(Typeface.SANS_SERIF)
                    // Specify a color for both the title and description text
                    .textColor(R.color.white)
                    // If set, will dim behind the view with 30% opacity of the given color
                    .dimColor(R.color.grey)
                    .drawShadow(true) // Whether to draw a drop shadow or not
                    // Whether tapping outside the outer circle dismisses the view
                    .cancelable(true)
                    .tintTarget(false) // Whether to tint the target view's color
                    // Specify whether the target is transparent (displays the content underneath)
                    .transparentTarget(false)
                    .targetRadius(50), listener
            )
        }
    }
}
