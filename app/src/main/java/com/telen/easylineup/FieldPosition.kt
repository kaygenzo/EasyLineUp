package com.telen.easylineup

enum class FieldPosition(val position: Int, val mask: Int) {
    PITCHER(1,      0b1),
    CATCHER(2,      0b10),
    FIRST_BASE(3,   0b100),
    SECOND_BASE(4,  0b1000),
    THIRD_BASE(5,   0b10000),
    SHORT_STOP(6,   0b100000),
    LEFT_FIELD(7,   0b1000000),
    CENTER_FIELD(8, 0b10000000),
    RIGHT_FIELD(9,  0b100000000);

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