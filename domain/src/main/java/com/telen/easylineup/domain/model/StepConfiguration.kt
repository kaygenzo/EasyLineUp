package com.telen.easylineup.domain.model

import androidx.annotation.StringRes

data class StepConfiguration(val nextStep: TeamCreationStep, val nextButtonEnabled: Boolean, val previousButtonEnabled: Boolean,
                             val nextButtonVisibility: Int, val previousButtonVisibility: Int, @StringRes val nextButtonLabel: Int,
                             @StringRes val previousButtonLabel: Int)