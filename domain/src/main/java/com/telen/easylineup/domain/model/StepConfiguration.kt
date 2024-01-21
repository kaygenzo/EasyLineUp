/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

import android.view.View
import androidx.annotation.StringRes

/**
 * @property nextStep
 * @property nextButtonEnabled
 * @property previousButtonEnabled
 * @property nextButtonVisibility
 * @property previousButtonVisibility
 * @property nextButtonLabel
 * @property previousButtonLabel
 */
data class StepConfiguration(
    val nextStep: TeamCreationStep = TeamCreationStep.TEAM,
    val nextButtonEnabled: Boolean = false,
    val previousButtonEnabled: Boolean = false,
    val nextButtonVisibility: Int = View.GONE,
    val previousButtonVisibility: Int = View.GONE,
    @StringRes val nextButtonLabel: Int = 0,
    @StringRes val previousButtonLabel: Int = 0
)
