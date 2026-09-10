package com.example.model

enum class PlayerRank(
    val titleKz: String,
    val titleRu: String,
    val minPoints: Int,
    val iconName: String,
    val badgeColorHex: Long
) {
    NOVICE("Бастаушы", "Новичок", 0, "🌱", 0xFF90CAF9),
    JIGIT("Жігіт", "Джигит", 1000, "🐎", 0xFF81C784),
    MERGEN("Мерген", "Меткий", 2500, "🏹", 0xFF4DD0E1),
    BATYR("Батыр", "Батыр", 5000, "⚔️", 0xFFFFB74D),
    BIY("Бий", "Би", 10000, "📜", 0xFFBA68C8),
    SULTAN("Сұлтан", "Султан", 20000, "👑", 0xFFFFD54F),
    ULY_KHAN("Ұлы Хан", "Великий Хан", 35000, "🦅", 0xFFFF8F00);

    companion object {
        fun fromPoints(points: Int): PlayerRank {
            return values().lastOrNull { points >= it.minPoints } ?: NOVICE
        }
    }
}

data class Player(
    val id: String,
    val name: String,
    val isUser: Boolean = false,
    val tengeBalance: Long = 50000L,
    val ratingPoints: Int = 0,
    val avatarEmoji: String = "🦅",
    val avatarBgColor: Long = 0xFF025955,
    val cards: List<Card> = emptyList(),
    val hasFolded: Boolean = false,
    val currentBet: Long = 0L,
    val isSpeaking: Boolean = false,
    val isMuted: Boolean = false,
    val isWinner: Boolean = false,
    val isReady: Boolean = true,
    val handsWon: Int = 0,
    val totalGamesPlayed: Int = 0,
    val aziCount: Int = 0,
    val tricksWonCount: Int = 0
) {
    val rank: PlayerRank get() = PlayerRank.fromPoints(ratingPoints)
    fun getEvaluation(trumpSuit: Suit? = null): HandEvaluation = AziEvaluator.evaluate(cards, trumpSuit)
    val evaluation: HandEvaluation get() = AziEvaluator.evaluate(cards)
}

enum class MatchResult {
    WIN,
    LOSS
}

data class MatchHistoryItem(
    val id: String,
    val timestamp: Long,
    val result: MatchResult,
    val potWonOrLost: Long,
    val ratingChange: Int,
    val opponentCount: Int,
    val handDescription: String = "",
    val isAzi: Boolean = false,
    val roomName: String = "Обычный стол"
)

data class LeaderboardEntry(
    val rankPosition: Int,
    val name: String,
    val rank: PlayerRank,
    val ratingPoints: Int,
    val tengeTotalWon: Long,
    val winRatePercent: Int,
    val avatarEmoji: String,
    val isCurrentUser: Boolean = false
)

