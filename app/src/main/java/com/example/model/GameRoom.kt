package com.example.model

enum class GamePhase {
    WAITING,
    WAITING_FOR_PLAYERS,
    DEALING,
    BETTING,
    PLAYING_TRICKS,
    TRICK_RESULT,
    SHOWDOWN,
    SVARA,
    WINNER_CELEBRATION
}

enum class BetActionType(val titleKz: String, val titleRu: String) {
    CHECK("Тексеру", "Чек"),
    CALL("Көру", "Вист (Колл)"),
    RAISE("Көтеру", "Поднять"),
    FOLD("Пас", "Пас"),
    SHOWDOWN("Ашу", "Вскрыть")
}

data class RoomStake(
    val id: String,
    val name: String,
    val anteTenge: Long,
    val minBetTenge: Long,
    val maxBetTenge: Long,
    val minRatingRequired: Int = 0
)

object StandardStakes {
    val STAKE_500 = RoomStake(
        id = "stake_500",
        name = "Стол Новичков (500₸)",
        anteTenge = 500L,
        minBetTenge = 500L,
        maxBetTenge = 5000L,
        minRatingRequired = 0
    )
    val STAKE_2000 = RoomStake(
        id = "stake_2000",
        name = "Золотой стол (2 000₸)",
        anteTenge = 2000L,
        minBetTenge = 2000L,
        maxBetTenge = 25000L,
        minRatingRequired = 1000
    )
    val STAKE_10000 = RoomStake(
        id = "stake_10000",
        name = "VIP Арена Хан (10 000₸)",
        anteTenge = 10000L,
        minBetTenge = 10000L,
        maxBetTenge = 150000L,
        minRatingRequired = 2500
    )

    val allStakes = listOf(STAKE_500, STAKE_2000, STAKE_10000)
}

data class OnlineTableInfo(
    val roomId: String,
    val title: String,
    val stake: RoomStake,
    val playerCount: Int,
    val maxPlayers: Int = 3,
    val hasVoiceChat: Boolean = true,
    val isPrivate: Boolean = false,
    val roomCode: String = "AZI-${(100..999).random()}"
)

