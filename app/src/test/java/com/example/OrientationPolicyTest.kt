package com.example

import com.example.model.GamePhase
import org.junit.Assert.*
import org.junit.Test

class OrientationPolicyTest {
    @Test fun lobbyAlwaysPortrait() {
        GamePhase.entries.forEach { assertFalse(usesLandscapeTable(ScreenState.LOBBY, it)) }
    }
    @Test fun roomWaitingIsPortrait() {
        assertFalse(usesLandscapeTable(ScreenState.GAME, GamePhase.WAITING_FOR_PLAYERS))
    }
    @Test fun dealingBettingTricksAndResultsStayLandscape() {
        GamePhase.entries.filterNot { it == GamePhase.WAITING_FOR_PLAYERS }.forEach {
            assertTrue(usesLandscapeTable(ScreenState.GAME, it))
        }
    }
}
