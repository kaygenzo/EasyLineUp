package com.telen.easylineup.domain

import android.content.Context
import android.content.res.Resources
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.usecases.GetBattersState
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////// EDITABLE ////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@RunWith(MockitoJUnitRunner.Silent::class)
internal abstract class GetBattersStateEditableTests(
    teamType: TeamType,
    strategy: TeamStrategy,
    batterSize: Int,
    extraHittersSize: Int
) : GetBattersStateTests(teamType, strategy, batterSize, extraHittersSize, true) {

    @Test
    fun dp_should_only_showIndex_and_showDescription_and_canMove_in_extra_hitter_when_lineup_editable() {
        addPlayers()
        addExtraHitters(extraHittersSize)
        players[0].position = FieldPosition.DP_DH.id
        //switch order to be sure dp will be at the end of the list because it's sorted by order
        val old = players[0].order
        val new = players[players.size - 1].order
        players[0].order = new
        players[players.size - 1].order = old

        applyUserCase()

        val expected = BatterState(
            1L,
            PlayerFieldPosition.FLAG_NONE,
            new,
            "t1",
            "1",
            FieldPosition.DP_DH,
            playerPositionDesc = if (teamType == TeamType.BASEBALL) "DH" else "DP",
            canShowPosition = false,
            canMove = true,
            canShowDescription = true,
            canShowOrder = true,
            origin = players[0],
            applyBackground = false,
            isEditable = isEditable
        )

        Assert.assertEquals(
            1,
            observer.values()[0].players.filter { it.playerPosition == FieldPosition.DP_DH }.size
        )
        Assert.assertEquals(
            expected,
            observer.values()[0].players.first { it.playerPosition == FieldPosition.DP_DH })
    }

    @Test
    fun flex_should_haveBackground_and_showPosition_in_extra_hitter_when_lineup_editable() {
        addPlayers()
        addExtraHitters(extraHittersSize)

        val newFlexOrder = batterSize + extraHittersSize + 1
        players[0].apply {
            flags = PlayerFieldPosition.FLAG_FLEX
            order = newFlexOrder
        }

        applyUserCase()

        val expected = BatterState(
            1L,
            PlayerFieldPosition.FLAG_FLEX,
            newFlexOrder,
            "t1",
            "1",
            FieldPosition.PITCHER,
            playerPositionDesc = "P",
            canShowPosition = true,
            canMove = false,
            canShowDescription = false,
            canShowOrder = false,
            origin = players[0],
            applyBackground = true,
            isEditable = isEditable
        )

        Assert.assertEquals(
            1,
            observer.values()[0].players.filter { it.playerFlag == PlayerFieldPosition.FLAG_FLEX }.size
        )
        Assert.assertEquals(
            expected,
            observer.values()[0].players.first { it.playerFlag == PlayerFieldPosition.FLAG_FLEX })
    }

    @Test
    fun defense_player_should_only_showIndex_and_showPosition_and_canMove_when_lineup_editable() {
        addPlayers()

        applyUserCase()

        var i = 1
        observer.values()[0].players.forEach { batterState ->
            val value = batterSize - i + 1
            // we don't test here the field position and desc, so let's use the same as the loop object
            val expected = BatterState(
                value.toLong(),
                PlayerFieldPosition.FLAG_NONE,
                i,
                "t$value",
                "$value",
                batterState.playerPosition,
                playerPositionDesc = batterState.playerPositionDesc,
                canShowPosition = true,
                canMove = true,
                canShowDescription = false,
                canShowOrder = true,
                origin = players[batterSize - i],
                applyBackground = false,
                isEditable = isEditable
            )
            Assert.assertEquals(expected, batterState)
            i++
        }
    }

    @Test
    fun substitute_should_in_extra_hitters_bounds_to_bat_when_lineup_editable() {
        addPlayers()
        addExtraHitters(extraHittersSize)
        addSubstitutes()

        applyUserCase()

        var i = batterSize + 1
        observer.values()[0].players.filter { it.playerPosition == FieldPosition.SUBSTITUTE }
            .forEach { batterState ->
                // we don't test here the field position and desc, so let's use the same as the loop object
                val subAllowBatting = i <= batterSize + extraHittersSize
                val expected = BatterState(
                    i.toLong(),
                    PlayerFieldPosition.FLAG_NONE,
                    i,
                    "t$i",
                    "$i",
                    FieldPosition.SUBSTITUTE,
                    playerPositionDesc = "SUB",
                    canShowPosition = false,
                    canMove = subAllowBatting,
                    canShowDescription = true,
                    canShowOrder = subAllowBatting,
                    origin = players[i - 1],
                    applyBackground = false,
                    isEditable = isEditable
                )
                Assert.assertEquals(expected, batterState)
                i++
            }
    }

    @Test
    fun substitute_should_not_bat_if_index_greater_than_extra_hitters_size_when_lineup_editable() {
        addExtraHitters(extraHittersSize)
        addSubstitutes()

        applyUserCase()

        var i = 1
        observer.values()[0].players.filter { it.playerPosition == FieldPosition.SUBSTITUTE }
            .forEach { batterState ->
                // we don't test here the field position and desc, so let's use the same as the loop object
                val subAllowBatting = i <= extraHittersSize
                val expected = BatterState(
                    i.toLong(),
                    PlayerFieldPosition.FLAG_NONE,
                    i,
                    "t$i",
                    "$i",
                    FieldPosition.SUBSTITUTE,
                    playerPositionDesc = "SUB",
                    canShowPosition = false,
                    canMove = subAllowBatting,
                    canShowDescription = true,
                    canShowOrder = subAllowBatting,
                    origin = players[i - 1],
                    applyBackground = false,
                    isEditable = isEditable
                )
                Assert.assertEquals(expected, batterState)
                i++
            }
    }

    @Test
    fun shouldNotShowSubstitutesOrderIfCannotMove_when_lineup_editable() {
        addPlayers()
        addSubstitutes(Constants.SUBSTITUTE_ORDER_VALUE)
        for (i in batterSize until (batterSize + extraHittersSize)) {
            players[i].order = i + 1
        }
        players.removeAt(0)
        applyUserCase()

        var i = batterSize + 1
        observer.values()[0].players.filter { it.playerPosition == FieldPosition.SUBSTITUTE }
            .forEach { batterState ->
                // we don't test here the field position and desc, so let's use the same as the loop object
                val subAllowBatting = i <= batterSize + extraHittersSize
                val expected = BatterState(
                    i.toLong(),
                    PlayerFieldPosition.FLAG_NONE,
                    if (subAllowBatting) i else Constants.SUBSTITUTE_ORDER_VALUE,
                    "t$i",
                    "$i",
                    FieldPosition.SUBSTITUTE,
                    playerPositionDesc = "SUB",
                    canShowPosition = false,
                    canMove = subAllowBatting,
                    canShowDescription = true,
                    canShowOrder = subAllowBatting,
                    origin = players[i - 2],
                    applyBackground = false,
                    isEditable = isEditable
                )
                Assert.assertEquals(expected, batterState)
                i++
            }
    }
}

////////////// EDITABLE NO HITTER SIZE //////////////

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateBaseballStandardEditableTests : GetBattersStateEditableTests(
    TeamType.BASEBALL, TeamStrategy.STANDARD,
    TeamStrategy.STANDARD.batterSize, 0
)

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateSoftballStandardEditableTests : GetBattersStateEditableTests(
    TeamType.SOFTBALL, TeamStrategy.STANDARD,
    TeamStrategy.STANDARD.batterSize, 0
)

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateBaseball5ManEditableTests : GetBattersStateEditableTests(
    TeamType.BASEBALL,
    TeamStrategy.FIVE_MAN_STANDARD,
    TeamStrategy.FIVE_MAN_STANDARD.batterSize,
    0
)

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateSoftballSlowpitchEditableTests : GetBattersStateEditableTests(
    TeamType.SOFTBALL, TeamStrategy.SLOWPITCH,
    TeamStrategy.SLOWPITCH.batterSize, 0
)

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateSoftball5ManEditableTests : GetBattersStateEditableTests(
    TeamType.SOFTBALL, TeamStrategy.FIVE_MAN_STANDARD,
    TeamStrategy.FIVE_MAN_STANDARD.batterSize, 0
)

////////////// EDITABLE CUSTOM HITTER SIZE //////////////

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateBaseballCustomStandardEditableTests : GetBattersStateEditableTests(
    TeamType.BASEBALL, TeamStrategy.STANDARD,
    TeamStrategy.STANDARD.batterSize, 3
)

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateSoftballCustomStandardEditableTests : GetBattersStateEditableTests(
    TeamType.SOFTBALL, TeamStrategy.STANDARD,
    TeamStrategy.STANDARD.batterSize, 3
)

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateBaseballCustom5ManEditableTests : GetBattersStateEditableTests(
    TeamType.BASEBALL,
    TeamStrategy.FIVE_MAN_STANDARD,
    TeamStrategy.FIVE_MAN_STANDARD.batterSize,
    3
)

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateSoftballCustomSlowpitchEditableTests : GetBattersStateEditableTests(
    TeamType.SOFTBALL, TeamStrategy.SLOWPITCH,
    TeamStrategy.SLOWPITCH.batterSize, 3
)

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateSoftballCustom5ManEditableTests : GetBattersStateEditableTests(
    TeamType.SOFTBALL, TeamStrategy.FIVE_MAN_STANDARD,
    TeamStrategy.FIVE_MAN_STANDARD.batterSize, 3
)

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////// NOT EDITABLE //////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@RunWith(MockitoJUnitRunner.Silent::class)
internal abstract class GetBattersStateNotEditableTests(
    teamType: TeamType,
    strategy: TeamStrategy,
    batterSize: Int,
    extraHittersSize: Int
) : GetBattersStateTests(teamType, strategy, batterSize, extraHittersSize, false) {

    @Test
    fun dp_should_only_showIndex_and_showDescription_when_lineup_not_editable() {
        addPlayers()
        addExtraHitters(extraHittersSize)
        players[0].position = FieldPosition.DP_DH.id
        //switch order to be sure dp will be at the end of the list because it's sorted by order
        val old = players[0].order
        val new = players[players.size - 1].order
        players[0].order = new
        players[players.size - 1].order = old

        applyUserCase()

        val expected = BatterState(
            1L,
            PlayerFieldPosition.FLAG_NONE,
            new,
            "t1",
            "1",
            FieldPosition.DP_DH,
            playerPositionDesc = if (teamType == TeamType.BASEBALL) "DH" else "DP",
            canShowPosition = false,
            canMove = false,
            canShowDescription = true,
            canShowOrder = true,
            origin = players[0],
            applyBackground = false,
            isEditable = isEditable
        )

        Assert.assertEquals(
            1,
            observer.values()[0].players.filter { it.playerPosition == FieldPosition.DP_DH }.size
        )
        Assert.assertEquals(
            expected,
            observer.values()[0].players.first { it.playerPosition == FieldPosition.DP_DH })
    }

    @Test
    fun flex_should_only_haveBackground_and_showPosition_showDescription_when_lineup_not_editable() {
        addPlayers()
        addExtraHitters(extraHittersSize)

        val newFlexOrder = batterSize + extraHittersSize + 1
        players[0].apply {
            flags = PlayerFieldPosition.FLAG_FLEX
            order = newFlexOrder
        }

        applyUserCase()

        val expected = BatterState(
            1L,
            PlayerFieldPosition.FLAG_FLEX,
            newFlexOrder,
            "t1",
            "1",
            FieldPosition.PITCHER,
            playerPositionDesc = "P",
            canShowPosition = true,
            canMove = false,
            canShowDescription = true,
            canShowOrder = false,
            origin = players[0],
            applyBackground = true,
            isEditable = isEditable
        )

        Assert.assertEquals(
            1,
            observer.values()[0].players.filter { it.playerFlag == PlayerFieldPosition.FLAG_FLEX }.size
        )
        Assert.assertEquals(
            expected,
            observer.values()[0].players.first { it.playerFlag == PlayerFieldPosition.FLAG_FLEX })
    }

    @Test
    fun defense_player_should_only_showIndex_and_showPosition_showDescription_when_lineup_editable() {
        addPlayers()

        applyUserCase()

        var i = 1
        observer.values()[0].players.forEach { batterState ->
            val value = batterSize - i + 1
            // we don't test here the field position and desc, so let's use the same as the loop object
            val expected = BatterState(
                value.toLong(),
                PlayerFieldPosition.FLAG_NONE,
                i,
                "t$value",
                "$value",
                batterState.playerPosition,
                playerPositionDesc = batterState.playerPositionDesc,
                canShowPosition = true,
                canMove = false,
                canShowDescription = true,
                canShowOrder = true,
                origin = players[batterSize - i],
                applyBackground = false,
                isEditable = isEditable
            )
            Assert.assertEquals(expected, batterState)
            i++
        }
    }

    @Test
    fun substitute_should_in_extra_hitters_bounds_to_bat_when_lineup_editable() {
        addPlayers()
        addExtraHitters(extraHittersSize)
        addSubstitutes()

        applyUserCase()

        var i = batterSize + 1
        observer.values()[0].players.filter { it.playerPosition == FieldPosition.SUBSTITUTE }
            .forEach { batterState ->
                // we don't test here the field position and desc, so let's use the same as the loop object
                val subAllowBatting = i <= batterSize + extraHittersSize
                val expected = BatterState(
                    i.toLong(),
                    PlayerFieldPosition.FLAG_NONE,
                    i,
                    "t$i",
                    "$i",
                    FieldPosition.SUBSTITUTE,
                    playerPositionDesc = "SUB",
                    canShowPosition = false,
                    canMove = false,
                    canShowDescription = true,
                    canShowOrder = subAllowBatting,
                    origin = players[i - 1],
                    applyBackground = false,
                    isEditable = isEditable
                )
                Assert.assertEquals(expected, batterState)
                i++
            }
    }

    @Test
    fun substitute_should_not_bat_if_index_greater_than_extra_hitters_size_when_lineup_editable() {
        addExtraHitters(extraHittersSize)
        addSubstitutes()

        applyUserCase()

        var i = 1
        observer.values()[0].players.filter { it.playerPosition == FieldPosition.SUBSTITUTE }
            .forEach { batterState ->
                // we don't test here the field position and desc, so let's use the same as the loop object
                val subAllowBatting = i <= extraHittersSize
                val expected = BatterState(
                    i.toLong(),
                    PlayerFieldPosition.FLAG_NONE,
                    i,
                    "t$i",
                    "$i",
                    FieldPosition.SUBSTITUTE,
                    playerPositionDesc = "SUB",
                    canShowPosition = false,
                    canMove = false,
                    canShowDescription = true,
                    canShowOrder = subAllowBatting,
                    origin = players[i - 1],
                    applyBackground = false,
                    isEditable = isEditable
                )
                Assert.assertEquals(expected, batterState)
                i++
            }
    }

    @Test
    fun shouldNotShowSubstitutesOrderIfCannotMove() {
        addPlayers()
        addSubstitutes(Constants.SUBSTITUTE_ORDER_VALUE)
        for (i in batterSize until (batterSize + extraHittersSize)) {
            players[i].order = i + 1
        }
        players.removeAt(0)
        applyUserCase()

        var i = batterSize + 1
        observer.values()[0].players.filter { it.playerPosition == FieldPosition.SUBSTITUTE }
            .forEach { batterState ->
                // we don't test here the field position and desc, so let's use the same as the loop object
                val subAllowBatting = i <= batterSize + extraHittersSize
                val expected = BatterState(
                    i.toLong(),
                    PlayerFieldPosition.FLAG_NONE,
                    if (subAllowBatting) i else Constants.SUBSTITUTE_ORDER_VALUE,
                    "t$i",
                    "$i",
                    FieldPosition.SUBSTITUTE,
                    playerPositionDesc = "SUB",
                    canShowPosition = false,
                    canMove = false,
                    canShowDescription = true,
                    canShowOrder = subAllowBatting,
                    origin = players[i - 2],
                    applyBackground = false,
                    isEditable = isEditable
                )
                Assert.assertEquals(expected, batterState)
                i++
            }
    }

}

////////////// NOT EDITABLE NO HITTER SIZE //////////////

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateBaseballStandardNotEditableTests : GetBattersStateNotEditableTests(
    TeamType.BASEBALL, TeamStrategy.STANDARD,
    TeamStrategy.STANDARD.batterSize, 0
)

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateSoftballStandardNotEditableTests : GetBattersStateNotEditableTests(
    TeamType.SOFTBALL, TeamStrategy.STANDARD,
    TeamStrategy.STANDARD.batterSize, 0
)

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateBaseball5ManNotEditableTests : GetBattersStateNotEditableTests(
    TeamType.BASEBALL, TeamStrategy.FIVE_MAN_STANDARD,
    TeamStrategy.FIVE_MAN_STANDARD.batterSize, 0
)

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateSoftballSlowpitchNotEditableTests : GetBattersStateNotEditableTests(
    TeamType.SOFTBALL, TeamStrategy.SLOWPITCH,
    TeamStrategy.SLOWPITCH.batterSize, 0
)

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateSoftball5ManNotEditableTests : GetBattersStateNotEditableTests(
    TeamType.SOFTBALL, TeamStrategy.FIVE_MAN_STANDARD,
    TeamStrategy.FIVE_MAN_STANDARD.batterSize, 0
)

////////////// NOT EDITABLE CUSTOM HITTER SIZE //////////////

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateBaseballCustomStandardNotEditableTests :
    GetBattersStateNotEditableTests(
        TeamType.BASEBALL, TeamStrategy.STANDARD,
        TeamStrategy.STANDARD.batterSize, 3
    )

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateSoftballCustomStandardNotEditableTests :
    GetBattersStateNotEditableTests(
        TeamType.SOFTBALL, TeamStrategy.STANDARD,
        TeamStrategy.STANDARD.batterSize, 3
    )

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateBaseballCustom5ManNotEditableTests : GetBattersStateNotEditableTests(
    TeamType.BASEBALL,
    TeamStrategy.FIVE_MAN_STANDARD,
    TeamStrategy.FIVE_MAN_STANDARD.batterSize,
    3
)

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateSoftballCustomSlowpitchNotEditableTests :
    GetBattersStateNotEditableTests(
        TeamType.SOFTBALL, TeamStrategy.SLOWPITCH,
        TeamStrategy.SLOWPITCH.batterSize, 3
    )

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateSoftballCustom5ManNotEditableTests : GetBattersStateNotEditableTests(
    TeamType.SOFTBALL, TeamStrategy.FIVE_MAN_STANDARD,
    TeamStrategy.FIVE_MAN_STANDARD.batterSize, 3
)

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////// BASE /////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@RunWith(MockitoJUnitRunner.Silent::class)
internal abstract class GetBattersStateTests(
    protected val teamType: TeamType, protected val strategy: TeamStrategy,
    protected val batterSize: Int, protected val extraHittersSize: Int,
    protected val isEditable: Boolean
) {

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var resources: Resources
    lateinit var mGetBattersState: GetBattersState
    protected val players = mutableListOf<PlayerWithPosition>()

    val lineupMode = MODE_ENABLED
    val observer = TestObserver<GetBattersState.ResponseValue>()

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mGetBattersState = GetBattersState()

        Mockito.`when`(context.resources).thenReturn(resources)
        Mockito.`when`(resources.getStringArray(R.array.field_positions_baseball_list)).thenReturn(
            arrayOf(
                "SUB",
                "P",
                "C",
                "1B",
                "2B",
                "3B",
                "SS",
                "LF",
                "CF",
                "RF",
                "DH",
                "LF",
                "LCF",
                "RCF",
                "RF",
                "MF",
                "SUB"
            )
        )

        Mockito.`when`(resources.getStringArray(R.array.field_positions_softball_list)).thenReturn(
            arrayOf(
                "SUB",
                "P",
                "C",
                "1B",
                "2B",
                "3B",
                "SS",
                "LF",
                "CF",
                "RF",
                "DP",
                "LF",
                "LCF",
                "RCF",
                "RF",
                "MF",
                "SUB"
            )
        )
    }

    protected fun addPlayers() {
        var i = 1
        teamType.getValidPositions(strategy).forEach {
            players.add(
                PlayerWithPosition(
                    playerName = "t${i}",
                    playerSex = 0,
                    shirtNumber = i,
                    licenseNumber = i.toLong(),
                    teamId = 1,
                    image = null,
                    position = it.id,
                    x = 0f, y = 0f,
                    flags = PlayerFieldPosition.FLAG_NONE,
                    order = batterSize - i + 1,
                    fieldPositionID = i.toLong(),
                    playerID = i.toLong(),
                    lineupId = 1,
                    playerPositions = 1
                )
            )
            i++
        }
    }

    protected fun addSubstitutes(order: Int = 0) {
        val fromIndex = players.size + 1
        for (j in fromIndex..fromIndex + 9) {
            players.add(
                PlayerWithPosition(
                    playerName = "t${j}",
                    playerSex = 0,
                    shirtNumber = j,
                    licenseNumber = j.toLong(),
                    teamId = 1,
                    image = null,
                    position = FieldPosition.SUBSTITUTE.id,
                    x = 0f, y = 0f,
                    flags = PlayerFieldPosition.FLAG_NONE,
                    order = order,
                    fieldPositionID = j.toLong(),
                    playerID = j.toLong(),
                    lineupId = 1,
                    playerPositions = 1
                )
            )
        }
    }

    protected fun addExtraHitters(extraHitters: Int) {
        val fromIndex = players.size + 1
        for (j in fromIndex until fromIndex + extraHitters) {
            players.add(
                PlayerWithPosition(
                    playerName = "t${j}",
                    playerSex = 0,
                    shirtNumber = j,
                    licenseNumber = j.toLong(),
                    teamId = 1,
                    image = null,
                    position = FieldPosition.SUBSTITUTE.id,
                    x = 0f, y = 0f,
                    flags = PlayerFieldPosition.FLAG_NONE,
                    order = j,
                    fieldPositionID = j.toLong(),
                    playerID = j.toLong(),
                    lineupId = 1,
                    playerPositions = 1
                )
            )
        }
    }

    protected fun applyUserCase() {
        mGetBattersState.executeUseCase(
            GetBattersState.RequestValues(
                context,
                players,
                teamType.id,
                batterSize,
                extraHittersSize,
                false,
                isEditable
            )
        )
            .subscribe(observer)
        observer.await()
    }

    @Test
    fun shouldFilterPlayersWithOrder0() {
        addPlayers()
        addSubstitutes()
        applyUserCase()
        Assert.assertEquals(batterSize, observer.values()[0].players.count())
        Assert.assertEquals(0, observer.values()[0].players.filter { it.playerOrder == 0 }.count())
    }
}