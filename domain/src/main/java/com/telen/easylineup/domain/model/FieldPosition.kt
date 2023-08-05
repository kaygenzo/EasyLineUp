package com.telen.easylineup.domain.model

import android.content.Context
import android.graphics.PointF
import com.telen.easylineup.domain.R

enum class FieldPosition(val id: Int, val mask: Int, val position: Int) {
    OLD_SUBSTITUTE(0, 0, 0),
    PITCHER(1, 0x01, 1),
    CATCHER(2, 0x02, 2),
    FIRST_BASE(3, 0x04, 3),
    SECOND_BASE(4, 0x08, 4),
    THIRD_BASE(5, 0x10, 5),
    SHORT_STOP(6, 0x20, 6),
    LEFT_FIELD(7, 0x40, 7),
    CENTER_FIELD(8, 0x80, 8),
    RIGHT_FIELD(9, 0x0100, 9),
    DP_DH(10, 0x0200, 255),

    //Slowpitch
    SLOWPITCH_LF(11, LEFT_FIELD.mask, 7),
    SLOWPITCH_LCF(12, 0x0800, 8),
    SLOWPITCH_RCF(13, 0x1000, 9),
    SLOWPITCH_RF(14, RIGHT_FIELD.mask, 10),

    //baseball5
    MID_FIELDER(15, 0x2000, 10),
    SUBSTITUTE(16, 0, 999);

    companion object {
        fun getFieldPositionById(id: Int): FieldPosition? {
            return values().firstOrNull { id == it.id }
        }
    }
}

//TODO move in use case
fun FieldPosition.isSubstitute(): Boolean {
    return id == FieldPosition.OLD_SUBSTITUTE.id || id == FieldPosition.SUBSTITUTE.id
}

//TODO move in use case
fun FieldPosition.isDefensePlayer(): Boolean {
    return !isSubstitute() && id != FieldPosition.DP_DH.id
}

fun FieldPosition.getPositionShortName(context: Context, teamType: Int): String {
    return when (teamType) {
        TeamType.SOFTBALL.id -> {
            context.resources.getStringArray(R.array.field_positions_softball_list)[ordinal]
        }

        else -> {
            context.resources.getStringArray(R.array.field_positions_baseball_list)[ordinal]
        }
    }
}

fun FieldPosition.getPositionPercentage(strategy: TeamStrategy): PointF {
    return when (strategy) {
        TeamStrategy.B5_DEFAULT -> {
            when (this) {
                FieldPosition.OLD_SUBSTITUTE, FieldPosition.SUBSTITUTE -> PointF(0f, 0f)
                FieldPosition.FIRST_BASE -> PointF(73.8f, 11.9f)
                FieldPosition.SECOND_BASE -> PointF(42.86f, 11.9f)
                FieldPosition.THIRD_BASE -> PointF(11.9f, 73.8f)
                FieldPosition.SHORT_STOP -> PointF(11.9f, 42.86f)
                FieldPosition.MID_FIELDER -> PointF(11.9f, 11.9f)
                else -> throw Exception("Not a valid position for strategy $strategy")
            }
        }

        else -> {
            when (this) {
                FieldPosition.OLD_SUBSTITUTE, FieldPosition.SUBSTITUTE -> PointF(0f, 0f)
                FieldPosition.PITCHER -> PointF(50f, 59f)
                FieldPosition.CATCHER -> PointF(50f, 87f)
                FieldPosition.FIRST_BASE -> PointF(74f, 57f)
                FieldPosition.SECOND_BASE -> PointF(63f, 44f)
                FieldPosition.THIRD_BASE -> PointF(27f, 57f)
                FieldPosition.SHORT_STOP -> PointF(37f, 44f)
                FieldPosition.DP_DH -> PointF(0f, 100f)
                else -> {
                    when (strategy) {
                        TeamStrategy.STANDARD -> {
                            when (this) {
                                FieldPosition.LEFT_FIELD -> PointF(15f, 15f)
                                FieldPosition.CENTER_FIELD -> PointF(50f, 10f)
                                FieldPosition.RIGHT_FIELD -> PointF(85f, 15f)
                                else -> throw Exception("Not a valid position $this for strategy $strategy")
                            }
                        }

                        TeamStrategy.SLOWPITCH -> {
                            when (this) {
                                FieldPosition.SLOWPITCH_LF -> PointF(14f, 20f)
                                FieldPosition.SLOWPITCH_LCF -> PointF(38f, 10f)
                                FieldPosition.SLOWPITCH_RCF -> PointF(62f, 10f)
                                FieldPosition.SLOWPITCH_RF -> PointF(86f, 20f)
                                else -> throw Exception("Not a valid position for strategy $strategy")
                            }
                        }

                        TeamStrategy.FIVE_MAN_STANDARD -> {
                            when (this) {
                                FieldPosition.LEFT_FIELD -> PointF(30f, 15f)
                                FieldPosition.RIGHT_FIELD -> PointF(70f, 15f)
                                FieldPosition.MID_FIELDER -> PointF(50f, 35f)
                                else -> throw Exception("Not a valid position for strategy $strategy")
                            }
                        }

                        else -> PointF(0f, 0f)
                    }
                }
            }
        }
    }
}