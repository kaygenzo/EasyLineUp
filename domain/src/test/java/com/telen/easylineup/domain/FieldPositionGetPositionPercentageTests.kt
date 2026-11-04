/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.FieldPositionCoordinate
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.getPositionPercentage
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Covers the marker-placement coordinates [com.telen.easylineup.lineup.defense.DefenseScreen]
 * renders from - a wrong branch here silently moves a marker to the wrong spot on the field
 * rather than crashing, so it needed its own dedicated test rather than only being exercised
 * incidentally through a screen test with one fixed strategy.
 */
internal class FieldPositionGetPositionPercentageTests {

    @Test
    fun standardStrategyPlacesOutfieldersAtTheirOwnCoordinates() {
        assertEquals(
            FieldPositionCoordinate(15f, 15f),
            FieldPosition.LEFT_FIELD.getPositionPercentage(TeamStrategy.STANDARD)
        )
        assertEquals(
            FieldPositionCoordinate(50f, 10f),
            FieldPosition.CENTER_FIELD.getPositionPercentage(TeamStrategy.STANDARD)
        )
    }

    @Test
    fun infieldAndBatteryPositionsAreSharedAcrossNonB5Strategies() {
        assertEquals(
            FieldPositionCoordinate(50f, 59f),
            FieldPosition.PITCHER.getPositionPercentage(TeamStrategy.STANDARD)
        )
        assertEquals(
            FieldPositionCoordinate(50f, 59f),
            FieldPosition.PITCHER.getPositionPercentage(TeamStrategy.SLOWPITCH)
        )
        assertEquals(
            FieldPositionCoordinate(0f, 100f),
            FieldPosition.DP_DH.getPositionPercentage(TeamStrategy.STANDARD)
        )
    }

    @Test
    fun slowpitchStrategyPlacesItsOwnFourOutfielders() {
        assertEquals(
            FieldPositionCoordinate(14f, 20f),
            FieldPosition.SLOWPITCH_LF.getPositionPercentage(TeamStrategy.SLOWPITCH)
        )
        assertEquals(
            FieldPositionCoordinate(86f, 20f),
            FieldPosition.SLOWPITCH_RF.getPositionPercentage(TeamStrategy.SLOWPITCH)
        )
    }

    @Test
    fun fiveManStandardStrategyPlacesItsMidFielder() {
        assertEquals(
            FieldPositionCoordinate(50f, 35f),
            FieldPosition.MID_FIELDER.getPositionPercentage(TeamStrategy.FIVE_MAN_STANDARD)
        )
    }

    @Test
    fun b5DefaultStrategyIsASeparateBranchWithNoPitcherOrCatcher() {
        assertEquals(
            FieldPositionCoordinate(73.8f, 11.9f),
            FieldPosition.FIRST_BASE.getPositionPercentage(TeamStrategy.B5_DEFAULT)
        )
        assertEquals(
            FieldPositionCoordinate(11.9f, 11.9f),
            FieldPosition.MID_FIELDER.getPositionPercentage(TeamStrategy.B5_DEFAULT)
        )
    }

    @Test(expected = IllegalStateException::class)
    fun b5DefaultHasNoCoordinateForPitcher() {
        FieldPosition.PITCHER.getPositionPercentage(TeamStrategy.B5_DEFAULT)
    }

    @Test
    fun substitutesAlwaysSitAtTheOriginRegardlessOfStrategy() {
        assertEquals(
            FieldPositionCoordinate(0f, 0f),
            FieldPosition.SUBSTITUTE.getPositionPercentage(TeamStrategy.STANDARD)
        )
    }
}
