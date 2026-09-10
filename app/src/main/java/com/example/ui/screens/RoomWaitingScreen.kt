package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.NetworkConnectionStatus
import com.example.network.RoomCode
import com.example.ui.theme.*
import com.example.viewmodel.AziGameViewModel

@Composable
fun RoomWaitingScreen(viewModel: AziGameViewModel, onBack: () -> Unit) {
    val players by viewModel.players.collectAsState()
    val host by viewModel.isRoomHost.collectAsState()
    val code by viewModel.roomCode.collectAsState()
    val max by viewModel.maxRoomPlayers.collectAsState()
    val confirmed by viewModel.roomConfirmed.collectAsState()
    val ready by viewModel.readyIds.collectAsState()
    val error by viewModel.roomError.collectAsState()
    val networkError by viewModel.networkError.collectAsState()
    val failed by viewModel.deliveryFailed.collectAsState()
    val status by viewModel.networkStatus.collectAsState()
    val user by viewModel.userPlayer.collectAsState()
    val stake by viewModel.currentStake.collectAsState()
    val excluded by viewModel.excludedSuit.collectAsState()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }
    val leave = { viewModel.leaveCurrentGame(); onBack() }
    BackHandler { leave() }
    Column(Modifier.fillMaxSize().background(KazakhNavyDark).verticalScroll(rememberScrollState())
        .padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        TextButton(onClick = leave) { Text("‹  В лобби", color = KazakhGold) }
        Text(if(host) "Твой стол" else "Комната друзей", color = Color.White,
            fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text(if(confirmed) "Все здесь? Подтвердите готовность." else "Проверяем приглашение…",
            color = TextMuted, fontSize = 15.sp)
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = KazakhNavy)) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("ПРИГЛАШЕНИЕ", color = KazakhTurquoise, fontSize = 11.sp, letterSpacing = 2.sp)
                Text(code.orEmpty(), color = KazakhGoldLight, fontSize = 23.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.testTag("room_code"))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { clipboard.setText(AnnotatedString(code.orEmpty())); copied = true },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                        Text(if(copied) "Скопировано" else "Копировать", fontSize = 12.sp)
                    }
                    Button(onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, RoomCode.invitation(code.orEmpty()))
                        }
                        context.startActivity(Intent.createChooser(intent, "Пригласить друзей"))
                    }, modifier = Modifier.weight(1f).heightIn(min = 48.dp), enabled = confirmed) {
                        Text("Пригласить", fontSize = 12.sp)
                    }
                }
                Text("${stake.anteTenge} ₸ · Мест: $max · без ${excluded.symbol}",
                    color = TextMuted, fontSize = 13.sp)
            }
        }
        Text("За столом ${players.size} / $max", color = Color.White, fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold)
        players.forEachIndexed { index, player ->
            Card(colors = CardDefaults.cardColors(containerColor = KazakhNavy),
                shape = RoundedCornerShape(16.dp)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(player.avatarEmoji, fontSize = 26.sp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(player.name.take(20), color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(if(index == 0 && confirmed) "Хозяин стола" else if(player.isUser) "Вы" else "Друг",
                            color = TextMuted, fontSize = 12.sp)
                    }
                    Text(if(player.id in ready) "✓ Готов" else "Ожидаем",
                        color = if(player.id in ready) KazakhTurquoise else TextMuted, fontSize = 12.sp)
                }
            }
        }
        Text(error ?: networkError ?: when {
            status != NetworkConnectionStatus.CONNECTED -> "Подключаемся к сети…"
            !confirmed -> "Ждём подтверждения хозяина комнаты"
            else -> "● Комната подключена"
        }, color = if(error != null || networkError != null) Color(0xFFFFB5AB) else KazakhTurquoise,
            fontSize = 14.sp, modifier = Modifier.testTag("room_status"))
        if(error != null || status == NetworkConnectionStatus.ERROR || failed) {
            OutlinedButton(onClick = { viewModel.retryRoomConnection() }, Modifier.fillMaxWidth()) {
                Text("Повторить подключение")
            }
        }
        if(host) {
            val canStart = confirmed && status == NetworkConnectionStatus.CONNECTED && !failed &&
                players.size >= 2 && players.all { it.id in ready }
            Button(onClick = { viewModel.startOnlineGameFromWaitingRoom() },
                enabled = canStart, modifier = Modifier.fillMaxWidth().height(56.dp).testTag("start_online"),
                shape = RoundedCornerShape(18.dp)) {
                Text(if(canStart) "Начать партию" else "Ждём готовности игроков", fontWeight = FontWeight.Bold)
            }
        } else {
            Button(onClick = { viewModel.toggleReady() },
                enabled = confirmed && status == NetworkConnectionStatus.CONNECTED && !failed,
                modifier = Modifier.fillMaxWidth().height(56.dp).testTag("ready_button"),
                shape = RoundedCornerShape(18.dp)) {
                Text(if(user.id in ready) "✓ Готов • отменить" else "Я готов играть", fontWeight = FontWeight.Bold)
            }
        }
        Text("Друг может вставить всё приглашение в поле входа. Для игры используйте одинаковую версию приложения.",
            color = TextMuted, fontSize = 13.sp, lineHeight = 20.sp)
    }
}
