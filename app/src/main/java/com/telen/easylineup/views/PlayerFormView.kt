package com.telen.easylineup.views

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.GridLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.nguyenhoanglam.imagepicker.model.Image
import com.squareup.picasso.Picasso
import com.telen.easylineup.FieldPosition
import com.telen.easylineup.R
import kotlinx.android.synthetic.main.view_create_player.view.*

interface PlayerFormListener {
    fun onSaveClicked(name: String, shirtNumber: Int, licenseNumber: Long, imageUri: Uri?, positions: Int)
    fun onCancel()
    fun onImagePickerRequested()
}

class PlayerFormView: ConstraintLayout {

    private var listener: PlayerFormListener? = null
    private var imageUri: Uri? = null
    private var playerPositions = 0
    private val positionState: MutableMap<FieldPosition, Boolean> = mutableMapOf()

    constructor(context: Context) : super(context) {initView(context)}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {initView(context)}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {initView(context)}

    fun setListener(listener: PlayerFormListener) {
        this.listener = listener
    }

    private fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.view_create_player, this)

        playerImage.setImageResource(R.drawable.unknown_player)

        playerImage.setOnClickListener {
            listener?.onImagePickerRequested()
        }

        playerSave.setOnClickListener {
            val name = getName()
            val shirtNumber = getShirtNumber()
            val licenseNumber = getLicenseNumber()
            val positions = getPlayerPositions()

            if(name!=null && shirtNumber!=null && licenseNumber!=null) {
                listener?.onSaveClicked(name, shirtNumber, licenseNumber, imageUri, positions)
            }
            else {
                if(name!=null) {
                    playerNameInputLayout.error = null
                } else {
                    playerNameInputLayout.error = resources.getString(R.string.player_creation_error_name_empty)
                }

                if(shirtNumber!=null) {
                    playerShirtNumberInputLayout.error = null
                } else {
                    playerShirtNumberInputLayout.error = resources.getString(R.string.player_creation_error_shirt_empty)
                }

                if(licenseNumber!=null) {
                    playerLicenseNumberInputLayout.error = null
                } else {
                    playerLicenseNumberInputLayout.error = resources.getString(R.string.player_creation_error_license_empty)
                }
            }
        }

        cancel.setOnClickListener {
            listener?.onCancel()
        }

        favoritePositionsContainer.useDefaultMargins = true
        favoritePositionsContainer.alignmentMode = GridLayout.ALIGN_MARGINS

        setPositionsFilter(0)
    }

    fun onImageUriReceived(image: Image) {
        val filePathUri = android.content.ContentResolver.SCHEME_FILE + ":///" + image.path
        setImage(filePathUri)
    }

    fun getName(): String? {
        val name = playerNameInput.text.toString()
        return if(TextUtils.isEmpty(name))
            null
        else
            name
    }

    fun getShirtNumber(): Int? {
        return try {
            playerShirtNumberInput.text.toString().toInt()
        }
        catch (exception: NumberFormatException) {
            null
        }
    }

    fun getLicenseNumber(): Long? {
        return try {
            playerLicenseNumberInput.text.toString().toLong()
        }
        catch (exception: NumberFormatException) {
            null
        }
    }

    fun getImageUri(): Uri? {
        return imageUri
    }

    fun setName(name: String) {
        playerNameInput.setText(name)
    }

    fun setShirtNumber(shirtNumber: Int) {
        playerShirtNumberInput.setText(shirtNumber.toString())
    }

    fun setLicenseNumber(licenseNumber: Long) {
        playerLicenseNumberInput.setText(licenseNumber.toString())
    }

    fun setImage(imagePath: String) {

        imageUri = Uri.parse(imagePath)

        playerImage.post {
            Picasso.get().load(imageUri)
                    .resize(playerImage.width, playerImage.height)
                    .centerCrop()
                    .transform(RoundedTransformationBuilder()
                            .borderColor(Color.BLACK)
                            .borderWidthDp(2f)
                            .cornerRadiusDp(16f)
                            .oval(true)
                            .build())
                    .placeholder(R.drawable.unknown_player)
                    .error(R.drawable.unknown_player)
                    .into(playerImage)
        }
    }

    fun enableSaveButton() {
        playerSave.isEnabled = true
    }

    fun disableSaveButton() {
        playerSave.isEnabled = false
    }

    fun getPlayerPositions() : Int {
        return playerPositions
    }

    fun setPositionsFilter(positions: Int) {

        this.playerPositions = positions
        this.positionState.clear()

        val positionShortDescription = resources.getStringArray(R.array.field_positions_list)

        favoritePositionsContainer.removeAllViews()

        FieldPosition.values()
                .filter { FieldPosition.isDefensePlayer(it.position) }
                .forEach { position ->
            val isEnabled = (positions and position.mask) != 0
            positionState[position] = isEnabled

            val view = PlayerPositionFilterView(context)
            view.setText(positionShortDescription[position.ordinal])
            applyFilterOnView(view, isEnabled)

            view.setOnClickListener { _ ->

                positionState[position]?.let { oldState ->
                    val newState = !oldState

                    if(newState) {
                        if(positionState.filterValues { it == true }.size >= 3) {
                            return@setOnClickListener
                        }
                    }

                    positionState[position] = newState
                    applyFilterOnView(view, newState)
                    playerPositions = if(newState) {
                        playerPositions or position.mask
                    } else {
                        playerPositions and position.mask.inv()
                    }
                }
            }

            favoritePositionsContainer.addView(view)
        }
    }

    private fun applyFilterOnView(view: PlayerPositionFilterView, isEnabled: Boolean) {
        when(isEnabled) {
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

//    private fun getRoundedBitmap(bitmap: Bitmap): RoundedBitmapDrawable {
//        val min = Math.min(bitmap.width, bitmap.height)
//        val drawable = RoundedBitmapDrawableFactory.create(resources, Bitmap.createBitmap(bitmap, 0,0,min, min))
//        drawable.isCircular = true
//        return drawable
//    }
}