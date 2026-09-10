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
    data class SyncRoom(val stakeId: String, val players: List<Player>, val maxPlayers: Int = 3, val excludedSuit: Suit = Suit.SPADES, val readyIds: Set<String> = emptySet()) : OnlineGameEvent()
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
    data class RoomRejected(val playerId: String, val reason: String) : OnlineGameEvent()
    data class ReadyChanged(val playerId: String, val ready: Boolean) : OnlineGameEvent()
    data class PlayerLeft(val playerId: String) : OnlineGameEvent()
    data class NextRound(val roundNumber: Int) : OnlineGameEvent()
}

/**
 * Real-time peer pub/sub multiplayer engine over secure WebSocket & HTTP relay.
 * Allows multiple physical Android devices anywhere in the world to connect by room code (e.g. AZI-777).
 */
class OnlineMultiplayerManager(
    private val scope: CoroutineScope,
    private val serverUrl: String = "https://ntfy.sh"
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // For persistent WebSocket
        .writeTimeout(10, TimeUnit.SECONDS)
        .pingInterval(15, TimeUnit.SECONDS)
        .build()

    private val publishClient by lazy { client.newBuilder().readTimeout(15, TimeUnit.SECONDS).build() }
    @Volatile private var generation = 0L
    private var connectionCallback: (() -> Unit)? = null
    private var connectedOnce = false
    @Volatile private var cursor = ""
    private val publishMutex = kotlinx.coroutines.sync.Mutex()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    private val _deliveryFailed = MutableStateFlow(false)
    val deliveryFailed: StateFlow<Boolean> = _deliveryFailed.asStateFlow()

    private fun markConnected(token: Long) {
        scope.launch(Dispatchers.Main) {
            if (token != generation) return@launch
            _status.value = NetworkConnectionStatus.CONNECTED
            if (!_deliveryFailed.value) _errorMessage.value = null
            if (!connectedOnce) {
                connectedOnce = true
                connectionCallback?.invoke()
            }
        }
    }

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

        val code = RoomCode.normalize(rawRoomCode)
        if (code == null) {
            _errorMessage.value = "Проверьте код приглашения"
            _status.value = NetworkConnectionStatus.ERROR
            return
        }
        val topic = "azi_v4_" + code.replace("-", "_")
        currentRoomTopic = topic
        val token = generation
        cursor = (System.currentTimeMillis() / 1000).toString()
        connectionCallback = onConnected
        connectedOnce = false
        _errorMessage.value = null
        _deliveryFailed.value = false
        _status.value = NetworkConnectionStatus.CONNECTING
        val wsUrl = serverUrl.replace("https://", "wss://").replace("http://", "ws://") + "/$topic/ws"
        currentWebSocket = client.newWebSocket(Request.Builder().url(wsUrl).build(), object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                if (token != generation) webSocket.close(1000, "Old session")
            }
            override fun onMessage(webSocket: WebSocket, text: String) {
                if (token != generation) return
                _lastPingTimeMs.value = System.currentTimeMillis()
                markConnected(token)
                handleIncomingRawMessage(text, token)
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                if (token == generation) startFallbackPolling(topic, token)
            }
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                if (token == generation) startFallbackPolling(topic, token)
            }
        })
    }

    private fun startFallbackPolling(topic: String, token: Long) {
        synchronized(this) {
            if (pollingJob?.isActive == true || token != generation) return
            pollingJob = scope.launch(Dispatchers.IO) {
                var failures = 0
                while (isActive && token == generation) {
                    try {
                        val request = Request.Builder().url("$serverUrl/$topic/json?poll=1&since=$cursor").build()
                        publishClient.newCall(request).execute().use { response ->
                            check(response.isSuccessful) { "HTTP ${response.code}" }
                            val lines = response.body?.string().orEmpty()
                            markConnected(token)
                            lines.lineSequence().filter { it.isNotBlank() }.forEach { handleIncomingRawMessage(it, token) }
                        }
                        failures = 0
                    } catch (e: Exception) {
                        if (token != generation) return@launch
                        failures++
                        _status.value = NetworkConnectionStatus.ERROR
                        _errorMessage.value = "Нет связи. Повторяем подключение…"
                    }
                    delay(if (failures == 0) 1500L else (1500L * failures).coerceAtMost(15000L))
                }
            }
        }
    }

    internal fun handleIncomingRawMessage(rawText: String, token: Long = generation) {
        try {
            if (token != generation) return
            val ntfyObj = JSONObject(rawText)
            val eventType = ntfyObj.optString("event")
            if (eventType == "keepalive" || eventType == "open") {
                return
            }

            ntfyObj.optString("id").takeIf { it.isNotBlank() }?.let { cursor = it }
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
                if (token != generation) return@launch
                try {
                when (type) {
                    "ROOM_REJECTED" -> onEventReceived?.invoke(OnlineGameEvent.RoomRejected(data.getString("playerId"), data.getString("reason")))
                    "READY" -> onEventReceived?.invoke(OnlineGameEvent.ReadyChanged(data.getString("playerId"), data.optBoolean("ready")))
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
                        onEventReceived?.invoke(OnlineGameEvent.SyncRoom(stakeId, players, data.optInt("maxPlayers", 3).coerceIn(2,6), Suit.valueOf(data.optString("excludedSuit", "SPADES")), data.optJSONArray("readyIds")?.let { a -> (0 until a.length()).map { a.getString(it) }.toSet() } ?: emptySet()))
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
                } catch (e: Exception) { Log.w("OnlineAzi", "Invalid event ignored", e) }
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
        val token = generation
        // Snapshot data on the caller thread; preserve invocation order with Main + FIFO mutex.
        val wrapper = JSONObject().apply {
            put("type", type)
            put("senderDeviceId", localDeviceId)
            put("msgId", UUID.randomUUID().toString())
            put("timestamp", System.currentTimeMillis())
            put("data", JSONObject().apply(dataBuilder))
        }.toString()
        scope.launch(Dispatchers.Main) {
            publishMutex.lock()
            try {
                if (type != "PLAYER_LEFT" && token != generation) return@launch
                var delivered = false
                for (attempt in 0..2) {
                    if (type != "PLAYER_LEFT" && token != generation) return@launch
                    delivered = kotlinx.coroutines.withContext(Dispatchers.IO) {
                        try {
                            val request = Request.Builder().url("$serverUrl/$topic")
                                .post(wrapper.toRequestBody("text/plain; charset=utf-8".toMediaType())).build()
                            publishClient.newCall(request).execute().use { it.isSuccessful }
                        } catch (_: Exception) { false }
                    }
                    if (delivered) break
                    delay(750L * (attempt + 1))
                }
                if (!delivered && token == generation) {
                    _deliveryFailed.value = true
                    _errorMessage.value = "Сообщение не доставлено. Вернитесь в комнату перед новой партией."
                }
            } finally { publishMutex.unlock() }
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
        generation++
        connectionCallback = null
        connectedOnce = false
        seenMessageIds.clear()
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
