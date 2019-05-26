package com.telen.easylineup.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.qingmei2.rximagepicker_extension.engine.ImageEngine
import com.squareup.picasso.Picasso

class PicassoEngine: ImageEngine {
    override fun loadGifImage(context: Context, resizeX: Int, resizeY: Int, imageView: ImageView, uri: Uri) {
        Picasso.get()
                .load(uri)
                .centerCrop()
                .into(imageView)
//        Glide.with(context)
//                .asBitmap()     // some .jpeg files are actually gif
//                .load(uri)
//                .apply(RequestOptions.centerCropTransform().placeholder(placeholder))
//                .into(imageView)
    }

    override fun loadGifThumbnail(context: Context, resize: Int, placeholder: Drawable, imageView: ImageView, uri: Uri) {
        Picasso.get()
                .load(uri)
                .centerCrop()
                .into(imageView)
//        Glide.with(context)
//                .asBitmap()     // some .jpeg files are actually gif
//                .load(uri)
//                .apply(RequestOptions.centerCropTransform()
//                        .placeholder(placeholder))
//                .into(imageView)
    }

    override fun loadImage(context: Context, resizeX: Int, resizeY: Int, imageView: ImageView, uri: Uri) {
        Picasso.get()
                .load(uri)
                .priority(Picasso.Priority.HIGH)
                .into(imageView)
//        Glide.with(context)
//                .load(uri)
//                .apply(RequestOptions.priorityOf(Priority.HIGH))
//                .into(imageView)
    }

    override fun loadThumbnail(context: Context, resize: Int, placeholder: Drawable, imageView: ImageView, uri: Uri) {
        Picasso.get()
                .load(uri)
                .priority(Picasso.Priority.HIGH)
                .placeholder(placeholder)
                .resize(resize, resize)
                .centerCrop()
                .into(imageView)
//        Glide.with(context)
//                .asGif()
//                .load(uri)
//                .apply(RequestOptions.priorityOf(Priority.HIGH))
//                .into(imageView)
    }

    override fun supportAnimatedGif(): Boolean {
        return false
    }
}