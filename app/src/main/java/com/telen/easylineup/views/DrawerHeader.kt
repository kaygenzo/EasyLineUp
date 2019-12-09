package com.telen.easylineup.views

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Picasso
import com.telen.easylineup.R
import kotlinx.android.synthetic.main.nav_drawer_header.view.*

class DrawerHeader(context: Context): ConstraintLayout(context) {

    init {
        LayoutInflater.from(context).inflate(R.layout.nav_drawer_header, this)
    }

    fun setImage(image: String?) {
        drawerImage.post {
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