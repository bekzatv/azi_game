package com.example.network

import com.example.model.*
import com.example.viewmodel.AziGameViewModel
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.*
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SixPlayersTest {
    @Before fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        com.example.audio.SoundManager.isSoundEnabled = false
    }
    @After fun cleanup() { Dispatchers.resetMain() }

    private suspend fun eventually(condition: () -> Boolean) {
        withTimeout(30000) { while(!condition()) delay(25) }
    }

    @Test fun sixPlayerPracticeDealsEighteenDistinctCards() = runBlocking {
        val game = AziGameViewModel(RuntimeEnvironment.getApplication())
        try {
            game.startSinglePlayerGame(5)
            assertEquals(6, game.players.value.size)
            eventually { game.gamePhase.value == GamePhase.BETTING }
            assertTrue(game.players.value.all { it.cards.size == 3 })
            assertEquals(18, game.players.value.flatMap { it.cards }.distinctBy { it.id }.size)
        } finally { game.leaveCurrentGame() }
    }

    @Test fun sixOnlinePlayersDrawAziAndCarryBankWithoutLosingSeats() = runBlocking {
        val messages = CopyOnWriteArrayList<Pair<String, String>>()
        val ids = AtomicInteger()
        val relay = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        relay.createContext("/") { exchange ->
            if(exchange.requestURI.path.endsWith("/ws")) exchange.sendResponseHeaders(400, -1)
            else if(exchange.requestMethod == "POST") {
                val body = exchange.requestBody.bufferedReader().use { it.readText() }
                val id = "m" + ids.incrementAndGet()
                messages += id to JSONObject().put("event", "message").put("id", id).put("message", body).toString()
                exchange.sendResponseHeaders(200, -1)
            } else {
                val cursor = exchange.requestURI.rawQuery.orEmpty().substringAfter("since=", "")
                val bytes = messages.drop(messages.indexOfFirst { it.first == cursor } + 1)
                    .joinToString("\n") { it.second }.toByteArray(Charsets.UTF_8)
                exchange.sendResponseHeaders(200, if(bytes.isEmpty()) -1 else bytes.size.toLong())
                if(bytes.isNotEmpty()) exchange.responseBody.write(bytes)
            }
            exchange.close()
        }
        relay.start()
        val app = RuntimeEnvironment.getApplication()
        val url = "http://127.0.0.1:" + relay.address.port
        // Each seat has one card of each suit. Three different players hold the three aces.
        val ordered = listOf(Suit.HEARTS, Suit.DIAMONDS, Suit.CLUBS).flatMapIndexed { winner, suit ->
            val low = listOf(Rank.SIX, Rank.SEVEN, Rank.EIGHT, Rank.NINE, Rank.TEN).iterator()
            (0..5).map { seat -> Card(suit, if(seat == winner) Rank.ACE else low.next()) }
        }
        val trump = Card(Suit.HEARTS, Rank.JACK)
        val deck = ordered + trump + AziEvaluator.createStandardAziDeck(Suit.SPADES)
            .filter { it !in ordered && it != trump }
        val host = AziGameViewModel(app, url) { deck }
        val guests = (1..5).map { AziGameViewModel(app, url) }
        val seventh = AziGameViewModel(app, url)
        val games = listOf(host) + guests
        try {
            val code = RoomCode.generate()
            host.createOnlineWaitingRoom(StandardStakes.STAKE_500, code, 6, "Host")
            eventually { host.roomConfirmed.value }
            guests.forEachIndexed { index, game -> game.joinOnlineRoomByCode(code, "Friend ${index + 1}") }
            eventually { games.all { it.roomConfirmed.value && it.players.value.size == 6 } }
            assertTrue(games.all { it.maxRoomPlayers.value == 6 })
            val seatIds = host.players.value.map { it.id }
            assertEquals(6, seatIds.distinct().size)
            seventh.joinOnlineRoomByCode(code, "Seventh")
            eventually { seventh.roomError.value != null }
            assertFalse(seventh.roomConfirmed.value)
            guests.forEach { it.toggleReady() }
            eventually { games.all { it.readyIds.value.size == 6 } }
            host.startOnlineGameFromWaitingRoom()
            eventually { games.all { it.gamePhase.value == GamePhase.BETTING } }
            assertTrue(games.all { it.potTenge.value == 3000L })
            assertTrue(games.all { it.players.value.flatMap { p -> p.cards }.distinctBy { c -> c.id }.size == 18 })
            assertTrue(games.all { it.trumpCard.value == trump })
            repeat(6) { turn ->
                eventually { games.all { it.currentTurnIndex.value == turn && it.gamePhase.value == GamePhase.BETTING } }
                val actor = games.first { it.userPlayer.value.id == seatIds[turn] }
                actor.onUserAction(BetActionType.CHECK)
            }
            repeat(18) { step ->
                eventually {
                    games.all { it.gamePhase.value == GamePhase.PLAYING_TRICKS } &&
                        games.map { it.currentTurnIndex.value }.distinct().size == 1
                }
                val turn = host.currentTurnIndex.value
                val actor = games.first { it.userPlayer.value.id == seatIds[turn] }
                val hand = actor.players.value[turn].cards
                val lead = actor.playedCardsInTrick.value.firstOrNull()?.card?.suit
                actor.playUserCard(hand.first { AziEvaluator.isValidMove(it, hand, lead) })
                eventually { games.all { it.players.value.sumOf { p -> p.cards.size } == 17 - step } }
            }
            eventually { games.all { it.gamePhase.value == GamePhase.SVARA } }
            assertTrue(games.all { it.lastActionText.value.contains("АЗИ!") && !it.lastActionText.value.contains("СВАРА") })
            assertTrue(games.all { it.players.value.count { p -> p.tricksWonCount == 1 } == 3 })
            assertTrue(games.all { it.potTenge.value == 3000L })
            eventually { games.all { it.gamePhase.value == GamePhase.BETTING } }
            assertTrue(games.all { it.potTenge.value == 6000L })
            assertTrue(games.all { it.players.value.map { p -> p.id } == seatIds })
        } finally {
            (games + seventh).forEach { it.leaveCurrentGame() }
            delay(300)
            relay.stop(0)
        }
    }
}
