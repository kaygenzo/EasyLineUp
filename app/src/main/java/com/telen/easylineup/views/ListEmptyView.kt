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
import com.telen.easylineup.databinding.ListEmptyViewBinding

/**
 * A common reusable view that shows a hint image and text for an empty list view.
 */
class ListEmptyView : LinearLayout {

    private val binding = ListEmptyViewBinding.inflate(LayoutInflater.from(context), this, true)

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun setImageHint(resId: Int) {
        binding.mEmptyImageHint?.setImageResource(resId)
    }

    fun setTextHint(resId: Int) {
        binding.mEmptyTextHint?.text = resources.getText(resId)
    }

    fun setTextHint(hintText: CharSequence) {
        binding.mEmptyTextHint?.text = hintText
    }

    fun setIsImageVisible(isImageVisible: Boolean) {
        binding.mEmptyImageHint?.visibility = if (isImageVisible) View.VISIBLE else View.GONE
    }

    fun setIsVerticallyCentered(isVerticallyCentered: Boolean) {
        val gravity =
            if (isVerticallyCentered) Gravity.CENTER else Gravity.TOP or Gravity.CENTER_HORIZONTAL
        (binding.mEmptyImageHint?.layoutParams as LayoutParams).gravity = gravity
        (binding.mEmptyTextHint?.layoutParams as LayoutParams).gravity = gravity
        layoutParams.height =
            if (isVerticallyCentered) LayoutParams.WRAP_CONTENT else LayoutParams.MATCH_PARENT
        requestLayout()
    }
}
