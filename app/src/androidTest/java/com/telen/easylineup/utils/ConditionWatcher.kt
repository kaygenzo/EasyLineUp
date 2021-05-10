package com.telen.easylineup.utils

/**
 * Created by F1sherKK on 08/10/15.
 * https://raw.githubusercontent.com/AzimoLabs/ConditionWatcher/master/conditionwatcher/src/main/java/com/azimolabs/conditionwatcher/ConditionWatcher.java
 */
class ConditionWatcher private constructor() {
    private var timeoutLimit = DEFAULT_TIMEOUT_LIMIT
    private var watchInterval = DEFAULT_INTERVAL

    companion object {
        const val CONDITION_NOT_MET = 0
        const val CONDITION_MET = 1
        const val TIMEOUT = 2
        const val DEFAULT_TIMEOUT_LIMIT = 1000 * 60
        const val DEFAULT_INTERVAL = 250
        private var conditionWatcher: ConditionWatcher? = null
        val instance: ConditionWatcher?
            get() {
                if (conditionWatcher == null) {
                    conditionWatcher = ConditionWatcher()
                }
                return conditionWatcher
            }

        @Throws(Exception::class)
        fun waitForCondition(instruction: Instruction) {
            var status = CONDITION_NOT_MET
            var elapsedTime = 0
            do {
                if (instruction.checkCondition()) {
                    status = CONDITION_MET
                } else {
                    elapsedTime += instance!!.watchInterval
                    Thread.sleep(instance!!.watchInterval.toLong())
                }
                if (elapsedTime >= instance!!.timeoutLimit) {
                    status = TIMEOUT
                    break
                }
            } while (status != CONDITION_MET)
            if (status == TIMEOUT) throw Exception(instruction.description + " - took more than " + instance!!.timeoutLimit / 1000 + " seconds. Test stopped.")
        }

        fun setWatchInterval(watchInterval: Int) {
            instance!!.watchInterval = watchInterval
        }

        fun setTimeoutLimit(ms: Int) {
            instance!!.timeoutLimit = ms
        }
    }
}