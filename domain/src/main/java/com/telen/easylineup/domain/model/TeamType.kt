package com.telen.easylineup.domain.model

enum class TeamType(val id: Int, val position: Int) {
    UNKNOWN(0, -1),
    BASEBALL(1, 0),
    SOFTBALL(2, 1);

    companion object {
        fun getTypeById(id: Int): TeamType {
            values().forEach {
                if(it.id == id)
                    return it
            }
            return UNKNOWN
        }

        fun getValidPositionsForTeam(teamType: TeamType, strategy: TeamStrategy): List<FieldPosition> {
            return when(teamType) {
                UNKNOWN, BASEBALL -> {
                    listOf(
                            FieldPosition.PITCHER,
                            FieldPosition.CATCHER,
                            FieldPosition.FIRST_BASE,
                            FieldPosition.SECOND_BASE,
                            FieldPosition.THIRD_BASE,
                            FieldPosition.SHORT_STOP,
                            FieldPosition.LEFT_FIELD,
                            FieldPosition.CENTER_FIELD,
                            FieldPosition.RIGHT_FIELD
                    )
                }
                SOFTBALL -> {
                    when(strategy) {
                        TeamStrategy.STANDARD -> {
                            listOf(
                                    FieldPosition.PITCHER,
                                    FieldPosition.CATCHER,
                                    FieldPosition.FIRST_BASE,
                                    FieldPosition.SECOND_BASE,
                                    FieldPosition.THIRD_BASE,
                                    FieldPosition.SHORT_STOP,
                                    FieldPosition.LEFT_FIELD,
                                    FieldPosition.CENTER_FIELD,
                                    FieldPosition.RIGHT_FIELD
                            )
                        }
                        TeamStrategy.SLOWPITCH -> {
                            listOf(
                                    FieldPosition.PITCHER,
                                    FieldPosition.CATCHER,
                                    FieldPosition.FIRST_BASE,
                                    FieldPosition.SECOND_BASE,
                                    FieldPosition.THIRD_BASE,
                                    FieldPosition.SHORT_STOP,
                                    FieldPosition.SLOWPITCH_LF,
                                    FieldPosition.SLOWPITCH_LCF,
                                    FieldPosition.SLOWPITCH_RCF,
                                    FieldPosition.SLOWPITCH_RF
                            )
                        }
                    }
                }
            }
        }
    }
}