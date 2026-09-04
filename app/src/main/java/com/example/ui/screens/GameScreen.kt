package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.model.AziEvaluator
import com.example.model.BetActionType
import com.example.model.Card
import com.example.model.DealingCardAnimationState
import com.example.model.GamePhase
import com.example.model.Player
import com.example.model.Suit
import com.example.network.NetworkConnectionStatus
import com.example.ui.components.AboutAuthorDialog
import com.example.ui.components.CustomDeckDialog
import com.example.ui.components.InGameChatSheet
import com.example.ui.components.KazakhCardView
import com.example.ui.components.LeaderboardSheet
import com.example.ui.components.PotView
import com.example.ui.components.SpeakingPulseEffect
import com.example.ui.components.TengeChip
import com.example.ui.components.VoiceChatControlBar
import com.example.ui.components.formatTenge
import com.example.ui.theme.KazakhCrimson
import com.example.ui.theme.KazakhCrimsonLight
import com.example.ui.theme.KazakhEmerald
import com.example.ui.theme.KazakhGold
import com.example.ui.theme.KazakhGoldDark
import com.example.ui.theme.KazakhGoldLight
import com.example.ui.theme.KazakhNavy
import com.example.ui.theme.KazakhNavyDark
import com.example.ui.theme.KazakhTurquoise
import com.example.ui.theme.TableBorderWood
import com.example.ui.theme.TableFeltDark
import com.example.ui.theme.TableFeltGreen
import com.example.viewmodel.AziGameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: AziGameViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val userPlayer by viewModel.userPlayer.collectAsState()
    val players by viewModel.players.collectAsState()
    val gamePhase by viewModel.gamePhase.collectAsState()
    val currentTurnIndex by viewModel.currentTurnIndex.collectAsState()
    val potTenge by viewModel.potTenge.collectAsState()
    val currentCallAmount by viewModel.currentCallAmount.collectAsState()
    val isSvara by viewModel.isSvara.collectAsState()
    val selectedDeck by viewModel.selectedDeck.collectAsState()
    val availableDecks by viewModel.availableDecks.collectAsState()
    val turnTimer by viewModel.turnTimerSeconds.collectAsState()
    val lastActionText by viewModel.lastActionText.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val leaderboard by viewModel.leaderboard.collectAsState()
    val matchHistory by viewModel.matchHistory.collectAsState()
    val currentStake by viewModel.currentStake.collectAsState()

    // Trick-taking & Trump State
    val trumpCard by viewModel.trumpCard.collectAsState()
    val trumpSuit by viewModel.trumpSuit.collectAsState()
    val playedCardsInTrick by viewModel.playedCardsInTrick.collectAsState()
    val trickNumber by viewModel.currentTrick.collectAsState()
    val dealingProgress by viewModel.dealingProgress.collectAsState()
    val trickWinnerAnnounce by viewModel.trickWinnerAnnounce.collectAsState()
    val currentlyDealingCard by viewModel.currentlyDealingCard.collectAsState()
    val isOnlineMode by viewModel.isOnlineMode.collectAsState()
    val roomCode by viewModel.roomCode.collectAsState()
    val maxRoomPlayers by viewModel.maxRoomPlayers.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()
    val isRoomHost by viewModel.isRoomHost.collectAsState()
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    var showCopiedToast by remember { mutableStateOf(false) }

    // Voice Chat State
    val isMicEnabled by viewModel.voiceChatManager.isMicEnabled.collectAsState()
    val isSpeakerEnabled by viewModel.voiceChatManager.isSpeakerEnabled.collectAsState()
    val isUserSpeaking by viewModel.voiceChatManager.isUserSpeaking.collectAsState()
    val audioAmplitude by viewModel.voiceChatManager.audioAmplitude.collectAsState()
    val speakingPlayers by viewModel.voiceChatManager.speakingPlayers.collectAsState()

    // Sheet / Dialog states
    var showChatSheet by remember { mutableStateOf(false) }
    var showDeckDialog by remember { mutableStateOf(false) }
    var showLeaderboardSheet by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }
    var showAboutAuthorDialog by remember { mutableStateOf(false) }
    var showAddFriendDialog by remember { mutableStateOf(false) }
    var friendNameInput by remember { mutableStateOf("") }
    var selectedRaiseChip by remember { mutableStateOf(currentStake.minBetTenge) }

    val chatSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val leaderboardSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Permission launcher for Real-Time Microphone recording
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.voiceChatManager.setMicEnabled(true)
        }
    }

    LaunchedEffect(showCopiedToast) {
        if (showCopiedToast) {
            kotlinx.coroutines.delay(2000)
            showCopiedToast = false
        }
    }

    val user = players.firstOrNull { it.isUser } ?: userPlayer
    val isUserTurn = players.getOrNull(currentTurnIndex)?.id == user.id

    // Pulse animation for user's turn
    val infiniteTransition = rememberInfiniteTransition(label = "turnPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(KazakhNavyDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Navigation & Stake Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KazakhNavy)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            viewModel.leaveCurrentGame()
                            onNavigateBack()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = if (isOnlineMode) "Онлайн стол" else currentStake.name,
                            color = KazakhGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isOnlineMode) {
                                when (networkStatus) {
                                    NetworkConnectionStatus.CONNECTED -> "🟢 В сети"
                                    NetworkConnectionStatus.CONNECTING -> "🟡 Подключение..."
                                    NetworkConnectionStatus.ERROR -> "🔴 Ошибка сети"
                                    NetworkConnectionStatus.DISCONNECTED -> "⚪ Отключено"
                                }
                            } else "Анте: ${currentStake.anteTenge}₸",
                            color = if (isOnlineMode && networkStatus == NetworkConnectionStatus.CONNECTED) Color(0xFF81C784) else Color(0xFF90A4AE),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Online Room Code Badge (clickable to copy)
                    if (isOnlineMode) {
                        val currentRoomCode = roomCode ?: "AZI-777"
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(KazakhTurquoise.copy(alpha = 0.2f))
                                .border(1.dp, KazakhTurquoise, RoundedCornerShape(8.dp))
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(currentRoomCode))
                                    showCopiedToast = true
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Скопировать код",
                                tint = KazakhTurquoise,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentRoomCode,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Top right actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.toggleSound() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("sound_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = if (isSoundEnabled) "Звуки включены" else "Звуки выключены",
                            tint = if (isSoundEnabled) KazakhGold else Color(0xFF90A4AE)
                        )
                    }

                    IconButton(
                        onClick = { showDeckDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("deck_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Style,
                            contentDescription = "Колода",
                            tint = KazakhGold
                        )
                    }

                    IconButton(
                        onClick = { showLeaderboardSheet = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("leaderboard_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Leaderboard,
                            contentDescription = "Рейтинг",
                            tint = KazakhGold
                        )
                    }

                    IconButton(
                        onClick = { showRulesDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Правила",
                            tint = Color(0xFFB0BEC5)
                        )
                    }

                    IconButton(
                        onClick = { showAboutAuthorDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("about_author_game_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Об авторе",
                            tint = KazakhGold
                        )
                    }

                    // Chat Button with badge
                    Box {
                        IconButton(
                            onClick = { showChatSheet = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "Чат",
                                tint = KazakhTurquoise
                            )
                        }
                        if (chatMessages.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(KazakhCrimson)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                }
            }

            // Green Felt Kazakh Card Table Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .shadow(16.dp, RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp))
                    .background(TableBorderWood)
                    .border(3.dp, KazakhGoldDark, RoundedCornerShape(28.dp))
                    .padding(6.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                TableFeltGreen,
                                Color(0xFF072C23),
                                TableFeltDark
                            )
                        )
                    )
            ) {
                // Opponents area (Top Row: Bot 1 and Bot 2)
                val opponents = players.filter { !it.isUser }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(top = 6.dp, start = 8.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    opponents.forEach { bot ->
                        val isTurn = players.getOrNull(currentTurnIndex)?.id == bot.id
                        val isSpeaking = speakingPlayers.contains(bot.id)

                        PlayerSeatView(
                            player = bot,
                            isCurrentTurn = isTurn,
                            isSpeaking = isSpeaking,
                            showCardsFaceUp = gamePhase == GamePhase.SHOWDOWN || gamePhase == GamePhase.WINNER_CELEBRATION,
                            deckTheme = selectedDeck,
                            turnSeconds = if (isTurn) turnTimer else 0,
                            trumpSuit = trumpSuit
                        )
                    }
                }

                // Center Table Area: Trump Card + Pot + Trick Area / Action Banner
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Trump Display Indicator & Pot in a neat row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Trump Card View (Козырь)
                        if (trumpCard != null || trumpSuit != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(KazakhNavyDark.copy(alpha = 0.92f))
                                    .border(1.5.dp, KazakhGold, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (trumpCard != null) {
                                        KazakhCardView(
                                            card = trumpCard,
                                            isFaceUp = true,
                                            deckTheme = selectedDeck,
                                            cardWidth = 40.dp,
                                            elevation = 3.dp,
                                            isTrump = true
                                        )
                                    }
                                    Column(
                                        horizontalAlignment = Alignment.Start,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "КОЗЫРЬ",
                                            color = KazakhGold,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp
                                        )
                                        val suit = trumpSuit ?: trumpCard?.suit
                                        if (suit != null) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = suit.symbol,
                                                    color = if (suit.isRed) Color(0xFFEF5350) else Color.White,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = suit.ruName,
                                                    color = if (suit.isRed) Color(0xFFFF8A80) else Color.White,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Pot View
                        PotView(
                            totalPotTenge = potTenge,
                            currentCallAmount = currentCallAmount,
                            isSvara = isSvara
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Dealing Animation: Physical cards flying to players
                    if (gamePhase == GamePhase.DEALING) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(60.dp, 84.dp)
                            ) {
                                // Deck pile in center
                                Box(
                                    modifier = Modifier
                                        .size(52.dp, 74.dp)
                                        .shadow(8.dp, RoundedCornerShape(8.dp))
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(TableBorderWood)
                                        .border(1.5.dp, KazakhGold, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🎴", fontSize = 28.sp)
                                }

                                // Card flying from deck towards player
                                if (currentlyDealingCard != null) {
                                    val targetIdx = opponents.indexOfFirst { it.id == currentlyDealingCard!!.targetPlayerId }
                                    DealingCardFlightAnimation(
                                        dealState = currentlyDealingCard!!,
                                        deckTheme = selectedDeck,
                                        opponentsCount = opponents.size.coerceAtLeast(1),
                                        targetPlayerIndex = if (targetIdx >= 0) targetIdx else 0
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(KazakhNavyDark.copy(alpha = 0.9f))
                                    .border(1.dp, KazakhGold, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (currentlyDealingCard != null) {
                                        "Раздача карты для ${currentlyDealingCard?.targetPlayerName}..."
                                    } else "Раздача карт...",
                                    color = KazakhGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Online Mode: Waiting for Players mode in center of table
                    if (gamePhase == GamePhase.WAITING_FOR_PLAYERS) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.95f)
                                .shadow(14.dp, RoundedCornerShape(18.dp)),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = KazakhNavyDark.copy(alpha = 0.96f)),
                            border = androidx.compose.foundation.BorderStroke(2.dp, KazakhGold)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "ОЖИДАНИЕ ДРУЗЕЙ 👥",
                                    color = KazakhGold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // Код комнаты с копированием
                                val activeRoomCode = roomCode ?: "AZI-777"
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(KazakhNavy)
                                        .border(1.5.dp, KazakhTurquoise, RoundedCornerShape(10.dp))
                                        .clickable {
                                            clipboardManager.setText(AnnotatedString(activeRoomCode))
                                            showCopiedToast = true
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("КОД КОМНАТЫ (НАЖМИТЕ ДЛЯ КОПИРОВАНИЯ)", color = Color(0xFFB0BEC5), fontSize = 9.sp)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = activeRoomCode,
                                                color = Color.White,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = 2.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Скопировать",
                                            tint = KazakhTurquoise,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                if (showCopiedToast) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "✓ Код $activeRoomCode скопирован!",
                                        color = Color(0xFF81C784),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Игроков за столом: ${players.size} из $maxRoomPlayers",
                                    color = KazakhTurquoise,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // List of joined players
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    players.forEach { p ->
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(p.avatarBgColor))
                                                    .border(1.5.dp, if (p.isUser) KazakhGold else KazakhTurquoise, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(p.avatarEmoji, fontSize = 16.sp)
                                            }
                                            Text(
                                                text = if (p.isUser) "${p.name} (Вы)" else p.name,
                                                color = Color.White,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }

                                val (statusText, statusColor) = when (networkStatus) {
                                    NetworkConnectionStatus.CONNECTED -> "🟢 Связь установлена (Онлайн)" to Color(0xFF81C784)
                                    NetworkConnectionStatus.CONNECTING -> "🟡 Подключение к комнате..." to KazakhGold
                                    NetworkConnectionStatus.ERROR -> "🔴 Ошибка связи (Повтор...)" to KazakhCrimson
                                    NetworkConnectionStatus.DISCONNECTED -> "⚪ Отключено" to Color.Gray
                                }
                                Text(
                                    text = statusText,
                                    color = statusColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Инструкция подключения
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1A2634))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "📱 Чтобы друг присоединился: пусть нажмет «Войти по коду» в лобби и введет код $activeRoomCode",
                                        color = Color(0xFFECEFF1),
                                        fontSize = 10.5.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (isRoomHost) {
                                        OutlinedButton(
                                            onClick = { showAddFriendDialog = true },
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = KazakhTurquoise),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, KazakhTurquoise),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("+ Игрок/Бот", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                        }

                                        Button(
                                            onClick = { viewModel.startOnlineGameFromWaitingRoom() },
                                            modifier = Modifier.weight(1.3f),
                                            enabled = players.size >= 2,
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = KazakhGold,
                                                disabledContainerColor = Color(0xFF455A64)
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(
                                                text = if (players.size >= maxRoomPlayers) "Все в сборе! Начать" else if (players.size >= 2) "Начать игру" else "Ждем игроков...",
                                                color = if (players.size >= 2) KazakhNavyDark else Color(0xFF90A4AE),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 11.5.sp,
                                                maxLines = 1
                                            )
                                        }
                                    } else {
                                        // Guest mode
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(KazakhNavy)
                                                .border(1.dp, KazakhTurquoise, RoundedCornerShape(10.dp))
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "⏳ Ожидание начала от создателя комнаты...",
                                                color = KazakhGold,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.5.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Trick-Taking Center Play Area: Display cards played in the current trick!
                    if (gamePhase == GamePhase.PLAYING_TRICKS || gamePhase == GamePhase.TRICK_RESULT) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(KazakhNavyDark.copy(alpha = 0.85f))
                                .border(1.dp, KazakhTurquoise.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Взятка $trickNumber из 3",
                                color = KazakhTurquoise,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Played cards row
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (playedCardsInTrick.isEmpty()) {
                                    Text(
                                        text = "Ожидание первого хода...",
                                        color = Color(0xFF90A4AE),
                                        fontSize = 10.sp
                                    )
                                } else {
                                    playedCardsInTrick.forEach { played ->
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = played.playerName,
                                                color = KazakhGoldLight,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            KazakhCardView(
                                                card = played.card,
                                                isFaceUp = true,
                                                deckTheme = selectedDeck,
                                                cardWidth = 46.dp,
                                                elevation = 4.dp,
                                                isTrump = played.card.suit == trumpSuit
                                            )
                                        }
                                    }
                                }
                            }

                            // Announcement banner when trick is won
                            if (trickWinnerAnnounce != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = trickWinnerAnnounce ?: "",
                                    color = KazakhGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Action notification banner if not dealing or tricks
                    if (gamePhase != GamePhase.DEALING && gamePhase != GamePhase.PLAYING_TRICKS && gamePhase != GamePhase.TRICK_RESULT) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(KazakhNavyDark.copy(alpha = 0.85f))
                                .border(1.dp, KazakhGold.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = lastActionText,
                                color = KazakhGoldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Next Hand / Play button when round finishes
                    if (gamePhase == GamePhase.WINNER_CELEBRATION || gamePhase == GamePhase.WAITING) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.startNextRound() },
                            colors = ButtonDefaults.buttonColors(containerColor = KazakhGold),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("next_round_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Раздать",
                                tint = KazakhNavyDark
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (gamePhase == GamePhase.WAITING) "Начать игру" else "Следующая раздача",
                                color = KazakhNavyDark,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // User Hand Area (Bottom)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Turn prompt during trick-taking
                    if (gamePhase == GamePhase.PLAYING_TRICKS && isUserTurn) {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(KazakhGold.copy(alpha = pulseAlpha))
                                .padding(horizontal = 12.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "👉 ВАШ ХОД! Выберите карту для хода",
                                color = KazakhNavyDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    } else if (user.cards.isNotEmpty()) {
                        val eval = user.evaluation
                        if (eval.isAzi || eval.combinationType == com.example.model.HandCombinationType.TRIO) {
                            Box(
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (eval.isAzi) KazakhGold else KazakhNavyDark.copy(alpha = 0.9f))
                                    .border(1.dp, KazakhGold, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = eval.description,
                                    color = if (eval.isAzi) KazakhNavyDark else KazakhGoldLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    // User 3 Dealt Cards (Interactive during trick-taking!)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (user.cards.isEmpty()) {
                            repeat(3) {
                                KazakhCardView(
                                    card = null,
                                    isFaceUp = false,
                                    deckTheme = selectedDeck,
                                    cardWidth = 68.dp
                                )
                            }
                        } else {
                            val leadSuit = playedCardsInTrick.firstOrNull()?.card?.suit
                            user.cards.forEach { card ->
                                val isValidMove = leadSuit == null || AziEvaluator.isValidMove(card, user.cards, leadSuit)
                                val canPlayThisCard = gamePhase == GamePhase.PLAYING_TRICKS && isUserTurn && isValidMove

                                KazakhCardView(
                                    card = card,
                                    isFaceUp = true,
                                    deckTheme = selectedDeck,
                                    cardWidth = 72.dp,
                                    isHighlighted = user.isWinner || (gamePhase == GamePhase.PLAYING_TRICKS && isUserTurn && isValidMove),
                                    isTrump = card.suit == trumpSuit,
                                    modifier = if (canPlayThisCard) Modifier.offset(y = (-6).dp) else Modifier,
                                    onClick = if (gamePhase == GamePhase.PLAYING_TRICKS && isUserTurn) {
                                        { viewModel.playUserCard(card) }
                                    } else null
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // User Info pill with Won Tricks Counter
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(KazakhNavyDark.copy(alpha = 0.88f))
                            .border(
                                1.5.dp,
                                if (isUserTurn) KazakhGold else Color(0xFF37474F),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            SpeakingPulseEffect(isSpeaking = isUserSpeaking)
                            Text(text = user.avatarEmoji, fontSize = 16.sp)
                        }
                        Text(
                            text = user.name,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatTenge(user.tengeBalance),
                            color = KazakhGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (gamePhase == GamePhase.PLAYING_TRICKS || gamePhase == GamePhase.TRICK_RESULT) {
                            Text(
                                text = "Взятки: ${user.tricksWonCount}/3",
                                color = KazakhTurquoise,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (isUserTurn && turnTimer > 0) {
                            Text(
                                text = "⏱ ${turnTimer}с",
                                color = if (turnTimer <= 5) KazakhCrimsonLight else KazakhGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // Real-Time Voice Chat Bar
            VoiceChatControlBar(
                isMicEnabled = isMicEnabled,
                isSpeakerEnabled = isSpeakerEnabled,
                isUserSpeaking = isUserSpeaking,
                audioAmplitude = audioAmplitude,
                onToggleMic = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        viewModel.voiceChatManager.toggleMic()
                    } else {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onToggleSpeaker = { viewModel.voiceChatManager.toggleSpeaker() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 3.dp)
            )

            // User Betting Action Controls Bar (shown when gamePhase == BETTING)
            val isUserBettingTurn = isUserTurn && gamePhase == GamePhase.BETTING && !user.hasFolded

            AnimatedVisibility(
                visible = isUserBettingTurn,
                enter = slideInVertically { it } + fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KazakhNavy)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    // Quick Raise Chips row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Размер повышения:",
                            color = Color(0xFFB0BEC5),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "+${formatTenge(selectedRaiseChip)}",
                            color = KazakhGold,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val chipAmounts = listOf(500L, 1000L, 2000L, 5000L, 10000L)
                        chipAmounts.forEach { amount ->
                            TengeChip(
                                amount = amount,
                                onClick = {
                                    if (selectedRaiseChip == amount) {
                                        viewModel.onUserAction(BetActionType.RAISE, amount)
                                    } else {
                                        selectedRaiseChip = amount
                                    }
                                },
                                isSelected = selectedRaiseChip == amount
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Action Buttons Row: Пас, Чек / Колл, Поднять, Вскрыть / К взяткам
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // FOLD (Пас)
                        OutlinedButton(
                            onClick = { viewModel.onUserAction(BetActionType.FOLD) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("fold_button"),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = KazakhCrimsonLight
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, KazakhCrimsonLight),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Пас", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, maxLines = 1)
                        }

                        // CALL / CHECK (Чек / Колл)
                        val callDiff = (currentCallAmount - user.currentBet).coerceAtLeast(0L)
                        Button(
                            onClick = { viewModel.onUserAction(BetActionType.CALL) },
                            modifier = Modifier
                                .weight(1.25f)
                                .height(50.dp)
                                .testTag("call_button"),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = KazakhTurquoise),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (callDiff == 0L) "Чек" else "Уравнять",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    maxLines = 1
                                )
                                if (callDiff > 0L) {
                                    Text(
                                        text = "${callDiff}₸",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Normal,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // RAISE (Поднять)
                        Button(
                            onClick = { viewModel.onUserAction(BetActionType.RAISE, selectedRaiseChip) },
                            modifier = Modifier
                                .weight(1.25f)
                                .height(50.dp)
                                .testTag("raise_button"),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = KazakhGold),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Поднять",
                                    fontWeight = FontWeight.Black,
                                    color = KazakhNavyDark,
                                    fontSize = 11.5.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = "+${formatTenge(selectedRaiseChip)}",
                                    color = KazakhNavyDark,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }

                        // SHOWDOWN (Вскрыть / К взяткам)
                        Button(
                            onClick = { viewModel.onUserAction(BetActionType.SHOWDOWN) },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(50.dp)
                                .testTag("showdown_button"),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Вскрыть",
                                    fontWeight = FontWeight.Black,
                                    color = KazakhNavyDark,
                                    fontSize = 11.5.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = "К взяткам",
                                    color = KazakhNavyDark.copy(alpha = 0.85f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Modals / Sheets
        if (showChatSheet) {
            InGameChatSheet(
                messages = chatMessages,
                onSendMessage = { viewModel.sendUserChatMessage(it) },
                onDismiss = { showChatSheet = false },
                sheetState = chatSheetState
            )
        }

        if (showDeckDialog) {
            CustomDeckDialog(
                availableDecks = availableDecks,
                selectedDeck = selectedDeck,
                onSelectDeck = {
                    viewModel.selectDeck(it)
                    showDeckDialog = false
                },
                onDismiss = { showDeckDialog = false }
            )
        }

        if (showLeaderboardSheet) {
            LeaderboardSheet(
                currentUser = userPlayer,
                leaderboardEntries = leaderboard,
                onDismiss = { showLeaderboardSheet = false },
                sheetState = leaderboardSheetState,
                matchHistory = matchHistory,
                onResetStats = { viewModel.resetPlayerStats() }
            )
        }

        if (showRulesDialog) {
            AziRulesDialog(onDismiss = { showRulesDialog = false })
        }

        if (showAboutAuthorDialog) {
            AboutAuthorDialog(
                onDismiss = { showAboutAuthorDialog = false },
                onResetStats = { viewModel.resetPlayerStats() }
            )
        }

        if (showAddFriendDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddFriendDialog = false
                    friendNameInput = ""
                },
                containerColor = KazakhNavyDark,
                title = {
                    Text(
                        text = "Вход друга по коду",
                        color = KazakhGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Код комнаты: ${roomCode ?: "AZI-777"}",
                            color = KazakhTurquoise,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Друг вводит этот код на своем телефоне и подключается к вашему столу.",
                            color = Color(0xFFB0BEC5),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = friendNameInput,
                            onValueChange = { friendNameInput = it },
                            label = { Text("Имя друга") },
                            placeholder = { Text("Например: Марат") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = KazakhGold,
                                unfocusedBorderColor = Color.Gray,
                                focusedLabelColor = KazakhGold,
                                unfocusedLabelColor = Color.Gray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.addFriendToWaitingRoom(friendNameInput.ifBlank { null })
                            showAddFriendDialog = false
                            friendNameInput = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KazakhGold)
                    ) {
                        Text("Подключить к столу", color = KazakhNavyDark, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showAddFriendDialog = false
                        friendNameInput = ""
                    }) {
                        Text("Отмена", color = Color.White)
                    }
                }
            )
        }
    }
}

@Composable
fun PlayerSeatView(
    player: Player,
    isCurrentTurn: Boolean,
    isSpeaking: Boolean,
    showCardsFaceUp: Boolean,
    deckTheme: com.example.model.DeckTheme,
    turnSeconds: Int,
    trumpSuit: Suit?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(130.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Player Pill (Avatar, Name, Balance)
        Box(
            modifier = Modifier
                .shadow(6.dp, RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(KazakhNavyDark)
                .border(
                    width = if (isCurrentTurn) 2.dp else 1.dp,
                    color = if (isCurrentTurn) KazakhGold else Color(0xFF37474F),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Avatar with speaking effect
                Box(contentAlignment = Alignment.Center) {
                    SpeakingPulseEffect(isSpeaking = isSpeaking)
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(player.avatarBgColor)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = player.avatarEmoji, fontSize = 14.sp)
                    }
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = player.name,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (player.hasFolded) {
                            Text(
                                text = " (Пас)",
                                color = KazakhCrimsonLight,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Text(
                        text = formatTenge(player.tengeBalance),
                        color = KazakhGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Won tricks count or timer
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 2.dp)
        ) {
            if (player.tricksWonCount > 0) {
                Text(
                    text = "Взяток: ${player.tricksWonCount}",
                    color = KazakhTurquoise,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (isCurrentTurn && turnSeconds > 0) {
                Text(
                    text = "⏱ ${turnSeconds}с",
                    color = KazakhGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Cards Mini-Fan
        Row(
            horizontalArrangement = Arrangement.spacedBy((-14).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (player.cards.isEmpty()) {
                repeat(3) {
                    KazakhCardView(
                        card = null,
                        isFaceUp = false,
                        deckTheme = deckTheme,
                        cardWidth = 42.dp,
                        elevation = 2.dp
                    )
                }
            } else {
                player.cards.forEach { card ->
                    KazakhCardView(
                        card = card,
                        isFaceUp = showCardsFaceUp,
                        deckTheme = deckTheme,
                        cardWidth = 44.dp,
                        elevation = 2.dp,
                        isTrump = card.suit == trumpSuit
                    )
                }
            }
        }

        if (showCardsFaceUp && player.cards.isNotEmpty()) {
            val eval = player.evaluation
            if (eval.isAzi || eval.combinationType == com.example.model.HandCombinationType.TRIO) {
                Text(
                    text = eval.description,
                    color = KazakhGoldLight,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun AziRulesDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(KazakhNavy)
                .border(1.5.dp, KazakhGold, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Правила игры в казахскую Ази 🃏",
                    color = KazakhGold,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "1. Колода: 28 карт (от 8 до Туза).\n" +
                            "2. Раздача: Каждому игроку раздается по 3 карты, открывается козырь.\n" +
                            "3. Торговля: Игроки делают ставки (Пас, Чек, Поднять или К взяткам).\n" +
                            "4. Розыгрыш взяток: Игроки по очереди ходят одной картой. Обязательно ходить в масть первого хода! Если масти нет — бьют козырем либо сбрасывают.\n" +
                            "5. Кто забирает взятку: Игрок, положивший старшую карту масти хода, либо старший козырь.\n" +
                            "6. Победа: Кто берет 2 или 3 взятки — забирает весь банк!\n" +
                            "7. Свара: Если все взяли по 1 взятке (ничья), объявляется Свара! Банк остается на кону и разыгрывается в следующей сдаче.",
                    color = Color.White,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = KazakhTurquoise),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Понятно", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DealingCardFlightAnimation(
    dealState: DealingCardAnimationState,
    deckTheme: com.example.model.DeckTheme,
    opponentsCount: Int,
    targetPlayerIndex: Int
) {
    val animProgress = remember(dealState.cardIndex, dealState.targetPlayerId) {
        Animatable(0f)
    }

    LaunchedEffect(dealState.cardIndex, dealState.targetPlayerId) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
        )
    }

    val (targetX, targetY) = if (dealState.isUser) {
        Pair(0.dp, 160.dp)
    } else {
        val xOffset = when (opponentsCount) {
            1 -> 0.dp
            2 -> if (targetPlayerIndex == 0) (-90).dp else 90.dp
            else -> when (targetPlayerIndex) {
                0 -> (-110).dp
                1 -> 0.dp
                else -> 110.dp
            }
        }
        Pair(xOffset, (-150).dp)
    }

    val currentX = targetX * animProgress.value
    val currentY = targetY * animProgress.value
    val currentScale = 0.85f + (if (dealState.isUser) 0.35f else 0.1f) * animProgress.value
    val currentRotation = (if (dealState.isUser) -6f else 8f) * animProgress.value

    Box(
        modifier = Modifier
            .offset(x = currentX, y = currentY)
            .graphicsLayer {
                scaleX = currentScale
                scaleY = currentScale
                rotationZ = currentRotation
            }
            .shadow(10.dp, RoundedCornerShape(8.dp))
    ) {
        KazakhCardView(
            card = dealState.card,
            isFaceUp = dealState.isUser && animProgress.value > 0.45f,
            deckTheme = deckTheme,
            cardWidth = 48.dp,
            elevation = 6.dp
        )
    }
}
