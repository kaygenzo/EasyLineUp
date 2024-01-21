/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.views

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.widget.GridLayout
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Picasso
import com.telen.easylineup.R
import com.telen.easylineup.databinding.ViewCreatePlayerBinding
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerSide
import com.telen.easylineup.domain.model.Sex
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.getPositionShortName
import com.telen.easylineup.domain.model.isDefensePlayer
import timber.log.Timber

interface PlayerFormListener {
    fun onSaveClicked(
        name: String?,
        shirtNumber: Int?,
        licenseNumber: Long?,
        imageUri: Uri?,
        positions: Int,
        pitching: Int,
        batting: Int,
        email: String?,
        phone: String?,
        sex: Int
    )

    fun onCancel()
    fun onImagePickerRequested()
}

class PlayerFormView : ConstraintLayout {
    private val binding = ViewCreatePlayerBinding.inflate(LayoutInflater.from(context), this, true)
    private var listener: PlayerFormListener? = null
    private var imageUri: Uri? = null
    private var playerPositions = 0
    private val positionState: MutableMap<FieldPosition, Boolean> = mutableMapOf()

    init {
        binding.playerImage.setImageResource(R.drawable.unknown_player)

        binding.playerImage.setOnClickListener {
            listener?.onImagePickerRequested()
        }

        binding.actionContainer.saveClickListener = OnClickListener {
            val name = getName()
            val shirtNumber = getShirtNumber()
            val licenseNumber = getLicenseNumber()
            val positions = getPlayerPositions()
            val pitching = getPitchingSide()?.flag ?: 0
            val batting = getBattingSide()?.flag ?: 0
            val email = getEmail()
            val phone = getPhone()
            val sex = getSex()?.id ?: Sex.UNKNOWN.id

            listener?.onSaveClicked(
                name,
                shirtNumber,
                licenseNumber,
                imageUri,
                positions,
                pitching,
                batting,
                email,
                phone,
                sex
            )
        }

        binding.actionContainer.cancelClickListener = OnClickListener {
            listener?.onCancel()
        }

        binding.favoritePositionsContainer.useDefaultMargins = true
        binding.favoritePositionsContainer.alignmentMode = GridLayout.ALIGN_MARGINS

        setPositionsFilter(0)

        binding.playerImageAction.run {
            setImageResource(R.drawable.add_image)
            setOnClickListener(null)
        }

        binding.sexMale.setOnClickListener {
            if (binding.sexFemale.isChecked) {
                setSex(Sex.MALE)
            }
        }

        binding.sexFemale.setOnClickListener {
            if (binding.sexMale.isChecked) {
                setSex(Sex.FEMALE)
            }
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun setListener(listener: PlayerFormListener) {
        this.listener = listener
    }

    fun onImageUriReceived(imageUri: Uri) {
        setImage(imageUri)
    }

    fun getName(): String {
        return binding.playerNameInput.text.toString()
    }

    fun getShirtNumber(): Int? {
        return try {
            binding.playerShirtNumberInput.text.toString().toInt()
        } catch (exception: NumberFormatException) {
            null
        }
    }

    fun getLicenseNumber(): Long? {
        return try {
            binding.playerLicenseNumberInput.text.toString().toLong()
        } catch (exception: NumberFormatException) {
            null
        }
    }

    fun getImageUri(): Uri? {
        return imageUri
    }

    fun setName(name: String) {
        binding.playerNameInput.setText(name)
    }

    fun setShirtNumber(shirtNumber: Int) {
        binding.playerShirtNumberInput.setText(shirtNumber.toString())
    }

    fun setLicenseNumber(licenseNumber: Long) {
        binding.playerLicenseNumberInput.setText(licenseNumber.toString())
    }

    fun setPlayerImage(@DrawableRes resId: Int) {
        Picasso.get().load(resId)
            .placeholder(R.drawable.unknown_player)
            .error(R.drawable.unknown_player)
            .into(binding.playerImage)
    }

    fun setImage(uri: Uri) {
        imageUri = uri
        binding.playerImage.post {
            try {
                Picasso.get().load(imageUri)
                    .resize(binding.playerImage.width, binding.playerImage.height)
                    .centerCrop()
                    .transform(
                        RoundedTransformationBuilder()
                            .borderColor(Color.BLACK)
                            .borderWidthDp(2f)
                            .cornerRadiusDp(16f)
                            .oval(true)
                            .build()
                    )
                    .placeholder(R.drawable.unknown_player)
                    .error(R.drawable.unknown_player)
                    .into(binding.playerImage)
            } catch (e: IllegalArgumentException) {
                Timber.e(e)
            }
        }

        imageUri?.let {
            binding.playerImageAction.run {
                setImageResource(R.drawable.remove_image)
                setOnClickListener {
                    imageUri = null
                    setPlayerImage(R.drawable.unknown_player)
                    setImageResource(R.drawable.add_image)
                    setOnClickListener(null)
                }
            }
        }
    }

    fun enableSaveButton() {
        binding.actionContainer.setSaveButtonEnabled(true)
    }

    fun disableSaveButton() {
        binding.actionContainer.setSaveButtonEnabled(false)
    }

    fun getPlayerPositions(): Int {
        return playerPositions
    }

    fun setPositionsFilter(positions: Int) {
        this.playerPositions = positions
        this.positionState.clear()

        binding.favoritePositionsContainer.removeAllViews()

        FieldPosition.values()
            .filter { it.isDefensePlayer() }
            .groupBy { it.mask }
            .map { it.value.first() }
            .forEach { position ->
                val isEnabled = (positions and position.mask) != 0
                positionState[position] = isEnabled

                val view = PlayerPositionFilterView(context)
                view.setText(position.getPositionShortName(context, TeamType.UNKNOWN.id))
                applyFilterOnView(view, isEnabled)

                view.setOnClickListener { _ ->

                    positionState[position]?.let { oldState ->
                        val newState = !oldState

                        if (newState && positionState.filterValues { it == true }.size >= 3) {
                            return@setOnClickListener
                        }

                        positionState[position] = newState
                        applyFilterOnView(view, newState)
                        playerPositions = if (newState) {
                            playerPositions or position.mask
                        } else {
                            playerPositions and position.mask.inv()
                        }
                    }
                }

                binding.favoritePositionsContainer.addView(view)
            }
    }

    private fun applyFilterOnView(view: PlayerPositionFilterView, isEnabled: Boolean) {
        when (isEnabled) {
            true -> {
                view.setBackground(R.drawable.position_selected_background)
                view.setTextColor(R.color.white)
            }

            else -> {
                view.setBackground(R.drawable.position_unselected_background)
                view.setTextColor(R.color.tile_team_size_background_color)
            }
        }
    }

    // private fun getRoundedBitmap(bitmap: Bitmap): RoundedBitmapDrawable {
    // val min = Math.min(bitmap.width, bitmap.height)
    // val drawable = RoundedBitmapDrawableFactory.create(resources, Bitmap.createBitmap(bitmap, 0,0,min, min))
    // drawable.isCircular = true
    // return drawable
    // }

    fun displayInvalidName() {
        binding.playerNameInputLayout.error =
                resources.getString(R.string.player_creation_error_name_empty)
        binding.playerEmailInputLayout.error = null
        binding.playerPhoneInputLayout.error = null
    }

    fun displayInvalidEmail() {
        binding.playerNameInputLayout.error = null
        binding.playerEmailInputLayout.error =
                resources.getString(R.string.player_creation_error_invalid_email_format)
        binding.playerPhoneInputLayout.error = null
    }

    fun displayInvalidPhoneNumber() {
        binding.playerNameInputLayout.error = null
        binding.playerEmailInputLayout.error = null
        binding.playerPhoneInputLayout.error =
                resources.getString(R.string.player_creation_error_invalid_phone_format)
    }

    fun getPitchingSide(): PlayerSide? {
        var pitching = 0
        if (binding.pitchingSideLeft.isChecked) {
            pitching = pitching or PlayerSide.LEFT.flag
        }
        if (binding.pitchingSideRight.isChecked) {
            pitching = pitching or PlayerSide.RIGHT.flag
        }
        return PlayerSide.getSideByValue(pitching)
    }

    fun getBattingSide(): PlayerSide? {
        var batting = 0
        if (binding.battingSideLeft.isChecked) {
            batting = batting or PlayerSide.LEFT.flag
        }
        if (binding.battingSideRight.isChecked) {
            batting = batting or PlayerSide.RIGHT.flag
        }
        return PlayerSide.getSideByValue(batting)
    }

    fun getSex(): Sex? {
        return if (binding.sexMale.isChecked) {
            Sex.MALE
        } else if (binding.sexFemale.isChecked) {
            Sex.FEMALE
        } else {
            null
        }
    }

    fun setPitchingSide(side: PlayerSide?) {
        when (side) {
            PlayerSide.LEFT -> {
                binding.pitchingSideLeft.isChecked = true
                binding.pitchingSideRight.isChecked = false
            }

            PlayerSide.RIGHT -> {
                binding.pitchingSideLeft.isChecked = false
                binding.pitchingSideRight.isChecked = true
            }

            PlayerSide.BOTH -> {
                binding.pitchingSideLeft.isChecked = true
                binding.pitchingSideRight.isChecked = true
            }

            null -> {
                binding.pitchingSideLeft.isChecked = false
                binding.pitchingSideRight.isChecked = false
            }
            else -> {
                // this is a generated else block
            }
        }
    }

    fun setSex(sex: Sex?) {
        when (sex) {
            Sex.MALE -> {
                binding.sexMale.isChecked = true
                binding.sexFemale.isChecked = false
            }

            Sex.FEMALE -> {
                binding.sexMale.isChecked = false
                binding.sexFemale.isChecked = true
            }

            else -> {
                binding.sexMale.isChecked = false
                binding.sexFemale.isChecked = false
            }
        }
    }

    fun setBattingSide(side: PlayerSide?) {
        when (side) {
            PlayerSide.LEFT -> {
                binding.battingSideLeft.isChecked = true
                binding.battingSideRight.isChecked = false
            }

            PlayerSide.RIGHT -> {
                binding.battingSideLeft.isChecked = false
                binding.battingSideRight.isChecked = true
            }

            PlayerSide.BOTH -> {
                binding.battingSideLeft.isChecked = true
                binding.battingSideRight.isChecked = true
            }

            null -> {
                binding.battingSideLeft.isChecked = false
                binding.battingSideRight.isChecked = false
            }
            else -> {
                // this is a generated else block
            }
        }
    }

    fun getEmail(): String? {
        return binding.playerEmailInput.text.toString()
    }

    fun setEmail(email: String?) {
        binding.playerEmailInput.setText(email ?: "")
    }

    fun getPhone(): String? {
        return binding.playerPhoneInput.text.toString()
    }

    fun setPhone(phone: String?) {
        binding.playerPhoneInput.setText(phone ?: "")
    }
}
