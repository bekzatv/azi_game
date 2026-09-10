package com.example.model

enum class Suit(val symbol: String, val kazakhName: String, val ruName: String, val isRed: Boolean) {
    HEARTS("♥", "Табан", "Червы", true),
    DIAMONDS("♦", "Қияр", "Бубны", true),
    CLUBS("♣", "Шыбын", "Трефы", false),
    SPADES("♠", "Қарға", "Пики", false)
}

enum class Rank(val symbol: String, val kazakhTitle: String, val ruTitle: String, val basePoints: Int, val order: Int) {
    SIX("6", "6", "6", 6, 1),
    SEVEN("7", "7", "7", 7, 2),
    EIGHT("8", "8", "8", 8, 3),
    NINE("9", "9", "9", 9, 4),
    TEN("10", "10", "10", 10, 5),
    JACK("J", "Батыр", "Валет", 10, 6),
    QUEEN("Q", "Ару", "Дама", 10, 7),
    KING("K", "Хан", "Король", 10, 8),
    ACE("A", "Тұз", "Туз", 11, 9)
}

data class Card(
    val suit: Suit,
    val rank: Rank,
    val id: String = "${suit.name}_${rank.name}"
) {
    val displayName: String get() = "${suit.symbol} ${rank.ruTitle}"
}

data class PlayedCard(
    val playerId: String,
    val playerName: String,
    val card: Card
)

enum class HandCombinationType(val kazakhName: String, val ruName: String) {
    AZI("Ази! (Үш Тұз)", "Ази! (Три туза)"),
    TRIO("Үштік", "Тройка"),
    REGULAR("Қалыпты", "Обычные карты")
}

data class HandEvaluation(
    val score: Float,
    val combinationType: HandCombinationType,
    val description: String,
    val isAzi: Boolean = false
)

object AziEvaluator {
    fun evaluate(cards: List<Card>, trumpSuit: Suit? = null): HandEvaluation {
        if (cards.size < 3) {
            return HandEvaluation(0f, HandCombinationType.REGULAR, "")
        }

        val aces = cards.filter { it.rank == Rank.ACE }
        // 1. Azi: Three Aces = 33 points (Supreme)
        if (aces.size == 3) {
            return HandEvaluation(
                score = 33f,
                combinationType = HandCombinationType.AZI,
                description = "Ази! Три туза",
                isAzi = true
            )
        }

        // 2. Three of a kind (other than Aces): e.g. 3 Kings, 3 Queens, 3 Tens
        val groupedByRank = cards.groupBy { it.rank }
        val trio = groupedByRank.entries.firstOrNull { it.value.size == 3 }
        if (trio != null) {
            val bonus = 30f + (trio.key.order * 0.1f)
            return HandEvaluation(
                score = bonus,
                combinationType = HandCombinationType.TRIO,
                description = "Тройка: 3×${trio.key.ruTitle}"
            )
        }

        // 3. Regular cards evaluation for bot AI strength (trumps + high cards)
        val trumps = if (trumpSuit != null) cards.filter { it.suit == trumpSuit } else emptyList()
        val trumpPoints = trumps.sumOf { card ->
            when (card.rank) {
                Rank.ACE -> 15
                Rank.KING -> 12
                Rank.QUEEN -> 10
                Rank.JACK -> 8
                Rank.TEN -> 7
                Rank.NINE -> 6
                Rank.EIGHT -> 5
                Rank.SEVEN -> 4
                Rank.SIX -> 3
            }
        }
        val nonTrumpPoints = cards.filter { it.suit != trumpSuit }.sumOf { card ->
            when (card.rank) {
                Rank.ACE -> 8
                Rank.KING -> 5
                Rank.QUEEN -> 4
                Rank.JACK -> 3
                Rank.TEN -> 2
                else -> 1
            }
        }
        val totalStrength = trumpPoints.toFloat() + nonTrumpPoints.toFloat()

        return HandEvaluation(
            score = totalStrength,
            combinationType = HandCombinationType.REGULAR,
            description = ""
        )
    }

    /**
     * Determines the winner of a single trick in Azi.
     * Follows classic card rules:
     * - Highest card of the trump suit wins
     * - Otherwise, highest card of the lead suit wins
     */
    fun determineTrickWinner(cardsPlayed: List<PlayedCard>, trumpSuit: Suit?): PlayedCard {
        require(cardsPlayed.isNotEmpty()) { "No cards played in trick" }
        val leadSuit = cardsPlayed.first().card.suit

        // 1. Trumps played
        if (trumpSuit != null) {
            val trumpPlays = cardsPlayed.filter { it.card.suit == trumpSuit }
            if (trumpPlays.isNotEmpty()) {
                return trumpPlays.maxByOrNull { it.card.rank.order }!!
            }
        }

        // 2. No trumps played - highest of lead suit wins
        val leadPlays = cardsPlayed.filter { it.card.suit == leadSuit }
        return leadPlays.maxByOrNull { it.card.rank.order } ?: cardsPlayed.first()
    }

    /**
     * Checks if a card move is valid according to Azi rules:
     * Must follow lead suit if player has it in hand.
     * If no lead suit, can play any card (including trump).
     */
    fun isValidMove(cardToPlay: Card, hand: List<Card>, leadSuit: Suit?): Boolean {
        if (leadSuit == null) return true // First player can play any card
        val hasLeadSuit = hand.any { it.suit == leadSuit }
        return if (hasLeadSuit) {
            cardToPlay.suit == leadSuit
        } else {
            true // Can play any card (including trump)
        }
    }

    /**
     * Creates an authentic Azi deck of 27 cards (3 suits × 9 ranks from 6 to Ace).
     * The specified [excludedSuit] is completely removed from the deck.
     */
    fun createStandardAziDeck(excludedSuit: Suit? = Suit.SPADES): List<Card> {
        val deck = mutableListOf<Card>()
        for (suit in Suit.values()) {
            if (suit == excludedSuit) continue
            for (rank in Rank.values()) {
                deck.add(Card(suit, rank))
            }
        }
        return deck
    }
}

data class DealingCardAnimationState(
    val targetPlayerId: String,
    val targetPlayerName: String,
    val cardIndex: Int,
    val card: Card,
    val isUser: Boolean
)


