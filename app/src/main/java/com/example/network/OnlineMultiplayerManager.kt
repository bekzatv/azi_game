package com.example.network

import android.util.Log
import com.example.model.Card
import com.example.model.Player
import com.example.model.Rank
import com.example.model.Suit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

enum class NetworkConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

sealed class OnlineGameEvent {
    data class PlayerJoined(val player: Player) : OnlineGameEvent()
    data class SyncRoom(val stakeId: String, val players: List<Player>) : OnlineGameEvent()
    data class GameStarted(
        val trumpCard: Card,
        val hands: Map<String, List<Card>>,
        val pot: Long,
        val ante: Long,
        val turnIndex: Int,
        val excludedSuit: Suit = Suit.SPADES
    ) : OnlineGameEvent()
    data class BetAction(val playerId: String, val actionType: String, val amount: Long) : OnlineGameEvent()
    data class CardPlayed(val playerId: String, val card: Card) : OnlineGameEvent()
    data class ChatReceived(val senderId: String, val senderName: String, val text: String) : OnlineGameEvent()
    data class PlayerLeft(val playerId: String) : OnlineGameEvent()
    data class NextRound(val roundNumber: Int) : OnlineGameEvent()
}

/**
 * Real-time peer pub/sub multiplayer engine over secure WebSocket & HTTP relay.
 * Allows multiple physical Android devices anywhere in the world to connect by room code (e.g. AZI-777).
 */
class OnlineMultiplayerManager(
    private val scope: CoroutineScope
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // For persistent WebSocket
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private var currentWebSocket: WebSocket? = null
    private var pollingJob: Job? = null

    private val _status = MutableStateFlow(NetworkConnectionStatus.DISCONNECTED)
    val status: StateFlow<NetworkConnectionStatus> = _status.asStateFlow()

    private val _lastPingTimeMs = MutableStateFlow(0L)
    val lastPingTimeMs: StateFlow<Long> = _lastPingTimeMs.asStateFlow()

    private var currentRoomTopic: String? = null
    val localDeviceId: String = UUID.randomUUID().toString().take(8)

    var onEventReceived: ((OnlineGameEvent) -> Unit)? = null

    private val seenMessageIds = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()

    fun connectToRoom(rawRoomCode: String, onConnected: (() -> Unit)? = null) {
        disconnect()

        val cleanTopic = "azi_game_" + rawRoomCode.trim().uppercase().replace("-", "_").replace("[^A-Z0-9_]".toRegex(), "")
        currentRoomTopic = cleanTopic
        _status.value = NetworkConnectionStatus.CONNECTING

        val wsUrl = "wss://ntfy.sh/$cleanTopic/ws"
        val request = Request.Builder().url(wsUrl).build()

        currentWebSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("OnlineAzi", "WebSocket connected to $cleanTopic")
                _status.value = NetworkConnectionStatus.CONNECTED
                _lastPingTimeMs.value = System.currentTimeMillis()
                scope.launch(Dispatchers.Main) {
                    onConnected?.invoke()
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                _lastPingTimeMs.value = System.currentTimeMillis()
                handleIncomingRawMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.w("OnlineAzi", "WebSocket failed: ${t.message}. Starting fallback polling.", t)
                _status.value = NetworkConnectionStatus.ERROR
                startFallbackPolling(cleanTopic)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("OnlineAzi", "WebSocket closed: $reason")
                _status.value = NetworkConnectionStatus.DISCONNECTED
            }
        })
    }

    /**
     * Fallback JSON stream / polling in case the network blocks WebSockets.
     */
    private fun startFallbackPolling(cleanTopic: String) {
        pollingJob?.cancel()
        pollingJob = scope.launch(Dispatchers.IO) {
            _status.value = NetworkConnectionStatus.CONNECTING
            while (isActive && currentRoomTopic == cleanTopic) {
                try {
                    val pollUrl = "https://ntfy.sh/$cleanTopic/json?poll=1&since=15s"
                    val request = Request.Builder().url(pollUrl).build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        _status.value = NetworkConnectionStatus.CONNECTED
                        val bodyString = response.body?.string().orEmpty()
                        bodyString.lineSequence().forEach { line ->
                            if (line.isNotBlank()) {
                                handleIncomingRawMessage(line)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w("OnlineAzi", "Polling error: ${e.message}")
                }
                delay(1500)
            }
        }
    }

    private fun handleIncomingRawMessage(rawText: String) {
        try {
            val ntfyObj = JSONObject(rawText)
            val eventType = ntfyObj.optString("event")
            if (eventType == "keepalive" || eventType == "open") {
                return
            }

            val innerMsg = ntfyObj.optString("message")
            if (innerMsg.isBlank()) return

            val payload = JSONObject(innerMsg)
            val senderDevice = payload.optString("senderDeviceId")
            val msgId = payload.optString("msgId")

            // Ignore our own echo or already processed duplicate messages
            if (senderDevice == localDeviceId) return
            if (msgId.isNotBlank() && !seenMessageIds.add(msgId)) return

            val type = payload.optString("type")
            val data = payload.optJSONObject("data") ?: JSONObject()

            scope.launch(Dispatchers.Main) {
                when (type) {
                    "PLAYER_JOIN" -> {
                        val player = parsePlayer(data.getJSONObject("player"))
                        onEventReceived?.invoke(OnlineGameEvent.PlayerJoined(player))
                    }
                    "SYNC_ROOM" -> {
                        val stakeId = data.optString("stakeId", "stake_500")
                        val playersArray = data.getJSONArray("players")
                        val players = (0 until playersArray.length()).map { i ->
                            parsePlayer(playersArray.getJSONObject(i))
                        }
                        onEventReceived?.invoke(OnlineGameEvent.SyncRoom(stakeId, players))
                    }
                    "GAME_STARTED" -> {
                        val trump = parseCard(data.getJSONObject("trumpCard"))
                        val pot = data.optLong("pot", 0L)
                        val ante = data.optLong("ante", 500L)
                        val turnIndex = data.optInt("turnIndex", 0)

                        val handsObj = data.getJSONObject("hands")
                        val handsMap = mutableMapOf<String, List<Card>>()
                        val keys = handsObj.keys()
                        while (keys.hasNext()) {
                            val pId = keys.next()
                            val cardsArr = handsObj.getJSONArray(pId)
                            val cards = (0 until cardsArr.length()).map { i ->
                                parseCard(cardsArr.getJSONObject(i))
                            }
                            handsMap[pId] = cards
                        }
                        val excludedSuitName = data.optString("excludedSuit", Suit.SPADES.name)
                        val excludedSuit = try { Suit.valueOf(excludedSuitName) } catch (e: Exception) { Suit.SPADES }
                        onEventReceived?.invoke(OnlineGameEvent.GameStarted(trump, handsMap, pot, ante, turnIndex, excludedSuit))
                    }
                    "BET_ACTION" -> {
                        val pId = data.getString("playerId")
                        val action = data.getString("action")
                        val amount = data.optLong("amount", 0L)
                        onEventReceived?.invoke(OnlineGameEvent.BetAction(pId, action, amount))
                    }
                    "PLAY_CARD" -> {
                        val pId = data.getString("playerId")
                        val card = parseCard(data.getJSONObject("card"))
                        onEventReceived?.invoke(OnlineGameEvent.CardPlayed(pId, card))
                    }
                    "CHAT" -> {
                        val sId = data.getString("senderId")
                        val sName = data.getString("senderName")
                        val text = data.getString("text")
                        onEventReceived?.invoke(OnlineGameEvent.ChatReceived(sId, sName, text))
                    }
                    "NEXT_ROUND" -> {
                        val round = data.optInt("round", 1)
                        onEventReceived?.invoke(OnlineGameEvent.NextRound(round))
                    }
                    "PLAYER_LEFT" -> {
                        val pId = data.getString("playerId")
                        onEventReceived?.invoke(OnlineGameEvent.PlayerLeft(pId))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("OnlineAzi", "Error parsing incoming online message: ${e.message}", e)
        }
    }

    /**
     * Broadcasts an event to all players in the room via HTTP publish.
     */
    fun broadcast(type: String, dataBuilder: JSONObject.() -> Unit) {
        val topic = currentRoomTopic ?: return
        scope.launch(Dispatchers.IO) {
            try {
                val dataObj = JSONObject().apply(dataBuilder)
                val wrapper = JSONObject().apply {
                    put("type", type)
                    put("senderDeviceId", localDeviceId)
                    put("msgId", UUID.randomUUID().toString())
                    put("timestamp", System.currentTimeMillis())
                    put("data", dataObj)
                }

                val postUrl = "https://ntfy.sh/$topic"
                val requestBody = wrapper.toString().toRequestBody("text/plain; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url(postUrl)
                    .post(requestBody)
                    .header("Title", "Azi Move")
                    .build()

                client.newCall(request).execute().use { resp ->
                    if (!resp.isSuccessful) {
                        Log.w("OnlineAzi", "Broadcast $type failed with HTTP ${resp.code}")
                    }
                }
            } catch (e: Exception) {
                Log.e("OnlineAzi", "Failed to broadcast $type: ${e.message}", e)
            }
        }
    }

    // Serialization Helpers
    fun cardToJson(card: Card): JSONObject {
        return JSONObject().apply {
            put("suit", card.suit.name)
            put("rank", card.rank.name)
        }
    }

    fun parseCard(obj: JSONObject): Card {
        val suit = Suit.valueOf(obj.getString("suit"))
        val rank = Rank.valueOf(obj.getString("rank"))
        return Card(suit, rank)
    }

    fun playerToJson(player: Player): JSONObject {
        return JSONObject().apply {
            put("id", player.id)
            put("name", player.name)
            put("isUser", false) // In remote receiver's eyes, this is a remote player
            put("tengeBalance", player.tengeBalance)
            put("ratingPoints", player.ratingPoints)
            put("avatarEmoji", player.avatarEmoji)
            put("avatarBgColor", player.avatarBgColor)
            put("tricksWonCount", player.tricksWonCount)
            put("hasFolded", player.hasFolded)
            put("currentBet", player.currentBet)
            put("deviceId", localDeviceId)
        }
    }

    fun parsePlayer(obj: JSONObject): Player {
        return Player(
            id = obj.getString("id"),
            name = obj.getString("name"),
            isUser = false, // Remote player
            tengeBalance = obj.optLong("tengeBalance", 50000L),
            ratingPoints = obj.optInt("ratingPoints", 1250),
            avatarEmoji = obj.optString("avatarEmoji", "🐎"),
            avatarBgColor = obj.optLong("avatarBgColor", 0xFF1565C0),
            tricksWonCount = obj.optInt("tricksWonCount", 0),
            hasFolded = obj.optBoolean("hasFolded", false),
            currentBet = obj.optLong("currentBet", 0L)
        )
    }

    fun disconnect() {
        pollingJob?.cancel()
        pollingJob = null
        try {
            currentWebSocket?.close(1000, "Leaving room")
        } catch (e: Exception) {
            // ignore
        }
        currentWebSocket = null
        _status.value = NetworkConnectionStatus.DISCONNECTED
        currentRoomTopic = null
    }
}
