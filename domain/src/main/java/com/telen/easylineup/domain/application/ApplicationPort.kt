package com.telen.easylineup.domain.application

import android.net.Uri
import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.model.tiles.ITileData
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject

interface ApplicationPort {

    fun observeErrors(): PublishSubject<DomainErrors>

    ///////////////////////////////
    ////////// LiveData ///////////
    ///////////////////////////////

    fun observeTeams(): LiveData<List<Team>>
    fun observeLineupById(id: Long): LiveData<Lineup>
    fun observeTeamPlayersAndMaybePositionsForLineup(id: Long): LiveData<List<PlayerWithPosition>>

    ///////////////////////////////
    ///////////// Rx //////////////
    ///////////////////////////////

    //dashboard
    fun getTeamSize(team: Team): Maybe<ITileData>
    fun getMostUsedPlayer(team: Team): Maybe<ITileData>

    //teams
    fun getTeam(): Single<Team>
    fun getAllTeams(): Single<List<Team>>
    fun getTeamsCount(): Single<Int>
    /** @deprecated **/ fun insertTeam(team: Team): Single<Long>
    fun updateCurrentTeam(currentTeam: Team): Completable
    fun saveTeam(team: Team): Completable
    fun getTeamType(): Single<Int>
    fun deleteTeam(team: Team): Completable
    fun getTeamCreationNextStep(currentStep: Int, team: Team): Single<StepConfiguration>
    fun getTeamCreationPreviousStep(currentStep: Int, team: Team): Single<StepConfiguration>

    //players
    /** @deprecated **/ fun insertPlayers(players: List<Player>): Completable
    fun getPlayer(playerID: Long?): Single<Player>
    fun getPlayerPositionsSummary(playerID: Long?): Single<Map<FieldPosition, Int>>
    fun savePlayer(playerID: Long?, name: String?, shirtNumber: Int?, licenseNumber: Long?, imageUri: Uri?, positions: Int, pitching: Int, batting: Int): Completable
    fun deletePlayer(playerID: Long?): Completable
    fun getPlayers(): Single<List<Player>>
    fun getNotSelectedPlayersFromList(list: List<PlayerWithPosition>, lineupID: Long?, sortBy: FieldPosition? = null): Single<List<Player>>
    fun getPlayersInFieldFromList(list: List<PlayerWithPosition>): Single<List<Player>>

    //lineups
    /** @deprecated **/ fun insertLineups(lineups: List<Lineup>): Completable
    fun getRoster(): Single<TeamRosterSummary>
    fun saveLineup(tournament: Tournament, lineupTitle: String): Single<Long>
    fun deleteLineup(lineupID: Long?): Completable
    fun updateLineupMode(isEnabled: Boolean, lineupID: Long?, lineupMode: Int, list: List<PlayerWithPosition>): Completable

    //player field positions
    /** @deprecated **/ fun insertPlayerFieldPositions(playerFieldPositions: List<PlayerFieldPosition>): Completable
    fun savePlayerFieldPosition(player: Player, position: FieldPosition, list: List<PlayerWithPosition>, lineupID: Long?, lineupMode: Int): Completable
    fun deletePlayerPosition(player: Player, position: FieldPosition, list: List<PlayerWithPosition>, lineupMode: Int): Completable
    fun saveBattingOrder(players: List<PlayerWithPosition>): Completable
    fun switchPlayersPosition(p1: FieldPosition, p2: FieldPosition, list: List<PlayerWithPosition>, lineupMode: Int): Completable
    fun getDpAndFlexFromPlayersInField(list: List<PlayerWithPosition>): Single<DpAndFlexConfiguration>
    fun linkDpAndFlex(dp: Player?, flex: Player?, lineupID: Long?, list: List<PlayerWithPosition>): Completable

    //tournaments
    fun getTournaments(): Single<List<Tournament>>
    /** @deprecated **/ fun insertTournaments(tournaments: List<Tournament>): Completable
    fun deleteTournament(tournament: Tournament) : Completable
    fun getCategorizedLineups(filter: String): Single<List<Pair<Tournament, List<Lineup>>>>
    fun getPlayersPositionForTournament(tournament: Tournament): Single<TournamentStatsUIConfig>

    //data
    fun importData(root: ExportBase, updateIfExists: Boolean): Completable
    fun deleteAllData(): Completable
    fun exportDataOnExternalMemory(name: String, fallbackName: String): Single<String>

    fun generateMockedData(): Completable
}