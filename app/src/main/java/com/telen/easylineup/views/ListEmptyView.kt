package com.telen.easylineup.views

/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.telen.easylineup.R
import kotlinx.android.synthetic.main.list_empty_view.view.*

/**
 * A common reusable view that shows a hint image and text for an empty list view.
 */
class ListEmptyView: LinearLayout {

    constructor(context: Context) : super(context) { init(context) }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){ init(context) }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){ init(context) }

    fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.list_empty_view, this)
    }

    fun setImageHint(resId: Int) {
        mEmptyImageHint?.setImageResource(resId)
    }

    fun setTextHint(resId: Int) {
        mEmptyTextHint?.text = resources.getText(resId)
    }

    fun setTextHint(hintText: CharSequence) {
        mEmptyTextHint?.text = hintText
    }

    fun setIsImageVisible(isImageVisible: Boolean) {
        mEmptyImageHint?.visibility = if (isImageVisible) View.VISIBLE else View.GONE
    }

    fun setIsVerticallyCentered(isVerticallyCentered: Boolean) {
        val gravity = if (isVerticallyCentered) Gravity.CENTER else Gravity.TOP or Gravity.CENTER_HORIZONTAL
        (mEmptyImageHint?.layoutParams as LayoutParams).gravity = gravity
        (mEmptyTextHint?.layoutParams as LayoutParams).gravity = gravity
        layoutParams.height = if (isVerticallyCentered) LayoutParams.WRAP_CONTENT else LayoutParams.MATCH_PARENT
        requestLayout()
    }
}
