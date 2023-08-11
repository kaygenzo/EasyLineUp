package com.telen.easylineup.views

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Picasso
import com.telen.easylineup.R
import com.telen.easylineup.databinding.NavDrawerHeaderBinding
import com.telen.easylineup.utils.ready
import timber.log.Timber

class DrawerHeader(context: Context) : ConstraintLayout(context) {

    val binding = NavDrawerHeaderBinding.inflate(LayoutInflater.from(context), this, true)

    fun setImage(image: String?) {
        binding.drawerImage.ready {
            try {
                Picasso.get().load(image)
                    .resize(binding.drawerImage.width, binding.drawerImage.height)
                    .centerCrop()
                    .transform(
                        RoundedTransformationBuilder()
                            .borderColor(Color.BLACK)
                            .borderWidthDp(2f)
                            .cornerRadiusDp(16f)
                            .oval(true)
                            .build()
                    )
                    .placeholder(R.drawable.ic_unknown_team)
                    .error(R.drawable.ic_unknown_team)
                    .into(binding.drawerImage)
            } catch (e: IllegalArgumentException) {
                Timber.e(e)
            }
        }
    }

    fun setTitle(title: String) {
        binding.drawerTitle.text = title
    }

    fun setOnImageClickListener(l: OnClickListener?) {
        binding.drawerImage.setOnClickListener(l)
    }

    fun setOnSwapTeamClickListener(l: OnClickListener?) {
        binding.changeTeam.setOnClickListener(l)
    }
}