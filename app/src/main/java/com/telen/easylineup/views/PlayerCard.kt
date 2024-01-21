/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.views

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.squareup.picasso.Picasso
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.Player
import timber.log.Timber

abstract class PlayerCard(context: Context) : ConstraintLayout(context) {
    abstract fun getCardRootView(): View
    abstract fun bind(player: Player)

    fun setImage(image: ImageView, path: String?) {
        image.post {
            // I put this test here because untilReady is too long to complete so the adapter
            // inflate too late the image. This causes the images to be at the wrong place in the
            // recycler
            if (image.width > 0 && image.height > 0) {
                try {
                    Picasso.get()
                        .load(path)
                        .resize(image.width, image.height)
                        .centerCrop()
                        .error(R.drawable.ic_unknown_field_player)
                        .placeholder(R.drawable.ic_unknown_field_player)
                        .into(image)
                } catch (e: IllegalArgumentException) {
                    Timber.e(e)
                }
            } else {
                Picasso.get()
                    .load(R.drawable.ic_unknown_field_player)
                    .error(R.drawable.ic_unknown_field_player)
                    .placeholder(R.drawable.ic_unknown_field_player)
                    .into(image)
            }
        }
    }

    protected fun setName(textView: AppCompatTextView, name: String) {
        textView.text = name.trim()
    }

    protected fun setShirtNumber(textView: AppCompatTextView, shirtNumber: Int) {
        textView.text = shirtNumber.toString()
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        getCardRootView().setOnClickListener(listener)
    }
}
