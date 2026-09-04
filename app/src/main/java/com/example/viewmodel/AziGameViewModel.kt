package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.audio.VoiceChatManager
import com.example.model.AvailableDecks
import com.example.model.AziEvaluator
import com.example.model.BetActionType
import com.example.model.Card
import com.example.model.ChatMessage
import com.example.model.DeckTheme
import com.example.model.GamePhase
import com.example.model.LeaderboardEntry
import com.example.model.MatchHistoryItem
import com.example.model.MatchResult
import com.example.model.OnlineTableInfo
import com.example.model.PlayedCard
import com.example.model.Player
import com.example.model.PlayerRank
import com.example.model.QuickPhrases
import com.example.model.RoomStake
import com.example.model.StandardStakes
import com.example.model.Suit
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.network.NetworkConnectionStatus
import com.example.network.OnlineGameEvent
import com.example.network.OnlineMultiplayerManager
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class AziGameViewModel(application: Application) : AndroidViewModel(application) {

    val voiceChatManager = VoiceChatManager(application.applicationContext)
    val multiplayerManager = OnlineMultiplayerManager(viewModelScope)
    val networkStatus: StateFlow<NetworkConnectionStatus> = multiplayerManager.status
    private val _isRoomHost = MutableStateFlow(false)
    val isRoomHost: StateFlow<Boolean> = _isRoomHost.asStateFlow()

    // User Profile
    private val _userPlayer = MutableStateFlow(
        Player(
            id = "user_${multiplayerManager.localDeviceId}",
            name = "Вы (Игрок)",
            isUser = true,
            tengeBalance = 100000L,
            ratingPoints = 0,
            avatarEmoji = "🦅",
            avatarBgColor = 0xFF025955,
            handsWon = 0,
            totalGamesPlayed = 0,
            aziCount = 0
        )
    )
    val userPlayer: StateFlow<Player> = _userPlayer.asStateFlow()

    // Match History for User Profile (last 5 matches)
    private val _matchHistory = MutableStateFlow<List<MatchHistoryItem>>(emptyList())
    val matchHistory: StateFlow<List<MatchHistoryItem>> = _matchHistory.asStateFlow()

    // Excluded Suit State (Azi is played with 3 suits, 27 cards)
    private val _excludedSuit = MutableStateFlow(Suit.SPADES)
    val excludedSuit: StateFlow<Suit> = _excludedSuit.asStateFlow()

    fun setExcludedSuit(suit: Suit) {
        _excludedSuit.value = suit
    }

    // Deck Customization
    private val _availableDecks = MutableStateFlow(AvailableDecks.allDecks)
    val availableDecks: StateFlow<List<DeckTheme>> = _availableDecks.asStateFlow()

    private val _selectedDeck = MutableStateFlow(AvailableDecks.ALTYN_ORDA)
    val selectedDeck: StateFlow<DeckTheme> = _selectedDeck.asStateFlow()

    // Room & Stakes
    private val _currentStake = MutableStateFlow(StandardStakes.STAKE_500)
    val currentStake: StateFlow<RoomStake> = _currentStake.asStateFlow()

    private val _isOnlineMode = MutableStateFlow(false)
    val isOnlineMode: StateFlow<Boolean> = _isOnlineMode.asStateFlow()

    private val _roomCode = MutableStateFlow<String?>("AZI-777")
    val roomCode: StateFlow<String?> = _roomCode.asStateFlow()

    private val _onlineTables = MutableStateFlow<List<OnlineTableInfo>>(emptyList())
    val onlineTables: StateFlow<List<OnlineTableInfo>> = _onlineTables.asStateFlow()

    // Game Table State
    private val _gamePhase = MutableStateFlow(GamePhase.WAITING)
    val gamePhase: StateFlow<GamePhase> = _gamePhase.asStateFlow()

    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    private val _currentTurnIndex = MutableStateFlow(0)
    val currentTurnIndex: StateFlow<Int> = _currentTurnIndex.asStateFlow()

    private val _potTenge = MutableStateFlow(0L)
    val potTenge: StateFlow<Long> = _potTenge.asStateFlow()

    private val _currentCallAmount = MutableStateFlow(0L)
    val currentCallAmount: StateFlow<Long> = _currentCallAmount.asStateFlow()

    private val _isSvara = MutableStateFlow(false)
    val isSvara: StateFlow<Boolean> = _isSvara.asStateFlow()

    private val _turnTimerSeconds = MutableStateFlow(30)
    val turnTimerSeconds: StateFlow<Int> = _turnTimerSeconds.asStateFlow()

    private val _isSoundEnabled = MutableStateFlow(SoundManager.isSoundEnabled)
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    fun toggleSound() {
        val next = !_isSoundEnabled.value
        _isSoundEnabled.value = next
        SoundManager.isSoundEnabled = next
    }

    private val _lastActionText = MutableStateFlow("Добро пожаловать в игру Ази!")
    val lastActionText: StateFlow<String> = _lastActionText.asStateFlow()

    private val _winnerPlayer = MutableStateFlow<Player?>(null)
    val winnerPlayer: StateFlow<Player?> = _winnerPlayer.asStateFlow()

    // Trump Card & Suit State
    private val _trumpCard = MutableStateFlow<Card?>(null)
    val trumpCard: StateFlow<Card?> = _trumpCard.asStateFlow()

    private val _trumpSuit = MutableStateFlow<Suit?>(null)
    val trumpSuit: StateFlow<Suit?> = _trumpSuit.asStateFlow()

    // Real-Time Tricks State (3 tricks in Azi)
    private val _currentTrick = MutableStateFlow(1)
    val currentTrick: StateFlow<Int> = _currentTrick.asStateFlow()

    private val _playedCardsInTrick = MutableStateFlow<List<PlayedCard>>(emptyList())
    val playedCardsInTrick: StateFlow<List<PlayedCard>> = _playedCardsInTrick.asStateFlow()

    private val _trickWinnerAnnounce = MutableStateFlow<String?>(null)
    val trickWinnerAnnounce: StateFlow<String?> = _trickWinnerAnnounce.asStateFlow()

    // Dealing animation state - dynamic card flying
    private val _currentlyDealingCard = MutableStateFlow<com.example.model.DealingCardAnimationState?>(null)
    val currentlyDealingCard: StateFlow<com.example.model.DealingCardAnimationState?> = _currentlyDealingCard.asStateFlow()

    private val _dealingProgress = MutableStateFlow(0f)
    val dealingProgress: StateFlow<Float> = _dealingProgress.asStateFlow()

    // Online room max players & waiting state
    private val _maxRoomPlayers = MutableStateFlow(3)
    val maxRoomPlayers: StateFlow<Int> = _maxRoomPlayers.asStateFlow()

    // Chat
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // Leaderboard (только реальные игроки!)
    private val _leaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard.asStateFlow()

    private var turnTimerJob: Job? = null
    private var botThinkingJob: Job? = null

    init {
        initLobbyTables()
        initLeaderboard()
        setupMultiplayerEvents()
        addSystemChatMessage("Добро пожаловать за игровой стол Ази! Желаем удачи! 🃏")
    }

    private fun setupMultiplayerEvents() {
        multiplayerManager.onEventReceived = { event ->
            when (event) {
                is OnlineGameEvent.PlayerJoined -> {
                    if (_isOnlineMode.value) {
                        val incoming = event.player
                        val current = _players.value
                        if (current.none { it.id == incoming.id }) {
                            val updated = current + incoming.copy(isUser = false)
                            _players.value = updated
                            SoundManager.playChipBet()
                            addSystemChatMessage("👋 ${incoming.name} вошёл в комнату по коду ${_roomCode.value}!")
                            _lastActionText.value = "${incoming.name} подключился! (${updated.size}/${_maxRoomPlayers.value})"

                            if (_isRoomHost.value) {
                                syncRoomToNetwork()
                            }
                        }
                    }
                }
                is OnlineGameEvent.SyncRoom -> {
                    if (_isOnlineMode.value && !_isRoomHost.value) {
                        val matchedStake = StandardStakes.allStakes.find { it.id == event.stakeId }
                        if (matchedStake != null) {
                            _currentStake.value = matchedStake
                        }
                        val myId = _userPlayer.value.id
                        val merged = event.players.map { p ->
                            if (p.id == myId) {
                                _userPlayer.value.copy(
                                    cards = emptyList(),
                                    hasFolded = false,
                                    currentBet = 0L,
                                    tricksWonCount = 0,
                                    isUser = true
                                )
                            } else {
                                p.copy(isUser = false)
                            }
                        }
                        _players.value = merged
                        _lastActionText.value = "Синхронизировано! За столом: ${merged.size} из ${_maxRoomPlayers.value}"
                        SoundManager.playChipBet()
                    }
                }
                is OnlineGameEvent.GameStarted -> {
                    if (_isOnlineMode.value && !_isRoomHost.value) {
                        applyRemoteGameStarted(event)
                    }
                }
                is OnlineGameEvent.BetAction -> {
                    if (_isOnlineMode.value) {
                        handleRemoteBetAction(event.playerId, event.actionType, event.amount)
                    }
                }
                is OnlineGameEvent.CardPlayed -> {
                    if (_isOnlineMode.value) {
                        handleRemoteCardPlayed(event.playerId, event.card)
                    }
                }
                is OnlineGameEvent.ChatReceived -> {
                    addChatMessage(event.senderName, event.senderId, event.text)
                }
                is OnlineGameEvent.NextRound -> {
                    if (_isOnlineMode.value && !_isRoomHost.value) {
                        _lastActionText.value = "Создатель начинает следующий раунд..."
                    }
                }
                is OnlineGameEvent.PlayerLeft -> {
                    val left = _players.value.find { it.id == event.playerId }
                    if (left != null) {
                        addSystemChatMessage("👋 ${left.name} вышел из комнаты")
                        _players.value = _players.value.filter { it.id != event.playerId }
                    }
                }
            }
        }
    }

    private fun syncRoomToNetwork() {
        multiplayerManager.broadcast("SYNC_ROOM") {
            put("stakeId", _currentStake.value.id)
            val arr = JSONArray()
            _players.value.forEach { p ->
                arr.put(multiplayerManager.playerToJson(p))
            }
            put("players", arr)
        }
    }

    private fun initLobbyTables() {
        _onlineTables.value = listOf(
            OnlineTableInfo("room_1", "Астана - Быстрый стол", StandardStakes.STAKE_500, 2, 3, roomCode = "AZI-101"),
            OnlineTableInfo("room_2", "Алматы - Ханский стол", StandardStakes.STAKE_2000, 3, 3, roomCode = "AZI-202"),
            OnlineTableInfo("room_3", "Шымкент - Арена Батыров", StandardStakes.STAKE_10000, 1, 3, roomCode = "AZI-303"),
            OnlineTableInfo("room_4", "Актау - Каспийский Бриз", StandardStakes.STAKE_500, 2, 3, roomCode = "AZI-404"),
            OnlineTableInfo("room_5", "Туркестан - VIP Золотая Орда", StandardStakes.STAKE_10000, 2, 3, roomCode = "AZI-505")
        )
    }

    private fun initLeaderboard() {
        val user = _userPlayer.value
        _leaderboard.value = listOf(
            LeaderboardEntry(
                rankPosition = 1,
                name = user.name,
                rank = user.rank,
                ratingPoints = user.ratingPoints,
                tengeTotalWon = 0L,
                winRatePercent = 0,
                avatarEmoji = user.avatarEmoji,
                isCurrentUser = true
            )
        )
    }

    fun updateUserName(newName: String) {
        val trimmed = newName.trim().take(20)
        if (trimmed.isEmpty()) return
        _userPlayer.value = _userPlayer.value.copy(name = trimmed)
        _players.value = _players.value.map {
            if (it.isUser) it.copy(name = trimmed) else it
        }
        _leaderboard.value = _leaderboard.value.map {
            if (it.isCurrentUser) it.copy(name = trimmed) else it
        }
    }

    fun resetPlayerStats() {
        val current = _userPlayer.value
        val resetUser = current.copy(
            ratingPoints = 0,
            handsWon = 0,
            totalGamesPlayed = 0,
            aziCount = 0,
            tengeBalance = 100000L
        )
        _userPlayer.value = resetUser
        _players.value = _players.value.map {
            if (it.isUser) resetUser else it
        }
        _matchHistory.value = emptyList()
        initLeaderboard()
    }

    fun startSinglePlayerGame(botCount: Int, excludedSuit: Suit = _excludedSuit.value) {
        _excludedSuit.value = excludedSuit
        startNewGame(
            stake = StandardStakes.STAKE_500,
            online = false,
            customCode = null,
            botCount = botCount.coerceIn(1, 3),
            excludedSuit = excludedSuit
        )
    }

    fun createOnlineWaitingRoom(
        stake: RoomStake,
        roomCode: String,
        maxPlayers: Int,
        userName: String? = null,
        excludedSuit: Suit = _excludedSuit.value
    ) {
        _excludedSuit.value = excludedSuit
        if (!userName.isNullOrBlank()) {
            updateUserName(userName)
        }
        _currentStake.value = stake
        _isOnlineMode.value = true
        _isRoomHost.value = true
        _roomCode.value = roomCode
        _maxRoomPlayers.value = maxPlayers.coerceIn(2, 4)
        botThinkingJob?.cancel()
        turnTimerJob?.cancel()

        _currentTrick.value = 1
        _playedCardsInTrick.value = emptyList()
        _trickWinnerAnnounce.value = null
        _potTenge.value = 0L
        _isSvara.value = false

        val hostUser = _userPlayer.value.copy(
            cards = emptyList(),
            hasFolded = false,
            currentBet = 0L,
            tricksWonCount = 0,
            isWinner = false,
            isUser = true
        )
        _players.value = listOf(hostUser)
        _gamePhase.value = GamePhase.WAITING_FOR_PLAYERS
        _lastActionText.value = "Комната $roomCode создана! Ожидание друзей..."
        addSystemChatMessage("Комната $roomCode создана. Скопируйте код и отправьте друзьям!")

        multiplayerManager.connectToRoom(roomCode) {
            addSystemChatMessage("🟢 Подключено к сети! Код: $roomCode")
        }
    }

    fun joinOnlineRoomByCode(code: String, userName: String? = null) {
        val formattedCode = code.trim().uppercase()
        if (!userName.isNullOrBlank()) {
            updateUserName(userName)
        }
        _currentStake.value = StandardStakes.STAKE_500
        _isOnlineMode.value = true
        _isRoomHost.value = false
        _roomCode.value = formattedCode
        _maxRoomPlayers.value = 3
        botThinkingJob?.cancel()
        turnTimerJob?.cancel()

        val currentUser = _userPlayer.value.copy(
            cards = emptyList(),
            hasFolded = false,
            currentBet = 0L,
            tricksWonCount = 0,
            isWinner = false,
            isUser = true
        )

        _players.value = listOf(currentUser)
        _gamePhase.value = GamePhase.WAITING_FOR_PLAYERS
        _lastActionText.value = "Подключение к комнате $formattedCode через интернет..."

        multiplayerManager.connectToRoom(formattedCode) {
            multiplayerManager.broadcast("PLAYER_JOIN") {
                put("player", multiplayerManager.playerToJson(currentUser))
            }
            _lastActionText.value = "🟢 Подключено! Ожидание ответа создателя..."
            addSystemChatMessage("🟢 Подключено к комнате $formattedCode. Синхронизация...")
        }
    }

    fun addFriendToWaitingRoom(friendName: String? = null) {
        if (_players.value.size >= _maxRoomPlayers.value) return
        val namesPool = listOf("Арман", "Алихан", "Дана", "Марат", "Жанна", "Диас", "Айбек")
        val chosenName = friendName?.trim()?.takeIf { it.isNotBlank() } ?: "Друг (${namesPool.random()})"
        val newPlayer = Player(
            id = "bot_friend_${System.currentTimeMillis()}",
            name = chosenName,
            isUser = false,
            tengeBalance = 50000L,
            ratingPoints = (1300..2100).random(),
            avatarEmoji = listOf("😎", "🎯", "🏇", "🦁", "⚡").random(),
            avatarBgColor = listOf(0xFF1565C0, 0xFFAD1457, 0xFF2E7D32, 0xFFE65100).random(),
            tricksWonCount = 0
        )
        _players.value = _players.value + newPlayer
        SoundManager.playChipBet()
        addSystemChatMessage("${newPlayer.name} добавлен в комнату!")

        if (_isOnlineMode.value && _isRoomHost.value) {
            syncRoomToNetwork()
        }

        _lastActionText.value = "${newPlayer.name} за столом! (${_players.value.size}/${_maxRoomPlayers.value})"
    }

    fun startNextRound() {
        if (_isOnlineMode.value) {
            if (_isRoomHost.value) {
                multiplayerManager.broadcast("NEXT_ROUND") {
                    put("round", _currentTrick.value + 1)
                }
                startOnlineGameFromWaitingRoom()
            } else {
                _lastActionText.value = "Ожидание начала следующего раунда от создателя..."
            }
        } else {
            startNewGame(
                stake = _currentStake.value,
                online = false,
                customCode = null,
                botCount = (_players.value.size - 1).coerceIn(1, 3)
            )
        }
    }

    fun startOnlineGameFromWaitingRoom() {
        if (_players.value.isEmpty()) return
        val stake = _currentStake.value
        val ante = stake.anteTenge
        var pot = 0L

        val updatedWithAnte = _players.value.map { p ->
            val newBal = (p.tengeBalance - ante).coerceAtLeast(0L)
            pot += ante
            p.copy(
                tengeBalance = newBal,
                cards = emptyList(),
                hasFolded = false,
                currentBet = ante,
                isWinner = false,
                tricksWonCount = 0
            )
        }

        _players.value = updatedWithAnte
        _potTenge.value = pot
        _currentCallAmount.value = ante
        _currentTrick.value = 1
        _playedCardsInTrick.value = emptyList()
        _trickWinnerAnnounce.value = null
        _lastActionText.value = "Все игроки готовы! Раздача карт..."
        _gamePhase.value = GamePhase.DEALING

        val fullDeck = AziEvaluator.createStandardAziDeck(excludedSuit = _excludedSuit.value).shuffled()
        val nPlayers = updatedWithAnte.size.coerceAtLeast(2)
        val trump = fullDeck[nPlayers * 3]
        _trumpCard.value = trump
        _trumpSuit.value = trump.suit

        val handsMap = mutableMapOf<String, List<Card>>()
        for (i in updatedWithAnte.indices) {
            val p = updatedWithAnte[i]
            val pCards = listOf(
                fullDeck[0 * nPlayers + i],
                fullDeck[1 * nPlayers + i],
                fullDeck[2 * nPlayers + i]
            )
            handsMap[p.id] = pCards
        }

        // Broadcast GAME_STARTED to all connected phones
        multiplayerManager.broadcast("GAME_STARTED") {
            put("trumpCard", multiplayerManager.cardToJson(trump))
            put("pot", pot)
            put("ante", ante)
            put("turnIndex", 0)
            put("excludedSuit", _excludedSuit.value.name)
            val handsObj = JSONObject()
            handsMap.forEach { (pId, cards) ->
                val arr = JSONArray()
                cards.forEach { c -> arr.put(multiplayerManager.cardToJson(c)) }
                handsObj.put(pId, arr)
            }
            put("hands", handsObj)
        }

        dealOnlineCardsLocally(handsMap, trump, 0)
    }

    private fun applyRemoteGameStarted(event: OnlineGameEvent.GameStarted) {
        val trump = event.trumpCard
        _trumpCard.value = trump
        _trumpSuit.value = trump.suit
        _excludedSuit.value = event.excludedSuit
        _potTenge.value = event.pot
        _currentCallAmount.value = event.ante
        _currentTrick.value = 1
        _playedCardsInTrick.value = emptyList()
        _trickWinnerAnnounce.value = null
        _gamePhase.value = GamePhase.DEALING

        val updated = _players.value.map { p ->
            val cardsForPlayer = event.hands[p.id] ?: emptyList()
            val newBal = (p.tengeBalance - event.ante).coerceAtLeast(0L)
            p.copy(
                tengeBalance = newBal,
                cards = cardsForPlayer,
                hasFolded = false,
                currentBet = event.ante,
                isWinner = false,
                tricksWonCount = 0
            )
        }
        _players.value = updated

        dealOnlineCardsLocally(event.hands, trump, event.turnIndex)
    }

    private fun dealOnlineCardsLocally(
        handsMap: Map<String, List<Card>>,
        trump: Card,
        startingTurnIndex: Int = 0
    ) {
        viewModelScope.launch {
            _dealingProgress.value = 0f
            _currentlyDealingCard.value = null
            val currentList = _players.value
            val nPlayers = currentList.size.coerceAtLeast(2)
            val totalDeals = nPlayers * 3

            for (cardRound in 0 until 3) {
                for (pIdx in currentList.indices) {
                    val p = currentList[pIdx]
                    val card = handsMap[p.id]?.getOrNull(cardRound) ?: Card(Suit.SPADES, com.example.model.Rank.TEN)

                    _currentlyDealingCard.value = com.example.model.DealingCardAnimationState(
                        targetPlayerId = p.id,
                        targetPlayerName = p.name,
                        cardIndex = cardRound,
                        card = card,
                        isUser = p.isUser
                    )
                    SoundManager.playCardDeal(variation = cardRound * nPlayers + pIdx)
                    _dealingProgress.value = (cardRound * nPlayers + pIdx + 1).toFloat() / totalDeals.toFloat()
                    delay(250)

                    updatePlayerCards(p.id, handsMap[p.id]?.take(cardRound + 1) ?: emptyList())
                    delay(80)
                }
            }

            _currentlyDealingCard.value = null
            _dealingProgress.value = 1f
            delay(200)

            _gamePhase.value = GamePhase.BETTING
            _currentTurnIndex.value = startingTurnIndex

            val userCards = _players.value.firstOrNull { it.isUser }?.cards ?: emptyList()
            val userEval = AziEvaluator.evaluate(userCards, _trumpSuit.value)
            val trumpInfo = "Козырь: ${trump.suit.symbol} ${trump.suit.ruName}"

            if (userEval.isAzi) {
                _lastActionText.value = "$trumpInfo. 🔥 ВАМ ВЫПАЛО АЗИ (ТРИ ТУЗА)!"
                SoundManager.playWinSound()
                addSystemChatMessage("🔥 ВАМ ВЫПАЛО АЗИ (ТРИ ТУЗА)!")
            } else if (userEval.combinationType == com.example.model.HandCombinationType.TRIO) {
                _lastActionText.value = "$trumpInfo. У вас ${userEval.description}!"
            } else {
                _lastActionText.value = "$trumpInfo. Идет торговля — сделайте ставку."
            }

            startTurnTimer()
        }
    }

    private fun handleRemoteBetAction(playerId: String, actionStr: String, raiseAmount: Long) {
        val player = _players.value.find { it.id == playerId } ?: return
        if (player.isUser) return

        val action = try { BetActionType.valueOf(actionStr) } catch (e: Exception) { BetActionType.CALL }

        when (action) {
            BetActionType.FOLD -> {
                _players.value = _players.value.map {
                    if (it.id == playerId) it.copy(hasFolded = true) else it
                }
                _lastActionText.value = "${player.name}: Пас"
                advanceTurn()
            }
            BetActionType.CALL, BetActionType.CHECK -> {
                val callDiff = (_currentCallAmount.value - player.currentBet).coerceAtLeast(0L)
                val newBal = (player.tengeBalance - callDiff).coerceAtLeast(0L)
                _potTenge.value += callDiff
                _players.value = _players.value.map {
                    if (it.id == playerId) it.copy(tengeBalance = newBal, currentBet = _currentCallAmount.value) else it
                }
                _lastActionText.value = if (callDiff == 0L) "${player.name}: Чек" else "${player.name}: Колл (${_currentCallAmount.value}₸)"
                SoundManager.playChipBet()
                advanceTurn()
            }
            BetActionType.RAISE -> {
                val totalBet = _currentCallAmount.value + raiseAmount
                val diff = (totalBet - player.currentBet).coerceAtLeast(0L)
                val newBal = (player.tengeBalance - diff).coerceAtLeast(0L)
                _potTenge.value += diff
                _currentCallAmount.value = totalBet
                _players.value = _players.value.map {
                    if (it.id == playerId) it.copy(tengeBalance = newBal, currentBet = totalBet) else it
                }
                _lastActionText.value = "${player.name} поднял ставку: +${raiseAmount}₸ (всего ${totalBet}₸)"
                SoundManager.playChipBet()
                advanceTurn()
            }
            BetActionType.SHOWDOWN -> {
                val callDiff = (_currentCallAmount.value - player.currentBet).coerceAtLeast(0L)
                val newBal = (player.tengeBalance - callDiff).coerceAtLeast(0L)
                _potTenge.value += callDiff
                _players.value = _players.value.map {
                    if (it.id == playerId) it.copy(tengeBalance = newBal, currentBet = _currentCallAmount.value) else it
                }
                _lastActionText.value = "${player.name} вскрывает торговлю!"
                startTricksPhase()
            }
        }
    }

    private fun handleRemoteCardPlayed(playerId: String, card: Card) {
        val player = _players.value.find { it.id == playerId } ?: return
        if (player.isUser) return

        turnTimerJob?.cancel()

        val newCards = if (player.cards.any { it.id == card.id }) {
            player.cards.filter { it.id != card.id }
        } else {
            player.cards.dropLast(1)
        }
        _players.value = _players.value.map {
            if (it.id == playerId) it.copy(cards = newCards) else it
        }

        val played = PlayedCard(player.id, player.name, card)
        _playedCardsInTrick.value = _playedCardsInTrick.value + played
        _lastActionText.value = "${player.name} сыграл: ${card.displayName}"
        SoundManager.playCardPlay()

        advanceTrickTurn()
    }

    fun leaveCurrentGame() {
        if (_isOnlineMode.value) {
            multiplayerManager.broadcast("PLAYER_LEFT") {
                put("playerId", _userPlayer.value.id)
            }
            multiplayerManager.disconnect()
        }
        botThinkingJob?.cancel()
        turnTimerJob?.cancel()
        _isOnlineMode.value = false
        _gamePhase.value = GamePhase.WAITING
    }

    fun startNewGame(
        stake: RoomStake = _currentStake.value,
        online: Boolean = false,
        customCode: String? = null,
        botCount: Int = 2,
        excludedSuit: Suit = _excludedSuit.value
    ) {
        _excludedSuit.value = excludedSuit
        _currentStake.value = stake
        _isOnlineMode.value = online
        _roomCode.value = customCode ?: "AZI-${(100..999).random()}"
        botThinkingJob?.cancel()
        turnTimerJob?.cancel()

        _currentTrick.value = 1
        _playedCardsInTrick.value = emptyList()
        _trickWinnerAnnounce.value = null

        val botsPool = listOf(
            Player(
                id = "bot_1",
                name = if (online) "Друг (Алихан)" else "Бауыржан",
                isUser = false,
                tengeBalance = 60000L,
                ratingPoints = 2100,
                avatarEmoji = if (online) "😎" else "🐎",
                avatarBgColor = 0xFF1565C0,
                tricksWonCount = 0
            ),
            Player(
                id = "bot_2",
                name = if (online) "Друг (Дана)" else "Айгерим",
                isUser = false,
                tengeBalance = 45000L,
                ratingPoints = 1850,
                avatarEmoji = if (online) "🎯" else "👸",
                avatarBgColor = 0xFFAD1457,
                tricksWonCount = 0
            ),
            Player(
                id = "bot_3",
                name = if (online) "Друг (Арман)" else "Серик",
                isUser = false,
                tengeBalance = 55000L,
                ratingPoints = 1950,
                avatarEmoji = if (online) "⚡" else "🏹",
                avatarBgColor = 0xFF2E7D32,
                tricksWonCount = 0
            )
        )

        val activeBots = botsPool.take(botCount.coerceIn(1, 3))
        val activeList = listOf(_userPlayer.value.copy(tricksWonCount = 0)) + activeBots
        _players.value = activeList
        _winnerPlayer.value = null

        // Ante deduction
        val ante = stake.anteTenge
        var pot = 0L

        val updatedWithAnte = activeList.map { p ->
            val newBal = (p.tengeBalance - ante).coerceAtLeast(0L)
            pot += ante
            p.copy(
                tengeBalance = newBal,
                cards = emptyList(),
                hasFolded = false,
                currentBet = ante,
                isWinner = false,
                tricksWonCount = 0
            )
        }

        _players.value = updatedWithAnte
        _potTenge.value = if (_isSvara.value) _potTenge.value + pot else pot
        _currentCallAmount.value = ante
        _lastActionText.value = "Начальная ставка (Анте): ${stake.anteTenge}₸"
        SoundManager.playChipBet()

        // Start Dealing Animation phase
        _gamePhase.value = GamePhase.DEALING
        dealCards()
    }

    private fun dealCards() {
        viewModelScope.launch {
            _dealingProgress.value = 0f
            _currentlyDealingCard.value = null
            val fullDeck = AziEvaluator.createStandardAziDeck(excludedSuit = _excludedSuit.value).shuffled()
            val currentList = _players.value
            val nPlayers = currentList.size.coerceAtLeast(2)

            // 1. Reveal Trump Card from remaining deck
            val trump = fullDeck[nPlayers * 3]
            _trumpCard.value = trump
            _trumpSuit.value = trump.suit

            delay(200)

            // 2. Deal 3 cards to each player sequentially with dynamic flying animation
            val playerCardsMap = currentList.associate { it.id to mutableListOf<Card>() }.toMutableMap()
            val totalDeals = nPlayers * 3

            for (cardRound in 0 until 3) {
                for (pIdx in currentList.indices) {
                    val p = currentList[pIdx]
                    val card = fullDeck[cardRound * nPlayers + pIdx]

                    _currentlyDealingCard.value = com.example.model.DealingCardAnimationState(
                        targetPlayerId = p.id,
                        targetPlayerName = p.name,
                        cardIndex = cardRound,
                        card = card,
                        isUser = p.isUser
                    )
                    SoundManager.playCardDeal(variation = cardRound * nPlayers + pIdx)
                    _dealingProgress.value = (cardRound * nPlayers + pIdx + 1).toFloat() / totalDeals.toFloat()

                    delay(300)

                    playerCardsMap[p.id]?.add(card)
                    updatePlayerCards(p.id, playerCardsMap[p.id]?.toList() ?: emptyList())

                    delay(120)
                }
            }

            _currentlyDealingCard.value = null
            _dealingProgress.value = 1f
            delay(300)
            SoundManager.playCardDeal(variation = 5)
            _lastActionText.value = "Козырь открыт: ${trump.suit.symbol} ${trump.suit.ruName} (${trump.rank.ruTitle})"
            delay(500)

            _gamePhase.value = GamePhase.BETTING
            _currentTurnIndex.value = 0 // User goes first

            val userCards = _players.value.firstOrNull { it.isUser }?.cards ?: emptyList()
            val userEval = AziEvaluator.evaluate(userCards, _trumpSuit.value)
            val trumpInfo = "Козырь: ${trump.suit.symbol} ${trump.suit.ruName}"

            if (userEval.isAzi) {
                _lastActionText.value = "$trumpInfo. 🔥 ВАМ ВЫПАЛО АЗИ (ТРИ ТУЗА)!"
                SoundManager.playWinSound()
                addSystemChatMessage("🔥 ВАМ ВЫПАЛО АЗИ (ТРИ ТУЗА)!")
            } else if (userEval.combinationType == com.example.model.HandCombinationType.TRIO) {
                _lastActionText.value = "$trumpInfo. У вас ${userEval.description}!"
            } else {
                _lastActionText.value = "$trumpInfo. Идет торговля — сделайте ставку."
            }

            startTurnTimer()
        }
    }

    private fun updatePlayerCards(playerId: String, cards: List<Card>) {
        _players.value = _players.value.map {
            if (it.id == playerId) it.copy(cards = cards) else it
        }
    }

    fun onUserAction(action: BetActionType, raiseAmountTenge: Long = _currentStake.value.minBetTenge) {
        if (_gamePhase.value != GamePhase.BETTING) return
        val currentPlayers = _players.value
        val user = currentPlayers.firstOrNull { it.isUser } ?: return
        if (currentPlayers.getOrNull(_currentTurnIndex.value)?.id != user.id) return

        turnTimerJob?.cancel()

        when (action) {
            BetActionType.FOLD -> {
                val updated = currentPlayers.map {
                    if (it.id == user.id) it.copy(hasFolded = true) else it
                }
                _players.value = updated
                _lastActionText.value = "Вы: Пас (вышли из раздачи)"
                if (_isOnlineMode.value) {
                    multiplayerManager.broadcast("BET_ACTION") {
                        put("playerId", user.id)
                        put("action", BetActionType.FOLD.name)
                        put("amount", 0L)
                    }
                }
                advanceTurn()
            }
            BetActionType.CALL, BetActionType.CHECK -> {
                val callDiff = (_currentCallAmount.value - user.currentBet).coerceAtLeast(0L)
                val newBal = (user.tengeBalance - callDiff).coerceAtLeast(0L)
                _potTenge.value += callDiff
                val updated = currentPlayers.map {
                    if (it.id == user.id) it.copy(tengeBalance = newBal, currentBet = _currentCallAmount.value) else it
                }
                _players.value = updated
                _lastActionText.value = if (callDiff == 0L) "Вы: Чек" else "Вы: Вист / Колл (${_currentCallAmount.value}₸)"
                SoundManager.playChipBet()
                if (_isOnlineMode.value) {
                    multiplayerManager.broadcast("BET_ACTION") {
                        put("playerId", user.id)
                        put("action", action.name)
                        put("amount", 0L)
                    }
                }
                advanceTurn()
            }
            BetActionType.RAISE -> {
                val totalBet = _currentCallAmount.value + raiseAmountTenge
                val diff = (totalBet - user.currentBet).coerceAtLeast(0L)
                val newBal = (user.tengeBalance - diff).coerceAtLeast(0L)
                _potTenge.value += diff
                _currentCallAmount.value = totalBet
                val updated = currentPlayers.map {
                    if (it.id == user.id) it.copy(tengeBalance = newBal, currentBet = totalBet) else it
                }
                _players.value = updated
                _lastActionText.value = "Вы подняли ставку: ${totalBet}₸"
                SoundManager.playChipBet()
                if (_isOnlineMode.value) {
                    multiplayerManager.broadcast("BET_ACTION") {
                        put("playerId", user.id)
                        put("action", BetActionType.RAISE.name)
                        put("amount", raiseAmountTenge)
                    }
                }
                advanceTurn()
            }
            BetActionType.SHOWDOWN -> {
                val callDiff = (_currentCallAmount.value - user.currentBet).coerceAtLeast(0L)
                val newBal = (user.tengeBalance - callDiff).coerceAtLeast(0L)
                _potTenge.value += callDiff
                val updated = currentPlayers.map {
                    if (it.id == user.id) it.copy(tengeBalance = newBal, currentBet = _currentCallAmount.value) else it
                }
                _players.value = updated
                if (_isOnlineMode.value) {
                    multiplayerManager.broadcast("BET_ACTION") {
                        put("playerId", user.id)
                        put("action", BetActionType.SHOWDOWN.name)
                        put("amount", 0L)
                    }
                }
                startTricksPhase()
            }
        }
    }

    private fun advanceTurn() {
        val activePlayers = _players.value.filter { !it.hasFolded }
        if (activePlayers.size <= 1) {
            executeImmediateFoldWin()
            return
        }

        // Check if all active players matched currentCallAmount
        val allMatched = activePlayers.all { it.currentBet == _currentCallAmount.value }
        if (allMatched && _currentTurnIndex.value == _players.value.lastIndex) {
            startTricksPhase()
            return
        }

        var nextIndex = (_currentTurnIndex.value + 1) % _players.value.size
        while (_players.value[nextIndex].hasFolded) {
            nextIndex = (nextIndex + 1) % _players.value.size
        }

        _currentTurnIndex.value = nextIndex
        startTurnTimer()

        val nextPlayer = _players.value[nextIndex]
        if (!nextPlayer.isUser && (!_isOnlineMode.value || nextPlayer.id.startsWith("bot_"))) {
            handleBotTurn(nextPlayer)
        }
    }

    private fun handleBotTurn(bot: Player) {
        botThinkingJob?.cancel()
        botThinkingJob = viewModelScope.launch {
            delay(1000)

            val eval = AziEvaluator.evaluate(bot.cards, _trumpSuit.value)
            val callDiff = (_currentCallAmount.value - bot.currentBet).coerceAtLeast(0L)

            val action: BetActionType
            val raiseStake = _currentStake.value.minBetTenge

            when {
                eval.isAzi || eval.score >= 26f -> {
                    action = if (Math.random() > 0.4 && _currentCallAmount.value < _currentStake.value.maxBetTenge) {
                        BetActionType.RAISE
                    } else BetActionType.CALL
                }
                eval.score >= 18f -> {
                    action = BetActionType.CALL
                }
                eval.score >= 12f -> {
                    action = if (callDiff <= _currentStake.value.anteTenge * 2) BetActionType.CALL else BetActionType.FOLD
                }
                else -> {
                    action = if (callDiff == 0L) BetActionType.CALL else BetActionType.FOLD
                }
            }

            if (Math.random() > 0.6) {
                voiceChatManager.simulatePlayerSpeaking(bot.id, 2000L)
            }
            if (Math.random() > 0.75) {
                val phrase = QuickPhrases.russianQuickPhrases.random()
                addChatMessage(bot.name, bot.id, phrase, isQuick = true)
            }

            val currentList = _players.value
            when (action) {
                BetActionType.FOLD -> {
                    _players.value = currentList.map {
                        if (it.id == bot.id) it.copy(hasFolded = true) else it
                    }
                    _lastActionText.value = "${bot.name}: Пас"
                }
                BetActionType.CALL -> {
                    val newBal = (bot.tengeBalance - callDiff).coerceAtLeast(0L)
                    _potTenge.value += callDiff
                    _players.value = currentList.map {
                        if (it.id == bot.id) it.copy(tengeBalance = newBal, currentBet = _currentCallAmount.value) else it
                    }
                    _lastActionText.value = "${bot.name}: Вист / Колл (${_currentCallAmount.value}₸)"
                    SoundManager.playChipBet()
                }
                BetActionType.RAISE -> {
                    val total = _currentCallAmount.value + raiseStake
                    val diff = (total - bot.currentBet).coerceAtLeast(0L)
                    val newBal = (bot.tengeBalance - diff).coerceAtLeast(0L)
                    _potTenge.value += diff
                    _currentCallAmount.value = total
                    _players.value = currentList.map {
                        if (it.id == bot.id) it.copy(tengeBalance = newBal, currentBet = total) else it
                    }
                    _lastActionText.value = "${bot.name} поднял ставку: ${total}₸"
                    SoundManager.playChipBet()
                }
                else -> {}
            }

            delay(600)
            advanceTurn()
        }
    }

    private fun executeImmediateFoldWin() {
        val nonFolded = _players.value.filter { !it.hasFolded }
        if (nonFolded.size == 1) {
            val winner = nonFolded.first()
            declareWinner(winner, reason = "Все соперники сбросили карты!")
        }
    }

    // ==========================================
    // REAL-TIME TRICK PLAYING ENGINE (РОЗЫГРЫШ 3-Х ВЗЯТОК)
    // ==========================================

    private fun startTricksPhase() {
        turnTimerJob?.cancel()
        _gamePhase.value = GamePhase.PLAYING_TRICKS
        _currentTrick.value = 1
        _playedCardsInTrick.value = emptyList()
        _trickWinnerAnnounce.value = null

        // Find first active player
        var firstActiveIndex = 0
        while (firstActiveIndex < _players.value.size && _players.value[firstActiveIndex].hasFolded) {
            firstActiveIndex++
        }
        _currentTurnIndex.value = firstActiveIndex

        _lastActionText.value = "Розыгрыш 3-х взяток! Козырь: ${_trumpSuit.value?.symbol} ${_trumpSuit.value?.ruName}"
        addSystemChatMessage("🃏 Ставки сделаны! Начинается розыгрыш 3-х взяток. Ходите картами!")

        startTrickTurnTimer()

        val activePlayer = _players.value.getOrNull(firstActiveIndex)
        if (activePlayer != null && !activePlayer.isUser) {
            handleBotTrickTurn(activePlayer)
        }
    }

    fun playUserCard(card: Card) {
        if (_gamePhase.value != GamePhase.PLAYING_TRICKS) return
        val user = _players.value.firstOrNull { it.isUser } ?: return
        if (_players.value.getOrNull(_currentTurnIndex.value)?.id != user.id) return

        val leadSuit = _playedCardsInTrick.value.firstOrNull()?.card?.suit
        if (!AziEvaluator.isValidMove(card, user.cards, leadSuit)) {
            _lastActionText.value = "Обязательно ходить в масть: ${leadSuit?.symbol} ${leadSuit?.ruName}!"
            return
        }

        turnTimerJob?.cancel()

        // Remove card from user's hand
        val remainingCards = user.cards.filter { it.id != card.id }
        _players.value = _players.value.map {
            if (it.id == user.id) it.copy(cards = remainingCards) else it
        }

        // Add to cards on table for current trick
        val played = PlayedCard(user.id, user.name, card)
        _playedCardsInTrick.value = _playedCardsInTrick.value + played
        _lastActionText.value = "Вы сыграли: ${card.displayName}"
        SoundManager.playCardPlay()

        if (_isOnlineMode.value) {
            multiplayerManager.broadcast("PLAY_CARD") {
                put("playerId", user.id)
                put("card", multiplayerManager.cardToJson(card))
            }
        }

        advanceTrickTurn()
    }

    private fun handleBotTrickTurn(bot: Player) {
        botThinkingJob?.cancel()
        botThinkingJob = viewModelScope.launch {
            delay(1000)

            if (bot.cards.isEmpty()) return@launch

            val leadSuit = _playedCardsInTrick.value.firstOrNull()?.card?.suit
            val validCards = bot.cards.filter { AziEvaluator.isValidMove(it, bot.cards, leadSuit) }
            val cardToPlay = chooseSmartBotCard(validCards, leadSuit, _trumpSuit.value)

            // Remove card from bot hand
            val remainingCards = bot.cards.filter { it.id != cardToPlay.id }
            _players.value = _players.value.map {
                if (it.id == bot.id) it.copy(cards = remainingCards) else it
            }

            // Play card to table
            val played = PlayedCard(bot.id, bot.name, cardToPlay)
            _playedCardsInTrick.value = _playedCardsInTrick.value + played
            _lastActionText.value = "${bot.name} сыграл: ${cardToPlay.displayName}"
            SoundManager.playCardPlay()

            delay(400)
            advanceTrickTurn()
        }
    }

    private fun chooseSmartBotCard(validCards: List<Card>, leadSuit: Suit?, trumpSuit: Suit?): Card {
        if (validCards.size == 1) return validCards.first()

        // If leading trick: prefer highest non-trump or strong trump
        if (leadSuit == null) {
            val highNonTrump = validCards.filter { it.suit != trumpSuit }.maxByOrNull { it.rank.order }
            return highNonTrump ?: validCards.maxByOrNull { it.rank.order } ?: validCards.first()
        }

        // Following trick:
        // Check if bot can beat current winning card
        val currentWinner = AziEvaluator.determineTrickWinner(_playedCardsInTrick.value, trumpSuit)
        val winningSuit = currentWinner.card.suit
        val winningRankOrder = currentWinner.card.rank.order

        val beatingCards = validCards.filter { c ->
            when {
                c.suit == trumpSuit && winningSuit != trumpSuit -> true
                c.suit == winningSuit && c.rank.order > winningRankOrder -> true
                else -> false
            }
        }

        return if (beatingCards.isNotEmpty()) {
            // Play lowest beating card to conserve strength
            beatingCards.minByOrNull { it.rank.order }!!
        } else {
            // Can't win -> slough lowest rank card
            validCards.minByOrNull { it.rank.order }!!
        }
    }

    private fun advanceTrickTurn() {
        val activePlayers = _players.value.filter { !it.hasFolded }
        val cardsPlayedCount = _playedCardsInTrick.value.size

        if (cardsPlayedCount >= activePlayers.size) {
            // Trick is complete! Resolve winner
            resolveCurrentTrick()
        } else {
            // Next player in trick
            var nextIndex = (_currentTurnIndex.value + 1) % _players.value.size
            while (_players.value[nextIndex].hasFolded) {
                nextIndex = (nextIndex + 1) % _players.value.size
            }
            _currentTurnIndex.value = nextIndex
            startTrickTurnTimer()

            val nextPlayer = _players.value[nextIndex]
            if (!nextPlayer.isUser && (!_isOnlineMode.value || nextPlayer.id.startsWith("bot_"))) {
                handleBotTrickTurn(nextPlayer)
            }
        }
    }

    private fun resolveCurrentTrick() {
        turnTimerJob?.cancel()
        _gamePhase.value = GamePhase.TRICK_RESULT

        viewModelScope.launch {
            val winningPlay = AziEvaluator.determineTrickWinner(_playedCardsInTrick.value, _trumpSuit.value)
            val trickNum = _currentTrick.value

            // Increment winner's trick count
            _players.value = _players.value.map {
                if (it.id == winningPlay.playerId) it.copy(tricksWonCount = it.tricksWonCount + 1) else it
            }

            _trickWinnerAnnounce.value = "Взятку №$trickNum взял: ${winningPlay.playerName} (${winningPlay.card.displayName})!"
            _lastActionText.value = "${winningPlay.playerName} забрал взятку!"
            SoundManager.playTrickCompleted(isUserWinner = winningPlay.playerId == _userPlayer.value.id)

            delay(1800)

            if (_currentTrick.value < 3) {
                // Next trick!
                _currentTrick.value += 1
                _playedCardsInTrick.value = emptyList()
                _trickWinnerAnnounce.value = null

                // Trick winner leads the next trick!
                val winnerIndex = _players.value.indexOfFirst { it.id == winningPlay.playerId }
                _currentTurnIndex.value = if (winnerIndex >= 0) winnerIndex else 0

                _gamePhase.value = GamePhase.PLAYING_TRICKS
                _lastActionText.value = "Взятка №${_currentTrick.value} из 3. Ходит ${winningPlay.playerName}"

                startTrickTurnTimer()

                val nextPlayer = _players.value.getOrNull(_currentTurnIndex.value)
                if (nextPlayer != null && !nextPlayer.isUser && (!_isOnlineMode.value || nextPlayer.id.startsWith("bot_"))) {
                    handleBotTrickTurn(nextPlayer)
                }
            } else {
                // All 3 tricks completed! Check overall round outcome
                finishTricksRound()
            }
        }
    }

    private fun finishTricksRound() {
        val nonFolded = _players.value.filter { !it.hasFolded }
        val maxTricks = nonFolded.maxOfOrNull { it.tricksWonCount } ?: 0
        val contenders = nonFolded.filter { it.tricksWonCount == maxTricks }

        if (contenders.size == 1 && maxTricks >= 2) {
            // Decisive winner (2 or 3 tricks taken = Azi!)
            val winner = contenders.first()
            declareWinner(winner, reason = "Взял ${winner.tricksWonCount} из 3 взяток (АЗИ)!")
        } else if (contenders.size == 1 && nonFolded.size == 2 && maxTricks == 2) {
            val winner = contenders.first()
            declareWinner(winner, reason = "Взял ${winner.tricksWonCount} взятки!")
        } else {
            // Tie (1:1:1 or split tricks) -> SVARA!
            _isSvara.value = true
            _gamePhase.value = GamePhase.SVARA
            _lastActionText.value = "⚡ СВАРА! Ничья по взяткам (1:1:1). Банк остается на кону!"
            SoundManager.playSvaraGong()
            addSystemChatMessage("⚡ СВАРА! Ни один игрок не взял 2 взятки. Банк переходит в следующий раунд!")

            viewModelScope.launch {
                delay(3200)
                startNewGame(_currentStake.value, _isOnlineMode.value, _roomCode.value)
            }
        }
    }

    private fun declareWinner(winner: Player, reason: String = "") {
        val potWon = _potTenge.value

        _winnerPlayer.value = winner
        _gamePhase.value = GamePhase.WINNER_CELEBRATION
        _lastActionText.value = "Победитель: ${winner.name} (+${potWon}₸! $reason)"

        SoundManager.playWinSound()

        val updatedPlayers = _players.value.map { p ->
            if (p.id == winner.id) {
                val newBal = p.tengeBalance + potWon
                val newRp = p.ratingPoints + 60
                val newHandsWon = p.handsWon + 1
                val newTotal = p.totalGamesPlayed + 1
                val newAzi = if (p.tricksWonCount >= 2) p.aziCount + 1 else p.aziCount
                p.copy(
                    tengeBalance = newBal,
                    ratingPoints = newRp,
                    handsWon = newHandsWon,
                    totalGamesPlayed = newTotal,
                    aziCount = newAzi,
                    isWinner = true
                )
            } else {
                val newRp = (p.ratingPoints - 25).coerceAtLeast(0)
                val newTotal = p.totalGamesPlayed + 1
                p.copy(ratingPoints = newRp, totalGamesPlayed = newTotal, isWinner = false)
            }
        }

        _players.value = updatedPlayers
        _potTenge.value = 0L
        _isSvara.value = false

        val updatedUser = updatedPlayers.firstOrNull { it.isUser }
        if (updatedUser != null) {
            _userPlayer.value = updatedUser

            // Сохраняем в историю последних 5 матчей
            val isUserWinner = winner.id == updatedUser.id
            val userEval = updatedUser.getEvaluation(_trumpSuit.value)
            val historyItem = MatchHistoryItem(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                result = if (isUserWinner) MatchResult.WIN else MatchResult.LOSS,
                potWonOrLost = if (isUserWinner) potWon else _currentStake.value.minBetTenge,
                ratingChange = if (isUserWinner) +60 else -25,
                opponentCount = (_players.value.size - 1).coerceAtLeast(1),
                handDescription = if (userEval.isAzi) "Ази (3 карты)" else "${userEval.score.toInt()} очк.",
                isAzi = userEval.isAzi,
                roomName = if (_isOnlineMode.value) "Онлайн комната" else "Стол ${_currentStake.value.minBetTenge}₸"
            )
            // Храним ровно последние 5 матчей
            val newHistory = (listOf(historyItem) + _matchHistory.value).take(5)
            _matchHistory.value = newHistory
        }

        // Обновляем реальных игроков в лидерборде
        updateLeaderboardAfterGame(updatedPlayers, winner, potWon)

        addSystemChatMessage("🏆 ${winner.name} забрал банк ${potWon}₸! $reason")
    }

    private fun updateLeaderboardAfterGame(players: List<Player>, winner: Player, potWon: Long) {
        val currentLeaderboard = _leaderboard.value.toMutableList()

        for (p in players) {
            val existingIndex = currentLeaderboard.indexOfFirst {
                if (p.isUser) it.isCurrentUser else it.name == p.name
            }
            val isWon = p.id == winner.id
            if (existingIndex >= 0) {
                val old = currentLeaderboard[existingIndex]
                val newWonAmount = if (isWon) old.tengeTotalWon + potWon else old.tengeTotalWon
                val games = p.totalGamesPlayed.coerceAtLeast(1)
                val winRate = (p.handsWon * 100) / games
                currentLeaderboard[existingIndex] = old.copy(
                    name = p.name,
                    rank = p.rank,
                    ratingPoints = p.ratingPoints,
                    tengeTotalWon = newWonAmount,
                    winRatePercent = winRate
                )
            } else {
                currentLeaderboard.add(
                    LeaderboardEntry(
                        rankPosition = currentLeaderboard.size + 1,
                        name = p.name,
                        rank = p.rank,
                        ratingPoints = p.ratingPoints,
                        tengeTotalWon = if (isWon) potWon else 0L,
                        winRatePercent = if (isWon) 100 else 0,
                        avatarEmoji = p.avatarEmoji,
                        isCurrentUser = p.isUser
                    )
                )
            }
        }

        _leaderboard.value = currentLeaderboard
            .sortedByDescending { it.ratingPoints }
            .mapIndexed { idx, it -> it.copy(rankPosition = idx + 1) }
    }

    private fun startTurnTimer() {
        turnTimerJob?.cancel()
        _turnTimerSeconds.value = 30
        turnTimerJob = viewModelScope.launch {
            while (_turnTimerSeconds.value > 0) {
                delay(1000)
                _turnTimerSeconds.value -= 1
            }
            val currentPlayer = _players.value.getOrNull(_currentTurnIndex.value)
            if (currentPlayer != null && currentPlayer.isUser) {
                onUserAction(BetActionType.FOLD)
            }
        }
    }

    private fun startTrickTurnTimer() {
        turnTimerJob?.cancel()
        _turnTimerSeconds.value = 30
        turnTimerJob = viewModelScope.launch {
            while (_turnTimerSeconds.value > 0) {
                delay(1000)
                _turnTimerSeconds.value -= 1
            }
            val currentPlayer = _players.value.getOrNull(_currentTurnIndex.value)
            if (currentPlayer != null && currentPlayer.isUser) {
                // Auto-play first valid card on timeout
                val leadSuit = _playedCardsInTrick.value.firstOrNull()?.card?.suit
                val valid = currentPlayer.cards.firstOrNull { AziEvaluator.isValidMove(it, currentPlayer.cards, leadSuit) }
                    ?: currentPlayer.cards.firstOrNull()
                if (valid != null) {
                    playUserCard(valid)
                }
            }
        }
    }

    // Chat methods
    fun sendUserChatMessage(text: String) {
        val user = _userPlayer.value
        addChatMessage(senderName = user.name, senderId = user.id, text = text)
        if (_isOnlineMode.value) {
            multiplayerManager.broadcast("CHAT") {
                put("text", text)
                put("senderName", user.name)
                put("senderId", user.id)
            }
        }
    }

    private fun addChatMessage(senderName: String, senderId: String, text: String, isQuick: Boolean = false) {
        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderName = senderName,
            senderId = senderId,
            text = text,
            isQuickPhrase = isQuick
        )
        _chatMessages.value = _chatMessages.value + msg
    }

    private fun addSystemChatMessage(text: String) {
        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderName = "Ази Игра",
            senderId = "system",
            text = text,
            isSystem = true
        )
        _chatMessages.value = _chatMessages.value + msg
    }

    // Deck Selection (Free customization)
    fun selectDeck(deck: DeckTheme) {
        _selectedDeck.value = deck
    }

    fun buyDeck(deck: DeckTheme) {
        _selectedDeck.value = deck
    }

    override fun onCleared() {
        super.onCleared()
        multiplayerManager.disconnect()
        voiceChatManager.cleanup()
        turnTimerJob?.cancel()
        botThinkingJob?.cancel()
    }
}

