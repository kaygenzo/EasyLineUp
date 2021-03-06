package com.telen.easylineup.domain.application

import android.net.Uri
import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.model.export.ExportBase
import io.reactivex.Completable
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
    fun getTiles(): LiveData<List<DashboardTile>>
    fun observePlayerNumberOverlays(lineupID: Long): LiveData<List<PlayerNumberOverlay>>

    ///////////////////////////////
    ///////////// Rx //////////////
    ///////////////////////////////

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
    fun savePlayer(playerID: Long?, name: String?, shirtNumber: Int?, licenseNumber: Long?, imageUri: Uri?, positions: Int, pitching: Int, batting: Int, email: String?, phone: String?): Completable
    fun deletePlayer(playerID: Long?): Completable
    fun getPlayers(): Single<List<Player>>
    fun getNotSelectedPlayersFromList(list: List<PlayerWithPosition>, lineupID: Long?, sortBy: FieldPosition? = null): Single<List<Player>>
    fun getPlayersInFieldFromList(list: List<PlayerWithPosition>): Single<List<Player>>
    fun saveOrUpdatePlayerNumberOverlays(overlays: List<RosterItem>): Completable
    fun getShirtNumberHistory(number: Int): Single<List<ShirtNumberEntry>>
    /** @deprecated **/fun insertPlayerNumberOverlays(overlays: List<PlayerNumberOverlay>): Completable
    fun getTeamEmails(): Single<List<String>>
    fun getTeamPhones(): Single<List<String>>

    //lineups
    /** @deprecated **/ fun insertLineups(lineups: List<Lineup>): Completable
    fun getCompleteRoster(): Single<TeamRosterSummary>
    fun getRoster(lineupID: Long): Single<TeamRosterSummary>
    fun updateRoster(lineupID: Long, roster: List<RosterPlayerStatus>): Completable
    fun saveLineup(tournament: Tournament, lineupTitle: String, rosterFilter: TeamRosterSummary, lineupEventTime: Long, strategy: TeamStrategy, extraHittersSize: Int): Single<Long>
    fun deleteLineup(lineupID: Long?): Completable
    fun updateLineupMode(isEnabled: Boolean, lineupID: Long?, lineupMode: Int, list: List<PlayerWithPosition>, strategy: TeamStrategy, extraHittersSize: Int): Completable

    //player field positions
    /** @deprecated **/ fun insertPlayerFieldPositions(playerFieldPositions: List<PlayerFieldPosition>): Completable
    fun savePlayerFieldPosition(player: Player, position: FieldPosition, list: List<PlayerWithPosition>, lineupID: Long?,
                                lineupMode: Int, strategy: TeamStrategy, batterSize: Int, extraBatterSize: Int): Completable
    fun deletePlayerPosition(player: Player, position: FieldPosition, list: List<PlayerWithPosition>, lineupMode: Int, extraHitterSize: Int): Completable
    fun saveBattingOrder(players: List<PlayerWithPosition>): Completable
    fun switchPlayersPosition(p1: FieldPosition, p2: FieldPosition, list: List<PlayerWithPosition>, lineupMode: Int, strategy: TeamStrategy, extraHittersSize: Int): Completable
    fun getDpAndFlexFromPlayersInField(list: List<PlayerWithPosition>): Single<DpAndFlexConfiguration>
    fun linkDpAndFlex(dp: Player?, flex: Player?, lineupID: Long?, list: List<PlayerWithPosition>, strategy: TeamStrategy, extraHittersSize: Int): Completable
    fun getBatterStates(players: List<PlayerWithPosition>, teamType: Int, batterSize: Int, extraHitterSize: Int, lineupMode: Int, isDebug: Boolean,isEditable: Boolean): Single<List<BatterState>>

    //tournaments
    fun getTournaments(): Single<List<Tournament>>
    /** @deprecated **/ fun insertTournaments(tournaments: List<Tournament>): Completable
    fun deleteTournament(tournament: Tournament) : Completable
    fun getCategorizedLineups(filter: String): Single<List<Pair<Tournament, List<Lineup>>>>
    fun getPlayersPositionForTournament(tournament: Tournament, strategy: TeamStrategy): Single<TournamentStatsUIConfig>

    //data
    fun importData(root: ExportBase, updateIfExists: Boolean): Completable
    fun deleteAllData(): Completable
    fun exportDataOnExternalMemory(name: String, fallbackName: String): Single<String>

    fun generateMockedData(): Completable

    //tiles
    fun updateTiles(tiles: List<DashboardTile>): Completable
}