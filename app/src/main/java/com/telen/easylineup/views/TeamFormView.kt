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
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.telen.easylineup.R
import com.telen.easylineup.databinding.ViewCreateTeamBinding
import com.telen.easylineup.utils.ready
import timber.log.Timber

interface TeamFormListener {
    fun onNameChanged(name: String)
    fun onImageChanged(imageUri: Uri?)
    fun onImagePickerRequested()
}

class TeamFormView : ConstraintLayout {

    private var listener: TeamFormListener? = null
    private var imageUri: Uri? = null

    val binding: ViewCreateTeamBinding =
        ViewCreateTeamBinding.inflate(LayoutInflater.from(context), this, true)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        Picasso.get().load(R.drawable.ic_unknown_team)
            .fit()
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
            .into(binding.teamImage)

        binding.teamImage.setOnClickListener {
            listener?.onImagePickerRequested()
        }

        binding.teamNameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                listener?.onNameChanged(s.toString())
            }
        })

        binding.teamImageAction.run {
            setImageResource(R.drawable.add_image)
            setOnClickListener(null)
        }
    }

    fun setListener(listener: TeamFormListener) {
        this.listener = listener
    }

    fun onImageUriReceived(imageUri: Uri) {
        setImage(imageUri)
        this.listener?.onImageChanged(imageUri)
    }

    fun getName(): String {
        return binding.teamNameInput.text.toString()
    }

    fun getImageUri(): Uri? {
        return imageUri
    }

    fun setName(name: String) {
        binding.teamNameInput.setText(name)
    }

    fun setTeamImage(@DrawableRes resId: Int) {
        Picasso.get().load(resId)
            .placeholder(R.drawable.ic_unknown_team)
            .error(R.drawable.ic_unknown_team)
            .into(binding.teamImage)
    }

    fun setImage(uri: Uri) {
        imageUri = uri
        binding.teamImage.ready {
            try {
                Picasso.get().load(uri)
                    .resize(binding.teamImage.width, binding.teamImage.height)
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
                    .into(binding.teamImage, object : Callback {
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
            binding.teamImageAction.run {
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
        binding.teamNameInputLayout.error =
            resources.getString(R.string.team_creation_error_name_empty)
    }
}