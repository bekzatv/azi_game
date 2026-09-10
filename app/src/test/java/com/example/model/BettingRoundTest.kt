package com.example.model

import org.junit.Assert.*
import org.junit.Test

class BettingRoundTest {
    private fun round() = BettingRound().apply { begin(listOf("a", "b", "c"), 500) }
    private val all = setOf("a", "b", "c")
    private val ante = mapOf("a" to 500L, "b" to 500L, "c" to 500L)

    @Test fun equalAntesDoNotSkipResponses() {
        val r = round()
        assertFalse(r.complete(ante, 500))
        assertFalse(r.canFinishWithCall("a", ante, 500))
        r.record("a", 500, all)
        assertFalse(r.canFinishWithCall("b", ante, 500))
        r.record("b", 500, all)
        assertTrue(r.canFinishWithCall("c", ante, 500))
        r.record("c", 500, all)
        assertTrue(r.complete(ante, 500))
    }

    @Test fun raiseInLastSeatClosesAsSoonAsOthersCall() {
        val r = round()
        r.record("a", 500, all); r.record("b", 500, all)
        r.record("c", 1000, all)
        assertFalse(r.canFinishWithCall("a", mapOf("a" to 500L, "b" to 500L, "c" to 1000L), 1000))
        r.record("a", 1000, all)
        assertTrue(r.canFinishWithCall("b", mapOf("a" to 1000L, "b" to 500L, "c" to 1000L), 1000))
        r.record("b", 1000, all)
        assertTrue(r.complete(all.associateWith { 1000L }, 1000))
        assertEquals("c", r.lastRaiserId)
    }

    @Test fun foldedLastSeatCannotKeepBettingLoopAlive() {
        val r = round()
        r.record("a", 1000, all)
        r.record("b", 1000, all)
        r.record("c", 1000, setOf("a", "b"))
        assertTrue(r.complete(mapOf("a" to 1000L, "b" to 1000L), 1000))
    }

    @Test fun reraiseRequiresFreshResponses() {
        val r = round()
        r.record("a", 1000, all); r.record("b", 1500, all)
        assertTrue(r.needsResponse("a"))
        assertTrue(r.needsResponse("c"))
        assertFalse(r.needsResponse("b"))
        assertEquals("b", r.lastRaiserId)
        r.record("c", 1500, all)
        assertFalse(r.complete(mapOf("a" to 1000L, "b" to 1500L, "c" to 1500L), 1500))
        r.record("a", 1500, all)
        assertTrue(r.complete(all.associateWith { 1500L }, 1500))
    }

    @Test fun newRoundResetsResponsesAndLeader() {
        val r = round()
        r.record("a", 1000, all)
        r.begin(all.toList(), 500)
        assertNull(r.lastRaiserId)
        assertFalse(r.canFinishWithCall("a", ante, 500))
    }

    @Test fun oneRemainingPlayerDoesNotStartTrickPlay() {
        val r = round()
        r.record("b", 500, setOf("a", "c"))
        r.record("c", 500, setOf("a"))
        assertFalse(r.complete(mapOf("a" to 500L), 500))
        assertFalse(r.canFinishWithCall("a", mapOf("a" to 500L), 500))
    }
}
