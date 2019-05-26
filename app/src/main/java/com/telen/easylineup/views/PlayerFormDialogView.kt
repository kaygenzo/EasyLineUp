package com.telen.easylineup.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.qingmei2.rximagepicker.core.RxImagePicker
import com.qingmei2.rximagepicker.entity.sources.Camera
import com.qingmei2.rximagepicker.entity.sources.Gallery
import com.qingmei2.rximagepicker.ui.ICustomPickerConfiguration
import com.qingmei2.rximagepicker_extension.MimeType
import com.qingmei2.rximagepicker_extension_zhihu.ZhihuConfigurationBuilder
import com.qingmei2.rximagepicker_extension_zhihu.ui.ZhihuImagePickerActivity
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.telen.easylineup.R
import com.telen.easylineup.utils.PicassoEngine
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.view_create_player.view.*

interface PlayerFormListener {
    fun onSaveClicked(name: String, shirtNumber: Int, licenseNumber: Long, imageUri: Uri?)
}

interface ImagePicker {

    @Gallery(componentClazz = ZhihuImagePickerActivity::class,
            openAsFragment = false)
    fun openGallery(context: Context,
                    config: ICustomPickerConfiguration): Observable<com.qingmei2.rximagepicker.entity.Result>

    @Camera
    fun openCamera(context: Context): Observable<com.qingmei2.rximagepicker.entity.Result>
}

class PlayerFormDialogView: ConstraintLayout {

    private var listener: PlayerFormListener? = null
    private var imageUri: Uri? = null

    constructor(context: Context?) : super(context) {initView(context)}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {initView(context)}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {initView(context)}

    fun setListener(listener: PlayerFormListener) {
        this.listener = listener
    }

    private fun initView(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.view_create_player, this)

        Picasso.get().load(R.drawable.pikachu).into(playerImage)

        playerImage.setOnClickListener {
            context?.let {
                RxImagePicker
                        .create(ImagePicker::class.java)
                        .openGallery(it, ZhihuConfigurationBuilder(MimeType.ofImage(), false)
                                .imageEngine(PicassoEngine())
                                .capture(true)
                                .maxSelectable(1)
                                .showSingleMediaType(true)
                                .countable(true)
                                .spanCount(3)
                                .theme(R.style.Zhihu_Dracula)
                                .build())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {result ->
                            val uri = result.uri
                            imageUri = uri
                            Picasso.get().load(uri).into(playerImage)
                        }
            }
        }

        playerSave.setOnClickListener {
            val name = getName()
            val shirtNumber = getShirtNumber()
            val licenseNumber = getLicenseNumber()
            if(name!=null && shirtNumber!=null && licenseNumber!=null) {
                listener?.onSaveClicked(name, shirtNumber, licenseNumber, imageUri)
            }
        }
    }

    private fun getName(): String? {
        val name = playerNameInput.text.toString()
        return if(TextUtils.isEmpty(name))
            null
        else
            name
    }

    private fun getShirtNumber(): Int? {
        return try {
            playerShirtNumberInput.text.toString().toInt()
        }
        catch (exception: NumberFormatException) {
            null
        }
    }

    private fun getLicenseNumber(): Long? {
        return try {
            playerShirtNumberInput.text.toString().toLong()
        }
        catch (exception: NumberFormatException) {
            null
        }
    }

//    private fun getRoundedBitmap(bitmap: Bitmap): RoundedBitmapDrawable {
//        val min = Math.min(bitmap.width, bitmap.height)
//        val drawable = RoundedBitmapDrawableFactory.create(resources, Bitmap.createBitmap(bitmap, 0,0,min, min))
//        drawable.isCircular = true
//        return drawable
//    }
}