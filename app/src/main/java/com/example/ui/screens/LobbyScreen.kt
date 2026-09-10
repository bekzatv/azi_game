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
fun LobbyScreen(viewModel: AziGameViewModel, onStartGame: () -> Unit) {
    val user by viewModel.userPlayer.collectAsState()
    val deck by viewModel.selectedDeck.collectAsState()
    val decks by viewModel.availableDecks.collectAsState()
    val excluded by viewModel.excludedSuit.collectAsState()
    val history by viewModel.matchHistory.collectAsState()
    var dialog by remember { mutableStateOf("") }
    var bots by remember { mutableIntStateOf(2) }
    Column(Modifier.fillMaxSize().background(KazakhNavyDark)
        .verticalScroll(rememberScrollState()).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("AZI", color = KazakhGoldLight, fontSize = 32.sp,
                    letterSpacing = 7.sp, fontWeight = FontWeight.Black)
                Text("КАРТОЧНЫЙ КЛУБ", color = Color(0xFF91A4C1), fontSize = 10.sp, letterSpacing = 2.sp)
            }
            OutlinedButton(onClick = { dialog = "name" }, shape = RoundedCornerShape(16.dp)) {
                Text(user.avatarEmoji + "  " + user.name, maxLines = 1, color = Color.White,
                    modifier = Modifier.width(130.dp))
            }
        }
        Card(shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = KazakhNavy),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF31415D))) {
            Row(Modifier.fillMaxWidth().padding(22.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("ТВОЙ СТОЛ.\nТВОЯ ИГРА.", color = Color.White, fontSize = 25.sp,
                        lineHeight = 30.sp, fontWeight = FontWeight.Bold)
                    Text("Три карты. Один победитель.\nСобери друзей за столом.",
                        color = Color(0xFFADC0D7), fontSize = 13.sp, lineHeight = 20.sp)
                    Text("БАЛАНС  " + formatTenge(user.tengeBalance),
                        color = KazakhGold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                KazakhCardView(null, false, deck, cardWidth = 92.dp, elevation = 10.dp,
                    modifier = Modifier.padding(start = 10.dp))
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Вместе интереснее", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Стол по приглашению для 2–6 друзей", color = Color(0xFF91A4C1), fontSize = 14.sp)
            Button(onClick = { dialog = "create" }, Modifier.fillMaxWidth().height(56.dp)
                .testTag("create_room_button"), shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KazakhGold)) {
                Icon(Icons.Default.Group, null)
                Spacer(Modifier.width(10.dp))
                Text("Создать комнату", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(onClick = { dialog = "join" }, Modifier.fillMaxWidth().height(56.dp)
                .testTag("join_room_button"), shape = RoundedCornerShape(18.dp)) {
                Text("Войти по приглашению", color = KazakhGoldLight, fontSize = 16.sp)
            }
        }
        Card(shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = KazakhNavy)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SmartToy, null, tint = KazakhTurquoise)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Партия с ботами", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Всего игроков за столом: ${bots + 1}", color = Color(0xFF91A4C1), fontSize = 13.sp)
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..5).forEach { count ->
                        androidx.compose.material3.FilterChip(selected = bots == count,
                            onClick = { bots = count }, label = { Text("${count + 1}", modifier = Modifier.testTag("player_count_${count + 1}")) },
                            modifier = Modifier.weight(1f))
                    }
                }
                Button(onClick = { dialog = "suit" }, Modifier.fillMaxWidth().height(50.dp)
                    .testTag("practice_button"), shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KazakhTurquoise)) {
                    Text("Быстрая партия", color = KazakhNavyDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Коллекция колод", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("12 рубашек · все доступны", color = Color(0xFF91A4C1), fontSize = 13.sp)
            }
            IconButton(onClick = { dialog = "decks" }) {
                Icon(Icons.Default.Palette, "Все колоды", tint = KazakhGold)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            decks.take(3).forEach { item ->
                Column(Modifier.weight(1f).clickable { viewModel.selectDeck(item) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    KazakhCardView(null, false, item, cardWidth = 82.dp, isHighlighted = item.id == deck.id)
                    Text(item.name.substringBefore(" •"), color = if(item.id == deck.id) KazakhGold else Color.White,
                        fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(if(item.id == deck.id) "Выбрана" else "Выбрать", color = Color(0xFF91A4C1), fontSize = 11.sp)
                }
            }
        }
        if (history.isNotEmpty()) MatchHistorySection(matchHistory = history)
        OutlinedButton(onClick = { dialog = "about" }, Modifier.fillMaxWidth()) {
            Text("Об игре и авторе", color = Color(0xFF91A4C1))
        }
        Spacer(Modifier.height(8.dp))
    }
    when(dialog) {
        "decks" -> CustomDeckDialog(decks, deck, onSelectDeck = { viewModel.selectDeck(it) }, onDismiss = { dialog = "" })
        "name" -> EditPlayerNameDialog(user.name, { dialog = "" }, { viewModel.updateUserName(it); dialog = "" })
        "about" -> AboutAuthorDialog(onDismiss = { dialog = "" }, onResetStats = { viewModel.resetPlayerStats() })
        "suit" -> SelectSuitBeforeGameDialog(excluded, { dialog = "" }) {
            dialog = ""; viewModel.startSinglePlayerGame(bots, it); onStartGame()
        }
        "create" -> CreateOnlineRoomDialog(user.name, excluded, { dialog = "" }) { stake, code, max, name, suit ->
            dialog = ""; viewModel.createOnlineWaitingRoom(stake, code, max, name, suit); onStartGame()
        }
        "join" -> JoinOnlineRoomDialog(user.name, { dialog = "" }) { code, name ->
            dialog = ""; viewModel.joinOnlineRoomByCode(code, name); onStartGame()
        }
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
    val generatedCode = remember { com.example.network.RoomCode.generate() }
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
            Column(Modifier.verticalScroll(rememberScrollState())) {
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
                    (2..6).map { it to it.toString() }.forEach { (count, label) ->
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
                    text = "Код или приглашение от друга:",
                    color = Color(0xFFECEFF1),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = roomCodeInput,
                    onValueChange = { roomCodeInput = it.take(160) },
                    placeholder = { Text("AZI-XXXXXXXXXX", color = Color.Gray) },
                    isError = roomCodeInput.isNotBlank() && com.example.network.RoomCode.normalize(roomCodeInput) == null,
                    supportingText = { Text("Вставьте код из сообщения друга") },
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
                            val code = com.example.network.RoomCode.normalize(roomCodeInput) ?: return@Button
                            val finalName = if (userNameInput.isNotBlank()) userNameInput.trim() else initialUserName
                            onJoin(code, finalName)
                        },
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(containerColor = KazakhGold),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Войти", color = KazakhNavyDark, fontWeight = FontWeight.Bold)
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
                                            text = if (isExcluded) "Масть исключена" else "9 карт в колоде",
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
                                        text = if (isExcluded) "Убрана" else "В игре",
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
