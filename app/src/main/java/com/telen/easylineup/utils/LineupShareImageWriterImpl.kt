/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import com.telen.easylineup.lineup.FRAGMENT_ATTACK_INDEX
import com.telen.easylineup.lineup.FRAGMENT_DEFENSE_INDEX
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar

class LineupShareImageWriterImpl(private val context: Context) : LineupShareImageWriter {
    override fun write(views: Map<Int, Bitmap?>): List<Uri> {
        val tmpPath = context.cacheDir.path
        val timeInMillis = Calendar.getInstance().timeInMillis
        val authority = context.packageName + ".fileprovider"
        val uris = mutableListOf<Uri>()

        views.forEach { entry ->
            val pathBuilder = StringBuilder(tmpPath)
                .append("/")
                .append(timeInMillis)
                .append("_")
            when (entry.key) {
                FRAGMENT_DEFENSE_INDEX -> pathBuilder.append("defense.png")
                FRAGMENT_ATTACK_INDEX -> pathBuilder.append("attack.png")
                else -> pathBuilder.append("all.png")
            }
            val filePath = pathBuilder.toString()
            val uri = FileProvider.getUriForFile(context, authority, File(filePath))
            try {
                FileOutputStream(filePath).use { stream ->
                    entry.value?.let { bitmap ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        uris.add(uri)
                    }
                }
            } catch (e: IOException) {
                Timber.e(e)
            }
        }
        return uris
    }
}
