package com.example.model

/** Tracks responses to the latest bet, independently of seat indexes. */
class BettingRound {
    private var pending = emptySet<String>()
    private var level = 0L
    var lastRaiserId: String? = null
        private set

    fun begin(playerIds: List<String>, initialBet: Long) {
        pending = playerIds.toSet()
        level = initialBet
        lastRaiserId = null
    }

    fun record(playerId: String, currentBet: Long, activeIds: Set<String>) {
        if (currentBet > level) {
            pending = activeIds - playerId
            lastRaiserId = playerId
            level = currentBet
        } else {
            pending = (pending intersect activeIds) - playerId
        }
    }

    fun complete(bets: Map<String, Long>, currentBet: Long): Boolean =
        bets.size >= 2 && pending.none { it in bets } && bets.values.all { it == currentBet }

    fun canFinishWithCall(playerId: String, bets: Map<String, Long>, currentBet: Long): Boolean =
        playerId in bets && bets.size >= 2 && pending.all { it == playerId || it !in bets } &&
            bets.filterKeys { it != playerId }.values.all { it == currentBet }

    fun needsResponse(playerId: String) = playerId in pending
}
