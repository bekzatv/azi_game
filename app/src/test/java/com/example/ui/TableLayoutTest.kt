package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.example.model.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w640dp-h360dp-land-mdpi")
class TableLayoutTest {
    @get:Rule val rule = createComposeRule()
    private val cards = listOf(Card(Suit.HEARTS, Rank.ACE), Card(Suit.CLUBS, Rank.QUEEN), Card(Suit.DIAMONDS, Rank.TEN))
    private fun scene(count: Int = 6): LandscapeTableState {
        val players = (0 until count).map { Player("p$it", if (it == 0) "Бекзат" else "Игрок $it с длинным именем",
            isUser = it == 0, cards = cards, avatarEmoji = "🦅") }
        return LandscapeTableState(players, players.first(), GamePhase.PLAYING_TRICKS, "p0", 30, 3000,
            cards.first(), players.mapIndexed { index, player -> PlayedCard(player.id, player.name, cards[index % 3]) },
            1, AvailableDecks.SHANYRAK_NOIR, "Выберите карту для хода", null, true)
    }
    private fun bounds(tag: String) = rule.onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
    private fun inside(inner: Rect, outer: Rect) {
        assertTrue("$inner not inside $outer", inner.left >= outer.left - 1 && inner.top >= outer.top - 1 &&
            inner.right <= outer.right + 1 && inner.bottom <= outer.bottom + 1)
    }

    @Test fun playedCardsStaySmallAndPortraitFromOneToSix() {
        val count = mutableStateOf(1)
        rule.setContent { MyApplicationTheme { Box(Modifier.size(290.dp, 100.dp)) {
            PlayedTrickCards(scene().played.take(count.value), AvailableDecks.SHANYRAK_NOIR)
        } } }
        for (n in 1..6) {
            rule.runOnIdle { count.value = n }
            rule.waitForIdle()
            val row = bounds("played_cards")
            val rectangles = (0 until n).map { bounds("played_card_p$it") }
            rectangles.forEach {
                assertTrue("Card grew with row width: $it", it.width <= 41f)
                assertEquals(0.68f, it.width / it.height, 0.02f)
                inside(it, row)
            }
            rectangles.zipWithNext().forEach { (a, b) -> assertTrue(a.right <= b.left) }
        }
    }

    @Test fun sixSeatsHandAndTrickFitTheSameSmallTable() {
        rule.setContent { MyApplicationTheme {
            LandscapeTableSurface(scene(), Modifier.size(496.dp, 288.dp), {})
        } }
        rule.waitForIdle()
        val table = bounds("table_surface")
        val profiles = (1..5).map { bounds("seat_p$it") }
        val hand = bounds("user_hand")
        val played = (0..5).map { bounds("played_card_p$it") }
        profiles.forEach { inside(it, table); assertFalse(it.overlaps(hand)) }
        profiles.forEachIndexed { i, a -> profiles.drop(i + 1).forEach { b -> assertFalse(a.overlaps(b)) } }
        inside(hand, table)
        inside(bounds("user_profile"), table)
        played.forEach { card ->
            inside(card, table)
            assertFalse(card.overlaps(hand))
            profiles.forEach { assertFalse(card.overlaps(it)) }
        }
        cards.forEach { assertTrue(bounds("hand_card_${it.id}").width <= 53f) }
    }

    @Test fun threePlayerResultDoesNotStretchTheLastTrick() {
        val result = scene(3).copy(phase = GamePhase.WINNER_CELEBRATION, currentId = null,
            action = "Победитель: Бекзат (+1500 ₸)")
        rule.setContent { MyApplicationTheme { LandscapeTableSurface(result, Modifier.size(496.dp, 288.dp), {}) } }
        rule.waitForIdle()
        (0..2).forEach { assertTrue(bounds("played_card_p$it").width <= 41f) }
    }

    @Test fun allPlayerCountsFitFullWidthWithLargerText() {
        val count = mutableStateOf(2)
        rule.setContent { MyApplicationTheme {
            CompositionLocalProvider(LocalDensity provides Density(1f, 1.3f)) {
                LandscapeTableSurface(scene(count.value), Modifier.size(628.dp, 254.dp), {})
            }
        } }
        for (n in 2..6) {
            rule.runOnIdle { count.value = n }
            rule.waitForIdle()
            val table = bounds("table_surface")
            val seats = (1 until n).map { bounds("seat_p$it") }
            val hand = bounds("user_hand")
            seats.forEach { inside(it, table); assertTrue(it.bottom < hand.top) }
            (0 until n).forEach {
                val card = bounds("played_card_p$it")
                inside(card, table)
                assertFalse(card.overlaps(hand))
                seats.forEach { seat -> assertFalse(card.overlaps(seat)) }
            }
            if (n == 6) assertTrue("Seats should use the full table width", seats.last().right - seats.first().left > 600)
        }
    }

    @Test fun bettingActionsFitAndRaiseMenuSendsSelectedAmount() {
        val actions = mutableListOf<Pair<BetActionType, Long>>()
        rule.setContent { MyApplicationTheme {
            CompositionLocalProvider(LocalDensity provides Density(1f, 1.3f)) {
                Box(Modifier.size(628.dp, 60.dp)) {
                    TableBettingActions(10000, true, true) { type, amount -> actions += type to amount }
                }
            }
        } }
        val bar = bounds("betting_actions")
        listOf("Пас", "Уравнять 10,000 ₸", "Поднять ▾", "Начать розыгрыш").forEach { label ->
            val node = rule.onNodeWithText(label)
            node.assertIsDisplayed()
            inside(node.fetchSemanticsNode().boundsInRoot, bar)
        }
        rule.onNodeWithText("Поднять ▾").performClick()
        rule.onNodeWithText("Поднять на 2,000 ₸").performClick()
        assertEquals(listOf(BetActionType.RAISE to 2000L), actions)
    }

    @Test fun disconnectedBettingActionsAreDisabled() {
        rule.setContent { MyApplicationTheme { TableBettingActions(0, false, false) { _, _ -> fail("Disconnected action") } } }
        listOf("Пас", "Чек", "Поднять ▾").forEach { rule.onNodeWithText(it).assertIsNotEnabled() }
        rule.onNodeWithText("Начать розыгрыш").assertDoesNotExist()
    }

    @Test fun onlyLegalHandCardsCanBePlayed() {
        val picked = mutableListOf<Card>()
        // Hearts were led. The user holds a heart and must follow it.
        val game = scene().copy(played = listOf(PlayedCard("p1", "Соперник", Card(Suit.HEARTS, Rank.SIX))))
        rule.setContent { MyApplicationTheme { LandscapeTableSurface(game, Modifier.size(496.dp, 288.dp), picked::add) } }
        rule.onNodeWithTag("hand_card_${cards[1].id}", useUnmergedTree = true).assertHasNoClickAction()
        rule.onNodeWithTag("hand_card_${cards[0].id}", useUnmergedTree = true).performClick()
        assertEquals(listOf(cards[0]), picked)
    }

    @Test fun dealFliesFromDeckToEverySeatAndEveryUserSlot() {
        val empty = scene().players.map { it.copy(cards = emptyList()) }
        val state = mutableStateOf(scene().copy(players = empty, user = empty.first(), played = emptyList(),
            currentId = null, phase = GamePhase.DEALING))
        rule.setContent { MyApplicationTheme { LandscapeTableSurface(state.value, Modifier.size(496.dp, 288.dp), {}) } }
        rule.waitForIdle()
        rule.mainClock.autoAdvance = false
        val targets = empty.drop(1).map { it to 0 } + (0..2).map { empty.first() to it }
        targets.forEach { (player, slot) ->
            rule.runOnIdle { state.value = state.value.copy(deal = DealingCardAnimationState(player.id, player.name, slot, cards[slot], player.isUser)) }
            rule.mainClock.advanceTimeBy(32)
            rule.waitForIdle()
            // Coordinate callbacks run during layout; let their resulting state reach composition.
            rule.mainClock.advanceTimeByFrame()
            rule.waitForIdle()
            rule.onNodeWithTag("dealing_flight", useUnmergedTree = true).assertExists("Flight to ${player.id}, slot $slot")
            val start = bounds("dealing_flight")
            rule.mainClock.advanceTimeBy(112)
            rule.waitForIdle()
            val middle = bounds("dealing_flight")
            assertTrue("The card did not move", (start.center - middle.center).getDistance() > 5f)
            inside(middle, bounds("table_surface"))
            rule.mainClock.advanceTimeBy(160)
            rule.waitForIdle()
            val arrived = bounds("dealing_flight").center
            val target = bounds(if (player.isUser) "hand_slot_$slot" else "seat_${player.id}").center
            assertTrue("Missed $player, $arrived != $target", (arrived - target).getDistance() < 2f)
        }
        rule.runOnIdle { state.value = state.value.copy(phase = GamePhase.BETTING, deal = null) }
        rule.mainClock.autoAdvance = true
        rule.waitForIdle()
        rule.onNodeWithTag("dealing_flight").assertDoesNotExist()
    }
}
