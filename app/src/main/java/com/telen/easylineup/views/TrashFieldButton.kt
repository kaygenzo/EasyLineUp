package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.R
import kotlinx.android.synthetic.main.view_add_player_button.view.*

class TrashFieldButton: ConstraintLayout {

    constructor(context: Context) : super(context) { init(context) }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) { init(context) }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init(context) }

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.view_trash_field_button, this)
    }

    fun setScaleType(scaleType: ImageView.ScaleType) {
        fab.scaleType = scaleType
    }

    override fun setOnClickListener(l: OnClickListener?) {
        fab.setOnClickListener(l)
    }
}