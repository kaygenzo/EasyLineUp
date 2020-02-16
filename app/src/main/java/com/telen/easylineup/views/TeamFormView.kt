package com.telen.easylineup.views

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.nguyenhoanglam.imagepicker.model.Image
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.telen.easylineup.R
import kotlinx.android.synthetic.main.view_create_team.view.*
import timber.log.Timber

interface TeamFormListener {
    fun onNameChanged(name: String)
    fun onImageChanged(imageUri: Uri?)
    fun onImagePickerRequested()
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

        Picasso.get().load(R.drawable.ic_unknown_team)
                .fit()
                .transform(RoundedTransformationBuilder()
                        .borderColor(Color.BLACK)
                        .borderWidthDp(2f)
                        .cornerRadiusDp(16f)
                        .oval(true)
                        .build())
                .placeholder(R.drawable.ic_unknown_team)
                .error(R.drawable.ic_unknown_team)
                .into(teamImage)

        teamImage.setOnClickListener {
            listener?.onImagePickerRequested()
        }

        teamNameInput.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                listener?.onNameChanged(s.toString())
            }
        })

        teamImageAction.run {
            setImageResource(R.drawable.add_image)
            setOnClickListener(null)
        }
    }

    fun onImageUriReceived(image: Image) {
        val filePathUri = android.content.ContentResolver.SCHEME_FILE + ":///" + image.path
        setImage(filePathUri)
        this.listener?.onImageChanged(Uri.parse(filePathUri))
    }

    fun getName(): String {
        return teamNameInput.text.toString()
    }

    fun getImageUri(): Uri? {
        return imageUri
    }

    fun setName(name: String) {
        teamNameInput.setText(name)
    }

    fun setTeamImage(@DrawableRes resId: Int) {
        Picasso.get().load(resId)
                .placeholder(R.drawable.ic_unknown_team)
                .error(R.drawable.ic_unknown_team)
                .into(teamImage)
    }

    fun setImage(path: String) {
        imageUri = Uri.parse(path)
        teamImage.post {
            try {
                Picasso.get().load(path)
                        .resize(teamImage.width, teamImage.height)
                        .centerCrop()
                        .transform(RoundedTransformationBuilder()
                                .borderColor(Color.BLACK)
                                .borderWidthDp(2f)
                                .cornerRadiusDp(16f)
                                .oval(true)
                                .build())
                        .placeholder(R.drawable.ic_unknown_team)
                        .error(R.drawable.ic_unknown_team)
                        .into(teamImage,object: Callback {
                            override fun onSuccess() {
                                Timber.e("Successfully loaded image")
                            }

                            override fun onError(e: Exception?) {
                                Timber.e(e)
                            }

                        })
            } catch (e: IllegalArgumentException) {
                Timber.e(e)
            }
        }

        imageUri?.let {
            teamImageAction.run {
                setImageResource(R.drawable.remove_image)
                setOnClickListener {
                    imageUri = null
                    setTeamImage(R.drawable.ic_unknown_team)
                    setImageResource(R.drawable.add_image)
                    setOnClickListener(null)
                    listener?.onImageChanged(null)
                }
            }
        }
    }

    fun displayInvalidName() {
        teamNameInputLayout.error = resources.getString(R.string.team_creation_error_name_empty)
    }
}