package com.telen.easylineup.views

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
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
        Picasso.get().load(image)
                .placeholder(R.drawable.unknown_team)
                .error(R.drawable.unknown_team)
                .into(imageView)
    }

    fun setTitle(title: String) {
        titleView.text = title
    }
}