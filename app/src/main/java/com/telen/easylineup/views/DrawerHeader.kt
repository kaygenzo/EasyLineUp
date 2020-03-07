package com.telen.easylineup.views

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Picasso
import com.telen.easylineup.R
import com.telen.easylineup.utils.ready
import kotlinx.android.synthetic.main.nav_drawer_header.view.*
import timber.log.Timber

class DrawerHeader(context: Context): ConstraintLayout(context) {

    init {
        LayoutInflater.from(context).inflate(R.layout.nav_drawer_header, this)
    }

    fun setImage(image: String?) {
        drawerImage.ready {
            try {
                Picasso.get().load(image)
                        .resize(drawerImage.width, drawerImage.height)
                        .centerCrop()
                        .transform(RoundedTransformationBuilder()
                                .borderColor(Color.BLACK)
                                .borderWidthDp(2f)
                                .cornerRadiusDp(16f)
                                .oval(true)
                                .build())
                        .placeholder(R.drawable.ic_unknown_team)
                        .error(R.drawable.ic_unknown_team)
                        .into(drawerImage)
            } catch (e: IllegalArgumentException) {
                Timber.e(e)
            }
        }
    }

    fun setTitle(title: String) {
        drawerTitle.text = title
    }

    fun setOnImageClickListener(l: OnClickListener?) {
        drawerImage.setOnClickListener(l)
    }

    fun setOnSwapTeamClickListener(l: OnClickListener?) {
        changeTeam.setOnClickListener(l)
    }
}