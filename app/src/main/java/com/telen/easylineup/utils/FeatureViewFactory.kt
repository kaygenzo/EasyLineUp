package com.telen.easylineup.utils

import android.graphics.Typeface
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.telen.easylineup.R

class FeatureViewFactory {
    companion object {
        fun apply(view: View, activity: AppCompatActivity, title: String, description: String, listener: TapTargetView.Listener) {
            TapTargetView.showFor(activity,
                    TapTarget.forView(view, title, description)
                            // All options below are optional
                            .outerCircleColor(R.color.baseball_field_color)      // Specify a color for the outer circle
                            .outerCircleAlpha(0.93f)            // Specify the alpha amount for the outer circle
                            .targetCircleColor(R.color.infield)   // Specify a color for the target circle
                            .titleTextDimen(R.dimen.app_max_text_size_biggest)                  // Specify the size (in sp) of the title text
                            .titleTypeface(Typeface.DEFAULT_BOLD)
                            .descriptionTextDimen(R.dimen.app_max_text_size_big)            // Specify the size (in sp) of the description text
                            .descriptionTypeface(Typeface.SANS_SERIF)
                            .textColor(R.color.white)            // Specify a color for both the title and description text
                            .dimColor(R.color.blue_grey_80)            // If set, will dim behind the view with 30% opacity of the given color
                            .drawShadow(true)                   // Whether to draw a drop shadow or not
                            .cancelable(true)                  // Whether tapping outside the outer circle dismisses the view
                            .tintTarget(false)                   // Whether to tint the target view's color
                            .transparentTarget(false)           // Specify whether the target is transparent (displays the content underneath)
                            .targetRadius(50), listener)
        }
    }
}