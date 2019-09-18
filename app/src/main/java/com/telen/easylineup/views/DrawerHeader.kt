package com.telen.easylineup.views

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Picasso
import com.telen.easylineup.R

class DrawerHeader(context: Context): ConstraintLayout(context) {

    private var imageView: AppCompatImageView
    private var titleView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.nav_drawer_header, this)
        imageView = findViewById(R.id.drawerImage)
        titleView = findViewById(R.id.drawerTitle)
    }

    fun setImage(image: String?) {
        imageView.post {
            Picasso.get().load(image)
                    .resize(imageView.width, imageView.height)
                    .centerCrop()
                    .transform(RoundedTransformationBuilder()
                            .borderColor(Color.BLACK)
                            .borderWidthDp(2f)
                            .cornerRadiusDp(16f)
                            .oval(true)
                            .build())
                    .placeholder(R.drawable.ic_unknown_team)
                    .error(R.drawable.ic_unknown_team)
                    .into(imageView)
        }
    }

    fun setTitle(title: String) {
        titleView.text = title
    }

    override fun setOnClickListener(l: OnClickListener?) {
        imageView.setOnClickListener(l)
    }
}