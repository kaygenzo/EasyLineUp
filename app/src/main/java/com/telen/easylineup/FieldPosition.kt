package com.telen.easylineup

enum class FieldPosition(val position: Int) {
    PITCHER(1),
    CATCHER(2),
    FIRST_BASE(3),
    SECOND_BASE(4),
    THIRD_BASE(5),
    SHORT_STOP(6),
    LEFT_FIELD(7),
    CENTER_FIELD(8),
    RIGHT_FIELD(9);

    companion object {
        fun getFieldPosition(position: Int): FieldPosition? {
            values().forEach {
                if(position == it.position)
                    return it
            }
            return null
        }
    }
}