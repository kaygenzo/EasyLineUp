package com.telen.easylineup.utils

import android.os.Bundle

/**
 * Created by F1sherKK on 16/12/15.
 * https://github.com/AzimoLabs/ConditionWatcher/blob/master/conditionwatcher/src/main/java/com/azimolabs/conditionwatcher/Instruction.java
 */
abstract class Instruction {
    var dataContainer = Bundle()
        private set

    fun setData(dataContainer: Bundle) {
        this.dataContainer = dataContainer
    }

    abstract val description: String?

    abstract fun checkCondition(): Boolean
}