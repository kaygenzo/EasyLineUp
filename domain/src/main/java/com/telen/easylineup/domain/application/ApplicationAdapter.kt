package com.telen.easylineup.domain.application

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.URLUtil
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.UseCaseHandler
import com.telen.easylineup.domain.repository.*
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.model.tiles.ITileData
import com.telen.easylineup.domain.usecases.*
import com.telen.easylineup.domain.usecases.exceptions.*
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.functions.Consumer
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

internal class ApplicationAdapter(private val _errors: MutableLiveData<DomainErrors> = MutableLiveData<DomainErrors>()): ApplicationPort, KoinComponent {

    private val context: Context by inject()

    private val teamsRepo: TeamRepository by inject()
    private val playersRepo: PlayerRepository by inject()
    private val tournamentsRepo: TournamentRepository by inject()
    private val lineupsRepo: LineupRepository by inject()
    private val playerFieldPositionRepo: PlayerFieldPositionRepository by inject()

    private val getTeamUseCase: GetTeam by inject()
    private val getAllTeamsUseCase: GetAllTeams by inject()
    private val saveCurrentTeam: SaveCurrentTeam by inject()
    private val getTeamSizeUseCase:  GetTeamSize by inject()
    private val getMostUsedPlayerUseCase: GetMostUsedPlayer by inject()
    private val createLineupUseCase: CreateLineup by inject()
    private val deleteTournamentUseCase: DeleteTournament by inject()
    private val getAllTournamentsWithLineupsUseCase: GetAllTournamentsWithLineups by inject()
    private val getTournamentsUseCase: GetTournaments by inject()
    private val getRosterUseCase: GetRoster by inject()
    private val tableDataUseCase: GetTournamentStatsForPositionTable by inject()
    private val mImporterUseCase: ImportData by inject()
    private val exportDataUseCase: ExportData by inject()
    private val getPlayerUseCase: GetPlayer by inject()
    private val deletePlayerUseCase: DeletePlayer by inject()
    private val savePlayerUseCase: SavePlayer by inject()
    private val getPlayerPositionsSummaryUseCase: GetPositionsSummaryForPlayer by inject()
    private val deleteAllDataUseCase: DeleteAllData by inject()
    private val checkHashUseCase: CheckHashData by inject()
    private val getPlayersUseCase: GetPlayers by inject()
    private val deleteTeamUseCase: DeleteTeam by inject()
    private val saveTeamUseCase: SaveTeam by inject()
    private val checkTeamUseCase: CheckTeam by inject()
    private val getTeamCreationNextStep: GetTeamCreationNextStep by inject()
    private val getTeamCreationPreviousStep: GetTeamCreationPreviousStep by inject()

    private val savePlayerFieldPositionUseCase: SavePlayerFieldPosition by inject()
    private val deletePlayerFieldPositionUseCase: DeletePlayerFieldPosition by inject()
    private val getListAvailablePlayersForLineup: GetListAvailablePlayersForSelection by inject()
    private val saveBattingOrder: SaveBattingOrder by inject()
    private val deleteLineup: DeleteLineup by inject()
    private val saveLineupMode: SaveLineupMode by inject()
    private val updatePlayersWithLineupMode: UpdatePlayersWithLineupMode by inject()
    private val switchPlayersPositionUseCase: SwitchPlayersPosition by inject()
    private val getPlayersInField: GetOnlyPlayersInField by inject()
    private val getDpAndFlexFromPlayersInFieldUseCase: GetDPAndFlexFromPlayersInField by inject()
    private val saveDpAndFlexUseCase: SaveDpAndFlex by inject()

    override fun observeErrors(): LiveData<DomainErrors> {
        return _errors
    }

    //teams

    override fun observeTeams(): LiveData<List<Team>> {
        return teamsRepo.getTeams()
    }

    //lineups

    override fun observeLineupById(id: Long): LiveData<Lineup> {
        return lineupsRepo.getLineupById(id)
    }

    //players

    override fun observeTeamPlayersAndMaybePositionsForLineup(id: Long): LiveData<List<PlayerWithPosition>> {
        return playersRepo.getTeamPlayersAndMaybePositions(id)
    }

    override fun getTeam(): Single<Team> {
        return UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues())
                .map {
                    it.team
                }
                .doOnError {
                    _errors.postValue(DomainErrors.GET_TEAM_FAILED)
                }
    }

    override fun getAllTeams(): Single<List<Team>> {
        return UseCaseHandler.execute(getAllTeamsUseCase, GetAllTeams.RequestValues()).map { it.teams }
    }

    override fun getTeamsCount(): Single<Int> {
        return getAllTeams().map { it.size }
    }

    override fun insertTeam(team: Team): Single<Long> {
        return teamsRepo.insertTeam(team)
    }

    override fun updateCurrentTeam(currentTeam: Team): Completable {
        return UseCaseHandler.execute(saveCurrentTeam, SaveCurrentTeam.RequestValues(currentTeam)).ignoreElement()
    }

    override fun saveTeam(team: Team): Completable {
        return UseCaseHandler.execute(checkTeamUseCase, CheckTeam.RequestValues(team)).ignoreElement()
                .andThen(UseCaseHandler.execute(saveTeamUseCase, SaveTeam.RequestValues(team)).map { it.team })
                .flatMapCompletable { UseCaseHandler.execute(saveCurrentTeam, SaveCurrentTeam.RequestValues(it)).ignoreElement() }
    }

    override fun getTeamType(): Single<Int> {
        return UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues())
                .map { it.team.type }
    }

    override fun deleteTeam(team: Team): Completable {
        return UseCaseHandler.execute(deleteTeamUseCase, DeleteTeam.RequestValues(team)).ignoreElement()
    }

    override fun getTeamCreationNextStep(currentStep: Int, team: Team): Single<StepConfiguration> {
        val requestValue = GetTeamCreationStep.RequestValues(
                TeamCreationStep.getStepById(currentStep) ?: TeamCreationStep.TEAM)
        return UseCaseHandler.execute(checkTeamUseCase, CheckTeam.RequestValues(team)).ignoreElement()
                .andThen(UseCaseHandler.execute(getTeamCreationNextStep, requestValue))
                .map { it.config }
    }

    override fun getTeamCreationPreviousStep(currentStep: Int, team: Team): Single<StepConfiguration> {
        val requestValue = GetTeamCreationStep.RequestValues(
                TeamCreationStep.getStepById(currentStep) ?: TeamCreationStep.TEAM)

        return UseCaseHandler.execute(checkTeamUseCase, CheckTeam.RequestValues(team)).ignoreElement()
                .onErrorResumeNext {
                    if(it is NameEmptyException && currentStep == TeamCreationStep.TEAM.id) {
                        Completable.complete()
                    }
                    else {
                        Completable.error(it)
                    }
                }
                .andThen(UseCaseHandler.execute(getTeamCreationPreviousStep, requestValue))
                .map { it.config }
    }

    override fun getTeamSize(team: Team): Maybe<ITileData> {
        return UseCaseHandler.execute(getTeamSizeUseCase, GetTeamSize.RequestValues(team)).flatMapMaybe { Maybe.just(it.data) }
    }

    override fun getMostUsedPlayer(team: Team): Maybe<ITileData> {
        return UseCaseHandler.execute(getMostUsedPlayerUseCase, GetMostUsedPlayer.RequestValues(team))
                .flatMapMaybe { it.data?.let { iData -> Maybe.just(iData) } ?: Maybe.empty() }
    }

    override fun insertPlayers(players: List<Player>): Completable {
        return playersRepo.insertPlayers(players)
    }

    override fun getPlayer(playerID: Long?): Single<Player> {
        return UseCaseHandler.execute(getPlayerUseCase, GetPlayer.RequestValues(playerID))
                .map { it.player }
                .doOnError {
                    if(it is NotExistingPlayer) {
                        _errors.postValue(DomainErrors.INVALID_PLAYER_ID)
                    }
                }
    }

    override fun getPlayerPositionsSummary(playerID: Long?): Single<Map<FieldPosition, Int>> {
        return UseCaseHandler.execute(getPlayerPositionsSummaryUseCase, GetPositionsSummaryForPlayer.RequestValues(playerID))
                .map { it.summary }
    }

    override fun savePlayer(playerID: Long?, name: String?, shirtNumber: Int?, licenseNumber: Long?, imageUri: Uri?, positions: Int): Completable {
        return UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues()).map { it.team }
                .flatMapCompletable {
                    val req = SavePlayer.RequestValues(playerID ?: 0, it.id, name, shirtNumber, licenseNumber, imageUri, positions)
                    UseCaseHandler.execute(savePlayerUseCase, req).ignoreElement()
                }
                .doOnError {
                    when (it) {
                        is NameEmptyException -> _errors.postValue(DomainErrors.INVALID_PLAYER_NAME)
                        is ShirtNumberEmptyException -> _errors.postValue(DomainErrors.INVALID_PLAYER_NUMBER)
                        is LicenseNumberEmptyException -> _errors.postValue(DomainErrors.INVALID_PLAYER_LICENSE)
                    }
                }
    }

    override fun deletePlayer(playerID: Long?): Completable {
        return UseCaseHandler.execute(getPlayerUseCase, GetPlayer.RequestValues(playerID)).map { it.player }
                .flatMapCompletable { player -> UseCaseHandler.execute(deletePlayerUseCase, DeletePlayer.RequestValues(player)).ignoreElement() }
    }

    override fun getPlayers(): Single<List<Player>> {
        return UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues()).map { it.team }
                .flatMap { team -> UseCaseHandler.execute(getPlayersUseCase, GetPlayers.RequestValues(team.id)).map { it.players } }
    }

    override fun getNotSelectedPlayersFromList(list: List<PlayerWithPosition>, lineupID: Long?, sortBy: FieldPosition?): Single<List<Player>> {
        return getTeam()
                .flatMap { UseCaseHandler.execute(getRosterUseCase, GetRoster.RequestValues(it.id, lineupID)) }
                .flatMap {
                    val requestValues = GetListAvailablePlayersForSelection.RequestValues(list, sortBy, it.summary.players)
                    UseCaseHandler.execute(getListAvailablePlayersForLineup, requestValues)
                }
                .map { it.players }
                .doOnError {
                    _errors.postValue(DomainErrors.LIST_AVAILABLE_PLAYERS_EMPTY)
                }
    }

    override fun getPlayersInFieldFromList(list: List<PlayerWithPosition>): Single<List<Player>> {
        return UseCaseHandler.execute(getPlayersInField, GetOnlyPlayersInField.RequestValues(list)).map { it.playersInField }
                .doOnError {
                    _errors.postValue(DomainErrors.LIST_AVAILABLE_PLAYERS_EMPTY)
                }
    }

    override fun insertLineups(lineups: List<Lineup>): Completable {
        return lineupsRepo.insertLineups(lineups)
    }

    override fun getRoster(): Single<TeamRosterSummary> {
        return UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues()).map { it.team }
                .flatMap { UseCaseHandler.execute(getRosterUseCase, GetRoster.RequestValues(it.id, null)) }
                .map { it.summary }
    }

    override fun saveLineup(tournament: Tournament, lineupTitle: String): Single<Long> {
        return UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues()).map { it.team }
                .flatMap { team ->
                    getRoster().map { it.players }.flatMap { roster ->
                        UseCaseHandler.execute(createLineupUseCase, CreateLineup.RequestValues(team.id, tournament, lineupTitle, roster))
                    }
                }
                .map { it.lineupID }
    }

    override fun deleteLineup(lineupID: Long?): Completable {
        val requestValues = DeleteLineup.RequestValues(lineupID)
        return UseCaseHandler.execute(deleteLineup, requestValues).ignoreElement()
                .doOnError {
                    _errors.postValue(DomainErrors.DELETE_LINEUP_FAILED)
                }
    }

    override fun updateLineupMode(isEnabled: Boolean, lineupID: Long?, lineupMode: Int, list: List<PlayerWithPosition>): Completable {
        return UseCaseHandler.execute(saveLineupMode, SaveLineupMode.RequestValues(lineupID, lineupMode))
                .doOnError {
                    _errors.postValue(DomainErrors.SAVE_LINEUP_MODE_FAILED)
                }
                .flatMapCompletable {
//            eventHandler.value = SaveLineupModeSuccess
                    getTeam()
                            .flatMapCompletable {
                                val requestValues = UpdatePlayersWithLineupMode.RequestValues(list, isEnabled, it.type)
                                UseCaseHandler.execute(updatePlayersWithLineupMode, requestValues).ignoreElement()
                            }
                }
    }

    override fun insertPlayerFieldPositions(playerFieldPositions: List<PlayerFieldPosition>): Completable {
        return playerFieldPositionRepo.insertPlayerFieldPositions(playerFieldPositions)
    }

    override fun savePlayerFieldPosition(player: Player, position: FieldPosition, list: List<PlayerWithPosition>, lineupID: Long?, lineupMode: Int): Completable {
        return getTeam()
                .flatMapCompletable {
                    val requestValues = SavePlayerFieldPosition.RequestValues(
                            lineupID = lineupID,
                            player = player,
                            position = position,
                            players = list,
                            lineupMode = lineupMode,
                            teamType = it.type)

                    UseCaseHandler.execute(savePlayerFieldPositionUseCase, requestValues).ignoreElement()
                }
                .doOnError {
                    _errors.postValue(DomainErrors.SAVE_PLAYER_FIELD_POSITION_FAILED)
                }
    }

    override fun deletePlayerPosition(player: Player, position: FieldPosition, list: List<PlayerWithPosition>, lineupMode: Int): Completable {
        val requestValues = DeletePlayerFieldPosition.RequestValues(list, player, position, lineupMode)
        return UseCaseHandler.execute(deletePlayerFieldPositionUseCase, requestValues).ignoreElement()
                .doOnError {
                    _errors.postValue(DomainErrors.DELETE_PLAYER_FIELD_POSITION_FAILED)
                }
    }

    override fun saveBattingOrder(players: List<PlayerWithPosition>): Completable {
        val requestValues = SaveBattingOrder.RequestValues(players)
        return UseCaseHandler.execute(saveBattingOrder, requestValues).ignoreElement()
                .doOnError {
                    _errors.postValue(DomainErrors.SAVE_BATTING_ORDER_FAILED)
                }
    }

    override fun switchPlayersPosition(p1: FieldPosition, p2: FieldPosition, list: List<PlayerWithPosition>, lineupMode: Int): Completable {
        return getTeam()
                .flatMapCompletable {
                    UseCaseHandler.execute(switchPlayersPositionUseCase, SwitchPlayersPosition.RequestValues(
                            players = list,
                            position1 = p1,
                            position2 = p2,
                            teamType = it.type,
                            lineupMode = lineupMode
                    )).ignoreElement()
                }
    }

    override fun getDpAndFlexFromPlayersInField(list: List<PlayerWithPosition>): Single<DpAndFlexConfiguration> {
        return getTeam().flatMap {
            UseCaseHandler.execute(getDpAndFlexFromPlayersInFieldUseCase, GetDPAndFlexFromPlayersInField.RequestValues(list, it.type))
        }.map {
            it.configResult
        }
                .doOnError {
                    if(it is NeedAssignPitcherFirstException) {
                        _errors.postValue(DomainErrors.NEED_ASSIGN_PITCHER_FIRST)
                    }
                    else {
                        _errors.postValue(DomainErrors.GET_TEAM_FAILED)
                    }
                }
    }

    override fun linkDpAndFlex(dp: Player?, flex: Player?, lineupID: Long?, list: List<PlayerWithPosition>): Completable {
        return UseCaseHandler.execute(saveDpAndFlexUseCase, SaveDpAndFlex.RequestValues(
                        lineupID = lineupID, dp = dp, flex = flex, players = list
                ))
                .ignoreElement()
                .doOnError {
                    if(it is NeedAssignBothPlayersException) {
                        _errors.postValue(DomainErrors.DP_OR_FLEX_NOT_ASSIGNED)
                    }
                }
    }

    override fun getTournaments(): Single<List<Tournament>> {
        return UseCaseHandler.execute(getTournamentsUseCase, GetTournaments.RequestValues()).map { it.tournaments }
    }

    override fun insertTournaments(tournaments: List<Tournament>): Completable {
        return tournamentsRepo.insertTournaments(tournaments)
    }

    override fun deleteTournament(tournament: Tournament): Completable {
        return UseCaseHandler.execute(deleteTournamentUseCase, DeleteTournament.RequestValues(tournament)).ignoreElement()
    }

    override fun getCategorizedLineups(filter: String): Single<List<Pair<Tournament, List<Lineup>>>> {
        return UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues())
                .flatMap { UseCaseHandler.execute(getAllTournamentsWithLineupsUseCase, GetAllTournamentsWithLineups.RequestValues(filter, it.team.id)) }
                .map { it.result }
    }

    override fun getPlayersPositionForTournament(tournament: Tournament): Single<TournamentStatsUIConfig> {
        return UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues())
                .flatMap { UseCaseHandler.execute(tableDataUseCase, GetTournamentStatsForPositionTable.RequestValues(tournament, it.team, context)) }
                .map { it.uiConfig }
    }

    override fun importData(root: ExportBase, updateIfExists: Boolean): Completable {
        return UseCaseHandler.execute(mImporterUseCase, ImportData.RequestValues(root, updateIfExists)).flatMapCompletable {
            println("Inserted: ${it.inserted.contentToString()} Updated: ${it.updated.contentToString()}")
            Completable.complete()
        }
    }

    override fun deleteAllData(): Completable {
        return UseCaseHandler.execute(deleteAllDataUseCase, DeleteAllData.RequestValues()).ignoreElement()
    }

    override fun exportDataOnExternalMemory(name: String, fallbackName: String): Single<String> {
        return UseCaseHandler.execute(checkHashUseCase, CheckHashData.RequestValues()).flatMapCompletable {
                    println("update result is ${it.updateResult}")
                    Completable.complete()
                }.andThen(UseCaseHandler.execute(exportDataUseCase, ExportData.RequestValues(object : ValidationCallback {
                    override fun isNetworkUrl(url: String?): Boolean {
                        return URLUtil.isNetworkUrl(url)
                    }

                    override fun isDigitsOnly(value: String): Boolean {
                        return value.isDigitsOnly()
                    }

                    override fun isBlank(value: String): Boolean {
                        return value.isBlank()
                    }
                })))
                .flatMap {
                    val storageDirectoryName = Constants.EXPORTS_DIRECTORY
                    val json = Gson().toJson(it.exportBase)

                    val rootDirectory = File(Environment.getExternalStorageDirectory().path
                            + "/" + storageDirectoryName)
                    if(!rootDirectory.exists())
                        rootDirectory.mkdirs()

                    val fileName = if(name.isNotBlank()) name else fallbackName
                    val file = File(rootDirectory.absolutePath + "/$fileName.elu")
                    if(!file.exists())
                        file.createNewFile()

                    var out: BufferedWriter? = null
                    try {
                        out = BufferedWriter(FileWriter(file.absolutePath, false))
                        out.write(json)
                        out.flush()
                    }
                    catch (e: Exception) {
                        println(e)
                    }
                    finally {
                        out?.close()
                    }
                    Single.just(storageDirectoryName)
                }
    }


}