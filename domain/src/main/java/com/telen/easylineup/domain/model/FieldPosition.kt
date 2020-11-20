package com.telen.easylineup.domain.model

import android.content.Context
import android.graphics.PointF
import com.telen.easylineup.domain.R
import java.lang.Exception

enum class FieldPosition(val id: Int, val mask: Int, private val position: Int) {
    SUBSTITUTE(0, 0, 0),
    PITCHER(1,      0x01, 1),
    CATCHER(2,      0x02, 2),
    FIRST_BASE(3,   0x04, 3),
    SECOND_BASE(4,  0x08, 4),
    THIRD_BASE(5,   0x10, 5),
    SHORT_STOP(6,   0x20, 6),
    LEFT_FIELD(7,   0x40, 7),
    CENTER_FIELD(8, 0x80, 8),
    RIGHT_FIELD(9,  0x0100, 9),
    DP_DH(10,  0x0200, 10),

    //Slowpitch
    SLOWPITCH_LF(11, LEFT_FIELD.mask, 7),
    SLOWPITCH_LCF(12, 0x0800, 8),
    SLOWPITCH_RCF(13, 0x1000, 9),
    SLOWPITCH_RF(14, RIGHT_FIELD.mask, 10);

    fun getPosition(): Int {
        return position
    }

    companion object {
        fun getFieldPositionById(id: Int): FieldPosition? {
            values().forEach {
                if(id == it.id)
                    return it
            }
            return null
        }

        //TODO move in domain
        fun isSubstitute(id: Int): Boolean {
            return id == SUBSTITUTE.id
        }

        //TODO move in domain
        fun isDefensePlayer(id: Int): Boolean {
            return !isSubstitute(id) && id != DP_DH.id
        }

        //TODO move in domain
        fun canBeBatterWhenModeEnabled(id: Int, flags: Int): Boolean {
            return !isSubstitute(id) && (flags and PlayerFieldPosition.FLAG_FLEX == 0)
        }

        fun getPositionShortNames(context: Context, teamType: Int): Array<String> {
            return when(teamType) {
                TeamType.SOFTBALL.id -> {
                    context.resources.getStringArray(R.array.field_positions_softball_list)
                }
                else -> { //apply baseball rule
                    context.resources.getStringArray(R.array.field_positions_baseball_list)
                }
            }
        }

        fun getPositionCoordinates(position: FieldPosition, strategy: TeamStrategy): PointF {
            return when(position) {
                SUBSTITUTE -> PointF(0f, 0f)
                PITCHER -> PointF(50f, 60f)
                CATCHER -> PointF(50f, 87f)
                FIRST_BASE -> PointF(74f, 57f)
                SECOND_BASE -> PointF(63f, 44f)
                THIRD_BASE -> PointF(27f, 59f)
                SHORT_STOP -> PointF(37f, 43f)
                DP_DH -> PointF(0f, 100f)
                else -> {
                    when(strategy) {
                        TeamStrategy.STANDARD -> {
                            when (position) {
                                LEFT_FIELD -> PointF(15f, 15f)
                                CENTER_FIELD -> PointF(50f, 10f)
                                RIGHT_FIELD -> PointF(85f, 15f)
                                else -> throw Exception("Not a valid position $position for strategy $strategy")
                            }
                        }
                        TeamStrategy.SLOWPITCH -> {
                            when (position) {
                                SLOWPITCH_LF -> PointF(14f, 20f)
                                SLOWPITCH_LCF -> PointF(38f, 10f)
                                SLOWPITCH_RCF -> PointF(62f, 10f)
                                SLOWPITCH_RF -> PointF(86f, 20f)
                                else -> throw Exception("Not a valid position for strategy $strategy")
                            }
                        }
//                TeamStrategy.SLOWPITCH_5_MAN -> TODO()
                    }
                }
            }
        }
    }
}