package com.telen.easylineup.views

import android.content.Context
import android.net.Uri
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.qingmei2.rximagepicker.core.RxImagePicker
import com.qingmei2.rximagepicker_extension.MimeType
import com.qingmei2.rximagepicker_extension_zhihu.ZhihuConfigurationBuilder
import com.squareup.picasso.Picasso
import com.telen.easylineup.R
import com.telen.easylineup.utils.PicassoEngine
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.view_create_team.view.*

interface TeamFormListener {
    fun onNameChanged(name: String)
    fun onImageChanged(imageUri: Uri)
}

class TeamFormView: ConstraintLayout {

    private var listener: TeamFormListener? = null
    private var imageUri: Uri? = null

    constructor(context: Context?) : super(context) {initView(context)}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {initView(context)}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {initView(context)}

    fun setListener(listener: TeamFormListener) {
        this.listener = listener
    }

    private fun initView(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.view_create_team, this)

        Picasso.get().load(R.drawable.pikachu).into(teamImage)

        teamImage.setOnClickListener {
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
                            setImage(uri)
                            this.listener?.onImageChanged(uri)
                        }
            }
        }

        teamNameInput.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                listener?.onNameChanged(s.toString())
            }

        })
    }

    fun getName(): String? {
        val name = teamNameInput.text.toString()
        return if(TextUtils.isEmpty(name))
            null
        else
            name
    }

    fun getImageUri(): Uri? {
        return imageUri
    }

    fun setName(name: String) {
        teamNameInput.setText(name)
    }

    fun setImage(uri: Uri) {
        imageUri = uri
        Picasso.get().load(uri).into(teamImage)
    }
}