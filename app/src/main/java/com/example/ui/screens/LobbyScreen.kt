package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.RoomStake
import com.example.model.StandardStakes
import com.example.model.Suit
import com.example.ui.components.AboutAuthorCard
import com.example.ui.components.AboutAuthorDialog
import com.example.ui.components.CustomDeckDialog
import com.example.ui.components.KazakhCardView
import com.example.ui.components.LeaderboardSheet
import com.example.ui.components.MatchHistorySection
import com.example.ui.components.UserProfileDialog
import com.example.ui.components.formatTenge
import com.example.ui.theme.KazakhGold
import com.example.ui.theme.KazakhGoldDark
import com.example.ui.theme.KazakhGoldLight
import com.example.ui.theme.KazakhNavy
import com.example.ui.theme.KazakhNavyDark
import com.example.ui.theme.KazakhTurquoise
import com.example.ui.theme.KazakhTurquoiseDark
import com.example.viewmodel.AziGameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    viewModel: AziGameViewModel,
    onStartGame: () -> Unit
) {
    val userPlayer by viewModel.userPlayer.collectAsState()
    val matchHistory by viewModel.matchHistory.collectAsState()
    val selectedDeck by viewModel.selectedDeck.collectAsState()
    val availableDecks by viewModel.availableDecks.collectAsState()
    val leaderboard by viewModel.leaderboard.collectAsState()
    val excludedSuit by viewModel.excludedSuit.collectAsState()

    var showDeckDialog by remember { mutableStateOf(false) }
    var showLeaderboardSheet by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showCreateRoomDialog by remember { mutableStateOf(false) }
    var showJoinCodeDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showSuitSelectDialog by remember { mutableStateOf(false) }
    var showAboutAuthorDialog by remember { mutableStateOf(false) }

    var selectedBotCount by remember { mutableIntStateOf(2) }
    var selectedBotStake by remember { mutableStateOf(StandardStakes.STAKE_500) }

    val leaderboardSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KazakhNavyDark)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar: Brand, Balance & Daily Bonus
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(KazakhNavy, KazakhTurquoiseDark, KazakhNavyDark)
                    )
                )
                .border(1.dp, KazakhGold.copy(alpha = 0.35f), RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // App title
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🃏", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "АЗИ",
                                color = KazakhGold,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "Карточная игра",
                                color = Color(0xFFCFD8DC),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Tengé Balance Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(KazakhNavyDark)
                            .border(1.5.dp, KazakhGold, RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "₸",
                                color = KazakhGold,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatTenge(userPlayer.tengeBalance),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Profile Strip & Fast Utility Actions (Cards Deck, Leaderboard)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(KazakhNavyDark.copy(alpha = 0.85f))
                        .border(1.dp, KazakhGold.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(KazakhTurquoise)
                            .clickable { showProfileDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = userPlayer.avatarEmoji, fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showProfileDialog = true }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userPlayer.name,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Профиль",
                                tint = KazakhGold,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Text(
                            text = "${userPlayer.ratingPoints} RP • Побед: ${userPlayer.handsWon} • Ази: ${userPlayer.aziCount}",
                            color = Color(0xFFFFE082),
                            fontSize = 10.sp
                        )
                    }

                    // Profile Details Button
                    IconButton(
                        onClick = { showProfileDialog = true },
                        modifier = Modifier.testTag("user_profile_open_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Профиль игрока",
                            tint = KazakhGoldLight
                        )
                    }

                    // Deck Customization Quick Button
                    IconButton(
                        onClick = { showDeckDialog = true },
                        modifier = Modifier.testTag("custom_deck_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Колода",
                            tint = KazakhGold
                        )
                    }

                    // Leaderboard Quick Button
                    IconButton(
                        onClick = { showLeaderboardSheet = true },
                        modifier = Modifier.testTag("leaderboard_open_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Рейтинг",
                            tint = KazakhGold
                        )
                    }

                    // About Author Quick Button
                    IconButton(
                        onClick = { showAboutAuthorDialog = true },
                        modifier = Modifier.testTag("about_author_open_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Об авторе",
                            tint = KazakhGold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Блок истории последних пяти матчей в профиле
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            MatchHistorySection(
                matchHistory = matchHistory
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Content: 2 Primary Modes
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // MODE 1: ОДИНОЧНАЯ ИГРА С БОТАМИ
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp))
                    .testTag("single_player_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = KazakhNavy),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, KazakhGold)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(KazakhGold, KazakhGoldDark))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "Боты",
                                tint = KazakhNavyDark,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Одиночная игра с ботами",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Быстрый бой против умного ИИ",
                                color = Color(0xFFB0BEC5),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Выберите число ботов:",
                        color = Color(0xFFECEFF1),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Selector for number of bots: 1 bot, 2 bots, 3 bots
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val botOptions = listOf(
                            Triple(1, "1 бот", "Дуэль ⚔️"),
                            Triple(2, "2 бота", "Втроем 👥"),
                            Triple(3, "3 бота", "Вчетвером 👑")
                        )

                        botOptions.forEach { (count, title, sub) ->
                            val isSelected = selectedBotCount == count
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) KazakhGold else KazakhNavyDark)
                                    .border(
                                        1.2.dp,
                                        if (isSelected) KazakhGold else Color(0xFF455A64),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedBotCount = count }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = title,
                                        color = if (isSelected) KazakhNavyDark else Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = sub,
                                        color = if (isSelected) KazakhNavyDark.copy(alpha = 0.85f) else Color(0xFF90A4AE),
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Suit to exclude selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Убрать масть из колоды:",
                            color = Color(0xFFECEFF1),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "3 масти (27 карт)",
                            color = KazakhGold,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Suit.values().forEach { suit ->
                            val isExcluded = excludedSuit == suit
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isExcluded) Color(0xFFB71C1C).copy(alpha = 0.35f)
                                        else KazakhNavyDark
                                    )
                                    .border(
                                        width = if (isExcluded) 1.5.dp else 1.dp,
                                        color = if (isExcluded) Color(0xFFEF5350) else Color(0xFF455A64),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { viewModel.setExcludedSuit(suit) }
                                    .padding(vertical = 7.dp, horizontal = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = suit.symbol,
                                        color = if (suit.isRed) Color(0xFFE53935) else Color.White,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = suit.ruName,
                                        color = if (isExcluded) Color(0xFFFF8A80) else Color(0xFFCFD8DC),
                                        fontSize = 9.5.sp,
                                        fontWeight = if (isExcluded) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isExcluded) "Убрана ❌" else "В игре ✓",
                                        color = if (isExcluded) Color(0xFFFF5252) else Color(0xFF81C784),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            showSuitSelectDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("start_bot_game_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = KazakhGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Играть",
                            tint = KazakhNavyDark
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Играть (без ${excludedSuit.ruName})",
                            color = KazakhNavyDark,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // MODE 2: ИГРА ОНЛАЙН С ДРУЗЬЯМИ
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp))
                    .testTag("online_player_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = KazakhNavy),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, KazakhTurquoise)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(KazakhTurquoise, KazakhTurquoiseDark))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = "Друзья",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Игра онлайн с друзьями",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Приватные столы и голосовой чат",
                                color = Color(0xFFB0BEC5),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Create Room Button
                        Button(
                            onClick = { showCreateRoomDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("create_room_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = KazakhTurquoise),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Создать",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        // Join by Code Button
                        OutlinedButton(
                            onClick = { showJoinCodeDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("join_code_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = KazakhGold),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, KazakhGold),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Войти по коду",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Creator Credit Footer (Clickable to open About Author)
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(12.dp))
                .background(KazakhNavyDark.copy(alpha = 0.75f))
                .border(1.dp, KazakhGoldDark.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .clickable { showAboutAuthorDialog = true }
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.creator_credit),
                    color = KazakhGold,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.3.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "ℹ️", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Dialogs & Sheets
    if (showAboutAuthorDialog) {
        AboutAuthorDialog(
            onDismiss = { showAboutAuthorDialog = false },
            onResetStats = { viewModel.resetPlayerStats() }
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

    if (showProfileDialog) {
        UserProfileDialog(
            userPlayer = userPlayer,
            matchHistory = matchHistory,
            onDismiss = { showProfileDialog = false },
            onEditName = {
                showProfileDialog = false
                showEditNameDialog = true
            },
            onResetStats = { viewModel.resetPlayerStats() }
        )
    }

    if (showEditNameDialog) {
        EditPlayerNameDialog(
            currentName = userPlayer.name,
            onDismiss = { showEditNameDialog = false },
            onSave = { newName ->
                viewModel.updateUserName(newName)
                showEditNameDialog = false
            }
        )
    }

    if (showSuitSelectDialog) {
        SelectSuitBeforeGameDialog(
            currentExcludedSuit = excludedSuit,
            onDismiss = { showSuitSelectDialog = false },
            onConfirm = { chosenSuit ->
                showSuitSelectDialog = false
                viewModel.setExcludedSuit(chosenSuit)
                viewModel.startSinglePlayerGame(selectedBotCount, chosenSuit)
                onStartGame()
            }
        )
    }

    if (showCreateRoomDialog) {
        CreateOnlineRoomDialog(
            initialUserName = userPlayer.name,
            initialExcludedSuit = excludedSuit,
            onDismiss = { showCreateRoomDialog = false },
            onCreate = { stake, code, maxPlayers, userName, chosenSuit ->
                showCreateRoomDialog = false
                viewModel.createOnlineWaitingRoom(stake, code, maxPlayers, userName, chosenSuit)
                onStartGame()
            }
        )
    }

    if (showJoinCodeDialog) {
        JoinOnlineRoomDialog(
            initialUserName = userPlayer.name,
            onDismiss = { showJoinCodeDialog = false },
            onJoin = { code, userName ->
                showJoinCodeDialog = false
                viewModel.joinOnlineRoomByCode(code, userName)
                onStartGame()
            }
        )
    }
}

@Composable
fun EditPlayerNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var nameInput by remember { mutableStateOf(currentName) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(KazakhNavy)
                .border(1.5.dp, KazakhGold, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "Ваше имя в игре ✏️",
                    color = KazakhGold,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Имя будут видеть друзья и соперники за столом:",
                    color = Color(0xFFB0BEC5),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { if (it.length <= 20) nameInput = it },
                    placeholder = { Text("Введите имя...", color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = KazakhGold,
                        unfocusedBorderColor = Color(0xFF455A64)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Отмена", color = Color(0xFFB0BEC5))
                    }
                    Button(
                        onClick = {
                            if (nameInput.isNotBlank()) onSave(nameInput.trim())
                        },
                        enabled = nameInput.isNotBlank(),
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(containerColor = KazakhGold),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Сохранить", color = KazakhNavyDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CreateOnlineRoomDialog(
    initialUserName: String,
    initialExcludedSuit: Suit = Suit.SPADES,
    onDismiss: () -> Unit,
    onCreate: (RoomStake, String, Int, String, Suit) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var userNameInput by remember { mutableStateOf(initialUserName) }
    var selectedStake by remember { mutableStateOf(StandardStakes.STAKE_500) }
    var selectedPlayersCount by remember { mutableIntStateOf(3) }
    var selectedExcludedSuit by remember { mutableStateOf(initialExcludedSuit) }
    val generatedCode = remember { "AZI-${(100..999).random()}" }
    var copiedCode by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(KazakhNavy)
                .border(1.5.dp, KazakhTurquoise, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "Создать комнату для друзей 👥",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Код комнаты с копированием в один клик
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(KazakhNavyDark)
                        .border(1.dp, KazakhGold, RoundedCornerShape(12.dp))
                        .clickable {
                            clipboardManager.setText(AnnotatedString(generatedCode))
                            copiedCode = true
                        }
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Код комнаты (нажмите скопировать):", color = Color(0xFFB0BEC5), fontSize = 10.5.sp)
                            Text(text = generatedCode, color = KazakhGold, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        }
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Скопировать",
                            tint = KazakhGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (copiedCode) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "✓ Код скопирован в буфер обмена!",
                        color = Color(0xFF81C784),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Поле ввода имени
                Text(
                    text = "Ваше имя за столом:",
                    color = Color(0xFFECEFF1),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = userNameInput,
                    onValueChange = { if (it.length <= 20) userNameInput = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = KazakhTurquoise,
                        unfocusedBorderColor = Color(0xFF455A64)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Число игроков в комнате
                Text(
                    text = "Число игроков в комнате:",
                    color = Color(0xFFECEFF1),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        Pair(2, "2 игрока"),
                        Pair(3, "3 игрока"),
                        Pair(4, "4 игрока")
                    ).forEach { (count, label) ->
                        val isSelected = selectedPlayersCount == count
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) KazakhTurquoise else KazakhNavyDark)
                                .border(1.dp, if (isSelected) KazakhTurquoise else Color(0xFF455A64), RoundedCornerShape(8.dp))
                                .clickable { selectedPlayersCount = count }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else Color(0xFFB0BEC5),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Выбор убираемой масти
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Убрать масть (3 масти, 27 карт):",
                        color = Color(0xFFECEFF1),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Suit.values().forEach { suit ->
                        val isExcluded = selectedExcludedSuit == suit
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isExcluded) Color(0xFFB71C1C).copy(alpha = 0.35f) else KazakhNavyDark)
                                .border(1.dp, if (isExcluded) Color(0xFFEF5350) else Color(0xFF455A64), RoundedCornerShape(8.dp))
                                .clickable { selectedExcludedSuit = suit }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = suit.symbol,
                                    color = if (suit.isRed) Color(0xFFE53935) else Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = suit.ruName,
                                    color = if (isExcluded) Color(0xFFFF8A80) else Color(0xFFB0BEC5),
                                    fontSize = 9.sp,
                                    fontWeight = if (isExcluded) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = if (isExcluded) "Убрана ❌" else "В игре ✓",
                                    color = if (isExcluded) Color(0xFFFF5252) else Color(0xFF81C784),
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Выберите ставку стола:",
                    color = Color(0xFFB0BEC5),
                    fontSize = 11.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))

                StandardStakes.allStakes.forEach { stake ->
                    val isSelected = selectedStake.id == stake.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) KazakhTurquoise.copy(alpha = 0.25f) else KazakhNavyDark)
                            .border(1.dp, if (isSelected) KazakhTurquoise else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { selectedStake = stake }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stake.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = "${stake.anteTenge}₸", color = KazakhGold, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Отмена", color = Color(0xFFB0BEC5))
                    }
                    Button(
                        onClick = {
                            val finalName = if (userNameInput.isNotBlank()) userNameInput.trim() else initialUserName
                            onCreate(selectedStake, generatedCode, selectedPlayersCount, finalName, selectedExcludedSuit)
                        },
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(containerColor = KazakhTurquoise),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Создать стол", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun JoinOnlineRoomDialog(
    initialUserName: String,
    onDismiss: () -> Unit,
    onJoin: (String, String) -> Unit
) {
    var userNameInput by remember { mutableStateOf(initialUserName) }
    var roomCodeInput by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(KazakhNavy)
                .border(1.5.dp, KazakhGold, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "Подключение по коду 🔑",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Ваше имя:",
                    color = Color(0xFFECEFF1),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = userNameInput,
                    onValueChange = { if (it.length <= 20) userNameInput = it },
                    placeholder = { Text("Ваше имя...", color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = KazakhGold,
                        unfocusedBorderColor = Color(0xFF455A64)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Код комнаты от друга (например AZI-777):",
                    color = Color(0xFFECEFF1),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = roomCodeInput,
                    onValueChange = { roomCodeInput = it.uppercase() },
                    placeholder = { Text("AZI-...", color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = KazakhGold,
                        unfocusedBorderColor = Color(0xFF455A64)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Отмена", color = Color(0xFFB0BEC5))
                    }
                    Button(
                        onClick = {
                            val code = if (roomCodeInput.isNotBlank()) roomCodeInput.trim() else "AZI-777"
                            val finalName = if (userNameInput.isNotBlank()) userNameInput.trim() else initialUserName
                            onJoin(code, finalName)
                        },
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(containerColor = KazakhGold),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Присоединиться", color = KazakhNavyDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SelectSuitBeforeGameDialog(
    currentExcludedSuit: Suit,
    onDismiss: () -> Unit,
    onConfirm: (Suit) -> Unit
) {
    var selectedSuit by remember { mutableStateOf(currentExcludedSuit) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(KazakhNavy)
                .border(2.dp, KazakhGold, RoundedCornerShape(22.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Выбор масти перед игрой 🃏",
                    color = KazakhGold,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "В Ази играют 27 картами (3 масти). Выберите одну масть, которую нужно убрать из колоды:",
                    color = Color(0xFFCFD8DC),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Suit.values().forEach { suit ->
                        val isExcluded = selectedSuit == suit
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isExcluded) Color(0xFFB71C1C).copy(alpha = 0.28f)
                                    else KazakhNavyDark
                                )
                                .border(
                                    width = if (isExcluded) 2.dp else 1.dp,
                                    color = if (isExcluded) Color(0xFFEF5350) else Color(0xFF455A64),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedSuit = suit }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = suit.symbol,
                                        color = if (suit.isRed) Color(0xFFE53935) else Color.White,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "${suit.ruName} (${suit.kazakhName})",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (isExcluded) "Убрана из колоды (0 карт)" else "Остается в игре (9 карт: 6..Т)",
                                            color = if (isExcluded) Color(0xFFFF8A80) else Color(0xFF81C784),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isExcluded) Color(0xFFD32F2F) else Color(0xFF2E7D32).copy(alpha = 0.35f)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isExcluded) "❌ УБРАНА" else "✓ В ИГРЕ",
                                        color = if (isExcluded) Color.White else Color(0xFF81C784),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Отмена", color = Color(0xFFB0BEC5))
                    }

                    Button(
                        onClick = { onConfirm(selectedSuit) },
                        modifier = Modifier.weight(1.4f),
                        colors = ButtonDefaults.buttonColors(containerColor = KazakhGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Начать игру ➔",
                            color = KazakhNavyDark,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
