/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import android.content.Context
import android.content.res.Resources
import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
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

// /////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////// EDITABLE ///////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////////

@RunWith(MockitoJUnitRunner.Silent::class)
internal abstract class GetBattersStateEditableTests : GetBattersStateTests() {

    override fun init() {
        isEditable = true
        super.init()
    }

    @Test
    fun dpShouldOnlyShowIndexAndShowDescriptionAndCanMoveInExtraHitterWhenLineupEditable() {
        addPlayers()
        addExtraHitters(extraHittersSize)
        players[0].position = FieldPosition.DP_DH.id
        // switch order to be sure dp will be at the end of the list because it's sorted by order
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
    fun flexShouldHaveBackgroundAndShowPositionInExtraHitterWhenLineupEditable() {
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
            applyBackground = true,
            isEditable = isEditable
        )

        Assert.assertEquals(
            1,
            observer.values()[0].players.filter {
                it.playerFlag == PlayerFieldPosition.FLAG_FLEX
            }.size
        )
        Assert.assertEquals(
            expected,
            observer.values()[0].players.first { it.playerFlag == PlayerFieldPosition.FLAG_FLEX })
    }

    @Test
    fun defensePlayerShouldOnlyShowIndexAndShowPositionAndCanMoveWhenLineupEditable() {
        addPlayers()

        applyUserCase()

        var i = 1
        observer.values()[0].players.forEach { batterState ->
            val value = batterSize - i + 1
            // we don't test here the field position and desc, so let's use the same as the loop
            // object
            val expected = BatterState(
                value.toLong(),
                PlayerFieldPosition.FLAG_NONE,
                i,
                "t$value",
                value.toString(),
                batterState.playerPosition,
                playerPositionDesc = batterState.playerPositionDesc,
                canShowPosition = true,
                canMove = true,
                canShowDescription = false,
                canShowOrder = true,
                applyBackground = false,
                isEditable = isEditable
            )
            Assert.assertEquals(expected, batterState)
            i++
        }
    }

    @Test
    fun substituteShouldInExtraHittersBoundsToBatWhenLineupEditable() {
        addPlayers()
        addExtraHitters(extraHittersSize)
        addSubstitutes()

        applyUserCase()

        var i = batterSize + 1
        observer.values()[0].players.filter { it.playerPosition == FieldPosition.SUBSTITUTE }
            .forEach { batterState ->
                // we don't test here the field position and desc, so let's use the same as the loop
                // object
                val subAllowBatting = i <= batterSize + extraHittersSize
                val expected = BatterState(
                    i.toLong(),
                    PlayerFieldPosition.FLAG_NONE,
                    i,
                    "t$i",
                    i.toString(),
                    FieldPosition.SUBSTITUTE,
                    playerPositionDesc = "SUB",
                    canShowPosition = false,
                    canMove = subAllowBatting,
                    canShowDescription = true,
                    canShowOrder = subAllowBatting,
                    applyBackground = false,
                    isEditable = isEditable
                )
                Assert.assertEquals(expected, batterState)
                i++
            }
    }

    @Test
    fun substituteShouldNotBatIfIndexGreaterThanExtraHittersSizeWhenLineupEditable() {
        addExtraHitters(extraHittersSize)
        addSubstitutes()

        applyUserCase()

        var i = 1
        observer.values()[0].players.filter { it.playerPosition == FieldPosition.SUBSTITUTE }
            .forEach { batterState ->
                // we don't test here the field position and desc, so let's use the same as the loop
                // object
                val subAllowBatting = i <= extraHittersSize
                val expected = BatterState(
                    i.toLong(),
                    PlayerFieldPosition.FLAG_NONE,
                    i,
                    "t$i",
                    i.toString(),
                    FieldPosition.SUBSTITUTE,
                    playerPositionDesc = "SUB",
                    canShowPosition = false,
                    canMove = subAllowBatting,
                    canShowDescription = true,
                    canShowOrder = subAllowBatting,
                    applyBackground = false,
                    isEditable = isEditable
                )
                Assert.assertEquals(expected, batterState)
                i++
            }
    }

    @Test
    fun shouldNotShowSubstitutesOrderIfCannotMoveWhenLineupEditable() {
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
                // we don't test here the field position and desc, so let's use the same as the loop
                // object
                val subAllowBatting = i <= batterSize + extraHittersSize
                val expected = BatterState(
                    i.toLong(),
                    PlayerFieldPosition.FLAG_NONE,
                    if (subAllowBatting) i else Constants.SUBSTITUTE_ORDER_VALUE,
                    "t$i",
                    i.toString(),
                    FieldPosition.SUBSTITUTE,
                    playerPositionDesc = "SUB",
                    canShowPosition = false,
                    canMove = subAllowBatting,
                    canShowDescription = true,
                    canShowOrder = subAllowBatting,
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
internal class GetBattersStateBaseballStandardEditableTests : GetBattersStateEditableTests() {
    override fun initParameters() {
        teamType = TeamType.BASEBALL
        strategy = TeamStrategy.STANDARD
        extraHittersSize = 0
    }
}

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateSoftballStandardEditableTests : GetBattersStateEditableTests() {
    override fun initParameters() {
        teamType = TeamType.SOFTBALL
        strategy = TeamStrategy.STANDARD
        extraHittersSize = 0
    }
}

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateBaseball5ManEditableTests : GetBattersStateEditableTests() {
    override fun initParameters() {
        teamType = TeamType.BASEBALL
        strategy = TeamStrategy.FIVE_MAN_STANDARD
        extraHittersSize = 0
    }
}

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateSoftballSlowpitchEditableTests : GetBattersStateEditableTests() {
    override fun initParameters() {
        teamType = TeamType.SOFTBALL
        strategy = TeamStrategy.SLOWPITCH
        extraHittersSize = 0
    }
}

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateSoftball5ManEditableTests : GetBattersStateEditableTests() {
    override fun initParameters() {
        teamType = TeamType.SOFTBALL
        strategy = TeamStrategy.FIVE_MAN_STANDARD
        extraHittersSize = 0
    }
}

////////////// EDITABLE CUSTOM HITTER SIZE //////////////

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateBaseballCustomStandardEditableTests : GetBattersStateEditableTests() {
    override fun initParameters() {
        teamType = TeamType.BASEBALL
        strategy = TeamStrategy.STANDARD
        extraHittersSize = 3
    }
}

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateSoftballCustomStandardEditableTests : GetBattersStateEditableTests() {
    override fun initParameters() {
        teamType = TeamType.SOFTBALL
        strategy = TeamStrategy.STANDARD
        extraHittersSize = 3
    }
}

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateBaseballCustom5ManEditableTests : GetBattersStateEditableTests() {
    override fun initParameters() {
        teamType = TeamType.BASEBALL
        strategy = TeamStrategy.FIVE_MAN_STANDARD
        extraHittersSize = 3
    }
}

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateSoftballCustomSlowpitchEditableTests :
    GetBattersStateEditableTests() {
    override fun initParameters() {
        teamType = TeamType.SOFTBALL
        strategy = TeamStrategy.SLOWPITCH
        extraHittersSize = 3
    }
}

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateSoftballCustom5ManEditableTests : GetBattersStateEditableTests() {
    override fun initParameters() {
        teamType = TeamType.SOFTBALL
        strategy = TeamStrategy.FIVE_MAN_STANDARD
        extraHittersSize = 3
    }
}

// /////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////// NOT EDITABLE //////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////////

@RunWith(MockitoJUnitRunner.Silent::class)
internal abstract class GetBattersStateNotEditableTests : GetBattersStateTests() {

    override fun init() {
        isEditable = false
        super.init()
    }

    @Test
    fun dpShouldOnlyShowIndexAndShowDescriptionWhenLineupNotEditable() {
        addPlayers()
        addExtraHitters(extraHittersSize)
        players[0].position = FieldPosition.DP_DH.id
        // switch order to be sure dp will be at the end of the list because it's sorted by order
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
    fun flexShouldOnlyHaveBackgroundAndShowPositionShowDescriptionWhenLineupNotEditable() {
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
            applyBackground = true,
            isEditable = isEditable
        )

        Assert.assertEquals(
            1,
            observer.values()[0].players.filter {
                it.playerFlag == PlayerFieldPosition.FLAG_FLEX
            }.size
        )
        Assert.assertEquals(
            expected,
            observer.values()[0].players.first { it.playerFlag == PlayerFieldPosition.FLAG_FLEX })
    }

    @Test
    fun defensePlayerShouldOnlyShowIndexAndShowPositionShowDescriptionWhenLineupEditable() {
        addPlayers()

        applyUserCase()

        var i = 1
        observer.values()[0].players.forEach { batterState ->
            val value = batterSize - i + 1
            // we don't test here the field position and desc, so let's use the same as the loop
            // object
            val expected = BatterState(
                value.toLong(),
                PlayerFieldPosition.FLAG_NONE,
                i,
                "t$value",
                value.toString(),
                batterState.playerPosition,
                playerPositionDesc = batterState.playerPositionDesc,
                canShowPosition = true,
                canMove = false,
                canShowDescription = true,
                canShowOrder = true,
                applyBackground = false,
                isEditable = isEditable
            )
            Assert.assertEquals(expected, batterState)
            i++
        }
    }

    @Test
    fun substituteShouldInExtraHittersBoundsToBatWhenLineupEditable() {
        addPlayers()
        addExtraHitters(extraHittersSize)
        addSubstitutes()

        applyUserCase()

        var i = batterSize + 1
        observer.values()[0].players.filter { it.playerPosition == FieldPosition.SUBSTITUTE }
            .forEach { batterState ->
                // we don't test here the field position and desc, so let's use the same as the loop
                // object
                val subAllowBatting = i <= batterSize + extraHittersSize
                val expected = BatterState(
                    i.toLong(),
                    PlayerFieldPosition.FLAG_NONE,
                    i,
                    "t$i",
                    i.toString(),
                    FieldPosition.SUBSTITUTE,
                    playerPositionDesc = "SUB",
                    canShowPosition = false,
                    canMove = false,
                    canShowDescription = true,
                    canShowOrder = subAllowBatting,
                    applyBackground = false,
                    isEditable = isEditable
                )
                Assert.assertEquals(expected, batterState)
                i++
            }
    }

    @Test
    fun substituteShouldNotBatIfIndexGreaterThanExtraHittersSizeWhenLineupEditable() {
        addExtraHitters(extraHittersSize)
        addSubstitutes()

        applyUserCase()

        var i = 1
        observer.values()[0].players.filter { it.playerPosition == FieldPosition.SUBSTITUTE }
            .forEach { batterState ->
                // we don't test here the field position and desc, so let's use the same as the loop
                // object
                val subAllowBatting = i <= extraHittersSize
                val expected = BatterState(
                    i.toLong(),
                    PlayerFieldPosition.FLAG_NONE,
                    i,
                    "t$i",
                    i.toString(),
                    FieldPosition.SUBSTITUTE,
                    playerPositionDesc = "SUB",
                    canShowPosition = false,
                    canMove = false,
                    canShowDescription = true,
                    canShowOrder = subAllowBatting,
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
                // we don't test here the field position and desc, so let's use the same as the loop
                // object
                val subAllowBatting = i <= batterSize + extraHittersSize
                val expected = BatterState(
                    i.toLong(),
                    PlayerFieldPosition.FLAG_NONE,
                    if (subAllowBatting) i else Constants.SUBSTITUTE_ORDER_VALUE,
                    "t$i",
                    i.toString(),
                    FieldPosition.SUBSTITUTE,
                    playerPositionDesc = "SUB",
                    canShowPosition = false,
                    canMove = false,
                    canShowDescription = true,
                    canShowOrder = subAllowBatting,
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
internal class GetBattersStateBaseballStandardNotEditableTests : GetBattersStateNotEditableTests() {
    override fun initParameters() {
        teamType = TeamType.BASEBALL
        strategy = TeamStrategy.STANDARD
        extraHittersSize = 0
    }
}

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateSoftballStandardNotEditableTests : GetBattersStateNotEditableTests() {
    override fun initParameters() {
        teamType = TeamType.SOFTBALL
        strategy = TeamStrategy.STANDARD
        extraHittersSize = 0
    }
}

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateBaseball5ManNotEditableTests : GetBattersStateNotEditableTests() {
    override fun initParameters() {
        teamType = TeamType.BASEBALL
        strategy = TeamStrategy.FIVE_MAN_STANDARD
        extraHittersSize = 0
    }
}

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateSoftballSlowpitchNotEditableTests :
    GetBattersStateNotEditableTests() {
    override fun initParameters() {
        teamType = TeamType.SOFTBALL
        strategy = TeamStrategy.SLOWPITCH
        extraHittersSize = 0
    }
}

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateSoftball5ManNotEditableTests : GetBattersStateNotEditableTests() {
    override fun initParameters() {
        teamType = TeamType.SOFTBALL
        strategy = TeamStrategy.FIVE_MAN_STANDARD
        extraHittersSize = 0
    }
}

////////////// NOT EDITABLE CUSTOM HITTER SIZE //////////////

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateBaseballCustomStandardNotEditableTests :
    GetBattersStateNotEditableTests() {
    override fun initParameters() {
        teamType = TeamType.BASEBALL
        strategy = TeamStrategy.STANDARD
        extraHittersSize = 3
    }
}

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateSoftballCustomStandardNotEditableTests :
    GetBattersStateNotEditableTests() {
    override fun initParameters() {
        teamType = TeamType.SOFTBALL
        strategy = TeamStrategy.STANDARD
        extraHittersSize = 3
    }
}

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateBaseballCustom5ManNotEditableTests :
    GetBattersStateNotEditableTests() {
    override fun initParameters() {
        teamType = TeamType.BASEBALL
        strategy = TeamStrategy.FIVE_MAN_STANDARD
        extraHittersSize = 3
    }
}

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateSoftballCustomSlowpitchNotEditableTests :
    GetBattersStateNotEditableTests() {
    override fun initParameters() {
        teamType = TeamType.SOFTBALL
        strategy = TeamStrategy.SLOWPITCH
        extraHittersSize = 3
    }
}

@RunWith(MockitoJUnitRunner.Silent::class)
internal class GetBattersStateSoftballCustom5ManNotEditableTests :
    GetBattersStateNotEditableTests() {
    override fun initParameters() {
        teamType = TeamType.SOFTBALL
        strategy = TeamStrategy.FIVE_MAN_STANDARD
        extraHittersSize = 3
    }
}

// /////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////// BASE ////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * @property teamType
 * @property strategy
 * @property batterSize
 * @property extraHittersSize
 * @property isEditable
 */
internal abstract class GetBattersStateTests {

    protected lateinit var teamType: TeamType
    protected lateinit var strategy: TeamStrategy
    protected var batterSize: Int = 0
    protected var extraHittersSize: Int = 0
    protected var isEditable: Boolean = false

    protected val players: MutableList<PlayerWithPosition> = mutableListOf()
    val lineupMode = MODE_ENABLED
    val observer: TestObserver<GetBattersState.ResponseValue> = TestObserver()

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var resources: Resources
    lateinit var getBattersState: GetBattersState

    protected abstract fun initParameters()

    @Before
    open fun init() {
        initParameters()
        MockitoAnnotations.initMocks(this)
        getBattersState = GetBattersState()
        batterSize = strategy.batterSize

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
                    playerName = "t$i",
                    playerSex = 0,
                    shirtNumber = i,
                    licenseNumber = i.toLong(),
                    teamId = 1,
                    image = null,
                    position = it.id,
                    x = 0f, y = 0f,
                    flags = PlayerFieldPosition.FLAG_NONE,
                    order = batterSize - i + 1,
                    fieldPositionId = i.toLong(),
                    playerId = i.toLong(),
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
                    playerName = "t$j",
                    playerSex = 0,
                    shirtNumber = j,
                    licenseNumber = j.toLong(),
                    teamId = 1,
                    image = null,
                    position = FieldPosition.SUBSTITUTE.id,
                    x = 0f, y = 0f,
                    flags = PlayerFieldPosition.FLAG_NONE,
                    order = order,
                    fieldPositionId = j.toLong(),
                    playerId = j.toLong(),
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
                    playerName = "t$j",
                    playerSex = 0,
                    shirtNumber = j,
                    licenseNumber = j.toLong(),
                    teamId = 1,
                    image = null,
                    position = FieldPosition.SUBSTITUTE.id,
                    x = 0f, y = 0f,
                    flags = PlayerFieldPosition.FLAG_NONE,
                    order = j,
                    fieldPositionId = j.toLong(),
                    playerId = j.toLong(),
                    lineupId = 1,
                    playerPositions = 1
                )
            )
        }
    }

    protected fun applyUserCase() {
        getBattersState.executeUseCase(
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
