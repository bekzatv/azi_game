package com.example.network

import com.example.model.Suit
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OnlineMultiplayerTest {
    private lateinit var scope: CoroutineScope
    private val managers = mutableListOf<OnlineMultiplayerManager>()
    @Before fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
    }
    @After fun cleanup() {
        managers.forEach { it.disconnect() }
        scope.cancel()
        Dispatchers.resetMain()
    }
    private fun manager(url: String = "http://127.0.0.1:1") =
        OnlineMultiplayerManager(scope, url).also { managers += it }

    private fun packet(id: String, type: String, data: JSONObject, sender: String = "remote") =
        JSONObject().put("event", "message").put("id", id)
            .put("message", JSONObject().put("msgId", id).put("senderDeviceId", sender)
                .put("type", type).put("data", data).toString()).toString()

    @Test fun parserIgnoresEchoDuplicatesAndMalformedEvents() {
        val manager = manager()
        val received = CopyOnWriteArrayList<OnlineGameEvent>()
        manager.onEventReceived = { received += it }
        val ready = JSONObject().put("playerId", "guest").put("ready", true)
        val message = packet("1", "READY", ready)
        manager.handleIncomingRawMessage(message)
        manager.handleIncomingRawMessage(message)
        manager.handleIncomingRawMessage(packet("2", "READY", ready, manager.localDeviceId))
        manager.handleIncomingRawMessage("not json")
        manager.handleIncomingRawMessage(packet("3", "READY", JSONObject()))
        assertEquals(listOf(OnlineGameEvent.ReadyChanged("guest", true)), received.toList())
    }

    @Test fun roomSyncRetainsRulesAndReadyPlayers() {
        val manager = manager()
        var received: OnlineGameEvent.SyncRoom? = null
        manager.onEventReceived = { received = it as? OnlineGameEvent.SyncRoom }
        manager.handleIncomingRawMessage(packet("sync", "SYNC_ROOM", JSONObject()
            .put("stakeId", "stake_500").put("players", JSONArray())
            .put("maxPlayers", 4).put("excludedSuit", "HEARTS")
            .put("readyIds", JSONArray(listOf("host", "guest")))))
        assertEquals(4, received?.maxPlayers)
        assertEquals(Suit.HEARTS, received?.excludedSuit)
        assertEquals(setOf("host", "guest"), received?.readyIds)
    }

    @Test fun invalidInvitationDoesNotStartNetworkConnection() {
        val manager = manager()
        manager.connectToRoom("AZI-777")
        assertEquals(NetworkConnectionStatus.ERROR, manager.status.value)
        assertNotNull(manager.errorMessage.value)
    }

    @Test fun twoPlayersJoinReadyDealAndLeaveWithoutGhostRound() = runBlocking {
        val messages = CopyOnWriteArrayList<Pair<String, String>>()
        val ids = AtomicInteger()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            if (exchange.requestURI.path.endsWith("/ws")) {
                exchange.sendResponseHeaders(400, -1)
            } else if (exchange.requestMethod == "POST") {
                val body = exchange.requestBody.bufferedReader().use { it.readText() }
                val id = "event" + ids.incrementAndGet()
                messages += id to JSONObject().put("event", "message").put("id", id)
                    .put("message", body).toString()
                exchange.sendResponseHeaders(200, -1)
            } else {
                val cursor = exchange.requestURI.rawQuery.orEmpty().substringAfter("since=", "")
                val result = messages.drop(messages.indexOfFirst { it.first == cursor } + 1)
                    .joinToString("\n") { it.second }.toByteArray(Charsets.UTF_8)
                exchange.sendResponseHeaders(200, if(result.isEmpty()) -1 else result.size.toLong())
                if(result.isNotEmpty()) exchange.responseBody.write(result)
            }
            exchange.close()
        }
        server.start()
        val url = "http://127.0.0.1:" + server.address.port
        val app = org.robolectric.RuntimeEnvironment.getApplication()
        com.example.audio.SoundManager.isSoundEnabled = false
        val host = com.example.viewmodel.AziGameViewModel(app, url)
        val guest = com.example.viewmodel.AziGameViewModel(app, url)
        val third = com.example.viewmodel.AziGameViewModel(app, url)
        try {
            val code = RoomCode.generate()
            host.createOnlineWaitingRoom(com.example.model.StandardStakes.STAKE_500, code, 2,
                "Host", Suit.HEARTS)
            withTimeout(15000) { while(!host.roomConfirmed.value) delay(25) }
            host.startOnlineGameFromWaitingRoom()
            assertEquals(com.example.model.GamePhase.WAITING_FOR_PLAYERS, host.gamePhase.value)
            guest.joinOnlineRoomByCode(RoomCode.invitation(code), "Guest")
            withTimeout(20000) { while(!guest.roomConfirmed.value) delay(25) }
            assertEquals(host.players.value.map { it.id }, guest.players.value.map { it.id })
            assertEquals(Suit.HEARTS, guest.excludedSuit.value)
            assertEquals(2, guest.maxRoomPlayers.value)
            host.startOnlineGameFromWaitingRoom()
            assertEquals(com.example.model.GamePhase.WAITING_FOR_PLAYERS, host.gamePhase.value)
            third.joinOnlineRoomByCode(code, "Third")
            withTimeout(20000) { while(third.roomError.value == null) delay(25) }
            assertFalse(third.roomConfirmed.value)
            assertEquals(2, host.players.value.size)
            guest.toggleReady()
            withTimeout(15000) { while(!host.readyIds.value.contains(guest.userPlayer.value.id)) delay(25) }
            host.startOnlineGameFromWaitingRoom()
            withTimeout(15000) {
                while(host.gamePhase.value != com.example.model.GamePhase.BETTING ||
                    guest.gamePhase.value != com.example.model.GamePhase.BETTING) delay(25)
            }
            assertEquals(host.potTenge.value, guest.potTenge.value)
            assertEquals(host.players.value.map { it.cards }, guest.players.value.map { it.cards })
            assertTrue(host.players.value.all { it.cards.size == 3 })
            assertTrue(host.players.value.flatMap { it.cards }.none { it.suit == Suit.HEARTS })
            assertFalse(host.canStartTricksAfterUserCall())
            host.onUserAction(com.example.model.BetActionType.SHOWDOWN)
            assertEquals(com.example.model.GamePhase.BETTING, host.gamePhase.value)
            assertEquals(0, host.currentTurnIndex.value)
            host.onUserAction(com.example.model.BetActionType.CHECK)
            withTimeout(15000) { while(guest.currentTurnIndex.value != 1) delay(25) }
            guest.onUserAction(com.example.model.BetActionType.RAISE, 500)
            withTimeout(15000) {
                while(host.currentCallAmount.value != 1000L || host.currentTurnIndex.value != 0) delay(25)
            }
            assertEquals(com.example.model.GamePhase.BETTING, host.gamePhase.value)
            assertTrue(host.canStartTricksAfterUserCall())
            host.onUserAction(com.example.model.BetActionType.SHOWDOWN)
            withTimeout(15000) {
                while(host.gamePhase.value != com.example.model.GamePhase.PLAYING_TRICKS ||
                    guest.gamePhase.value != com.example.model.GamePhase.PLAYING_TRICKS) delay(25)
            }
            assertEquals(1, host.currentTurnIndex.value)
            assertEquals(1, guest.currentTurnIndex.value)
            val played = guest.players.value.first { it.isUser }.cards.first()
            guest.playUserCard(played)
            withTimeout(15000) { while(host.playedCardsInTrick.value.isEmpty()) delay(25) }
            assertEquals(host.playedCardsInTrick.value.map { it.card }, guest.playedCardsInTrick.value.map { it.card })
            repeat(5) { step ->
                withTimeout(15000) {
                    while(host.gamePhase.value != com.example.model.GamePhase.PLAYING_TRICKS ||
                        guest.gamePhase.value != com.example.model.GamePhase.PLAYING_TRICKS ||
                        host.currentTurnIndex.value != guest.currentTurnIndex.value) delay(25)
                }
                val turn = host.currentTurnIndex.value
                val actor = if(host.players.value[turn].isUser) host else guest
                val hand = actor.players.value[turn].cards
                val lead = actor.playedCardsInTrick.value.firstOrNull()?.card?.suit
                actor.playUserCard(hand.first { com.example.model.AziEvaluator.isValidMove(it, hand, lead) })
                withTimeout(15000) {
                    while(host.players.value.sumOf { it.cards.size } != 4 - step ||
                        guest.players.value.sumOf { it.cards.size } != 4 - step) delay(25)
                }
            }
            withTimeout(15000) {
                while(host.gamePhase.value != com.example.model.GamePhase.WINNER_CELEBRATION ||
                    guest.gamePhase.value != com.example.model.GamePhase.WINNER_CELEBRATION) delay(25)
            }
            assertEquals(host.winnerPlayer.value?.id, guest.winnerPlayer.value?.id)
            assertEquals(host.players.value.map { it.tengeBalance }, guest.players.value.map { it.tengeBalance })
            host.startNextRound()
            withTimeout(15000) { while(guest.gamePhase.value != com.example.model.GamePhase.DEALING) delay(25) }
            guest.leaveCurrentGame()
            withTimeout(15000) { while(host.players.value.size != 1) delay(25) }
            assertEquals(com.example.model.GamePhase.WAITING_FOR_PLAYERS, host.gamePhase.value)
            host.leaveCurrentGame()
            delay(1000)
            assertEquals(com.example.model.GamePhase.WAITING, host.gamePhase.value)
            assertEquals(com.example.model.GamePhase.WAITING, guest.gamePhase.value)
        } finally {
            host.leaveCurrentGame(); guest.leaveCurrentGame(); third.leaveCurrentGame()
            server.stop(0)
        }
    }

    // Real HTTP transport, two clients, forced WS fallback and a transient publish failure.
    // The relay is local and contains no account or user data.
    @Test fun twoClientsExchangeInOrderWithFallbackAndRetry() = runBlocking {
        val messages = CopyOnWriteArrayList<Pair<String, String>>()
        val posts = AtomicInteger()
        val accepted = AtomicInteger()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            val path = exchange.requestURI.path
            when {
                path.endsWith("/ws") -> {
                    exchange.sendResponseHeaders(400, -1)
                }
                exchange.requestMethod == "POST" -> {
                    val body = exchange.requestBody.bufferedReader().use { it.readText() }
                    if (posts.incrementAndGet() == 1) exchange.sendResponseHeaders(503, -1)
                    else {
                        val id = "msg" + accepted.incrementAndGet()
                        val outer = JSONObject().put("event", "message").put("id", id)
                            .put("message", body).toString()
                        messages += id to outer
                        exchange.sendResponseHeaders(200, -1)
                    }
                }
                else -> {
                    val cursor = exchange.requestURI.rawQuery.orEmpty().substringAfter("since=", "")
                    val index = messages.indexOfFirst { it.first == cursor }
                    val result = messages.drop(index + 1).joinToString("\n") { it.second }
                        .toByteArray(Charsets.UTF_8)
                    exchange.sendResponseHeaders(200, if(result.isEmpty()) -1 else result.size.toLong())
                    if(result.isNotEmpty()) exchange.responseBody.write(result)
                }
            }
            exchange.close()
        }
        server.start()
        try {
            val url = "http://127.0.0.1:" + server.address.port
            val host = manager(url)
            val guest = manager(url)
            val received = CopyOnWriteArrayList<OnlineGameEvent>()
            guest.onEventReceived = { received += it }
            val code = RoomCode.generate()
            host.connectToRoom(code)
            guest.connectToRoom(code)
            withTimeout(15000) {
                while(host.status.value != NetworkConnectionStatus.CONNECTED ||
                    guest.status.value != NetworkConnectionStatus.CONNECTED) delay(25)
            }
            host.broadcast("READY") { put("playerId", "host"); put("ready", true) }
            host.broadcast("READY") { put("playerId", "host"); put("ready", false) }
            withTimeout(15000) { while(received.size < 2) delay(25) }
            assertEquals(listOf(OnlineGameEvent.ReadyChanged("host", true),
                OnlineGameEvent.ReadyChanged("host", false)), received.toList())
            assertEquals(3, posts.get())
            assertFalse(host.deliveryFailed.value)
            host.disconnect()
            assertEquals(NetworkConnectionStatus.DISCONNECTED, host.status.value)
        } finally { server.stop(0) }
    }
}
