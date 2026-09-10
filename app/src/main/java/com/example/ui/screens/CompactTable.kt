package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GamePhase
import com.example.model.Player
import com.example.ui.components.KazakhCardView
import com.example.ui.components.formatTenge
import com.example.ui.theme.*
import com.example.viewmodel.AziGameViewModel

/** Bounded cells: long names cannot push another seat outside the viewport. */
@Composable
fun OpponentGrid(players: List<Player>, currentId: String?, seconds: Int, compactStats: Boolean = false) {
    var selectedId by remember { mutableStateOf<String?>(null) }
    val columns = if (players.size <= 2) players.size.coerceAtLeast(1) else 3
    Column(Modifier.fillMaxWidth().testTag("opponent_grid"), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        players.chunked(columns).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { player ->
                    val turn = player.id == currentId
                    Column(Modifier.weight(1f).testTag("seat_${player.id}")
                        .background(KazakhNavyDark, RoundedCornerShape(12.dp))
                        .border(if(turn) 1.5.dp else 1.dp, if(turn) KazakhGold else Color(0xFF344B61),
                            RoundedCornerShape(12.dp))
                        .clickable { selectedId = player.id }.padding(7.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(player.avatarEmoji, fontSize = 15.sp, lineHeight = 18.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(player.name, modifier = Modifier.weight(1f), maxLines = 1,
                                overflow = TextOverflow.Ellipsis, color = Color.White,
                                fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text(formatTenge(player.tengeBalance), color = KazakhGold, fontSize = 11.sp, lineHeight = 14.sp,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(if(player.hasFolded) "Пас" else if(turn) "Ход · ${seconds}с"
                            else if (compactStats) "Карт: ${player.cards.size}\nВзяток: ${player.tricksWonCount}"
                            else "Карт ${player.cards.size} · Взяток ${player.tricksWonCount}",
                            color = if(turn) KazakhTurquoise else TextMuted, fontSize = 9.sp, lineHeight = 12.sp,
                            maxLines = if (compactStats) 2 else 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
    players.firstOrNull { it.id == selectedId }?.let { player ->
        AlertDialog(onDismissRequest = { selectedId = null },
            title = { Text("${player.avatarEmoji} ${player.name}") },
            text = { Text("Баланс: ${formatTenge(player.tengeBalance)}\nВзяток: ${player.tricksWonCount}\n" +
                if(player.hasFolded) "Игрок спасовал" else "Карт на руках: ${player.cards.size}") },
            confirmButton = { TextButton(onClick = { selectedId = null }) { Text("Закрыть") } })
    }
}

/** Compact middle for 4–6 players. Only this region scrolls if vertical space is limited. */
@Composable
fun CompactTableCenter(viewModel: AziGameViewModel, modifier: Modifier = Modifier) {
    val phase by viewModel.gamePhase.collectAsState()
    val pot by viewModel.potTenge.collectAsState()
    val trump by viewModel.trumpCard.collectAsState()
    val trick by viewModel.currentTrick.collectAsState()
    val played by viewModel.playedCardsInTrick.collectAsState()
    val deck by viewModel.selectedDeck.collectAsState()
    val action by viewModel.lastActionText.collectAsState()
    val deal by viewModel.currentlyDealingCard.collectAsState()
    val host by viewModel.isRoomHost.collectAsState()
    val online by viewModel.isOnlineMode.collectAsState()
    Column(modifier.verticalScroll(rememberScrollState()).testTag("table_center"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Козырь ${trump?.rank?.symbol.orEmpty()}${trump?.suit?.symbol.orEmpty()}",
                color = KazakhGold, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold)
            Text("Банк ${formatTenge(pot)}", color = KazakhGoldLight, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold)
        }
        when(phase) {
            GamePhase.PLAYING_TRICKS, GamePhase.TRICK_RESULT -> {
                Text("Взятка $trick / 3", color = KazakhTurquoise, fontSize = 11.sp)
                if(played.isEmpty()) Text("Ожидаем карту", color = TextMuted, fontSize = 12.sp)
                played.chunked(3).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        row.forEach { play ->
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                KazakhCardView(play.card, true, deck, cardWidth = 34.dp)
                                Text(play.playerName, fontSize = 9.sp, lineHeight = 12.sp, color = Color.White, maxLines = 1,
                                    overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 3.dp))
                            }
                        }
                    }
                }
            }
            GamePhase.DEALING -> {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = KazakhGold)
                Text("Раздача · ${deal?.targetPlayerName.orEmpty()}", color = KazakhGoldLight,
                    fontSize = 12.sp, lineHeight = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            GamePhase.SVARA -> Text("АЗИ! Банк остаётся на кону", color = KazakhGold, fontSize = 16.sp,
                fontWeight = FontWeight.Bold, modifier = Modifier.testTag("azi_draw_banner"))
            GamePhase.WINNER_CELEBRATION -> {
                if(!online || host) Button(onClick = { viewModel.startNextRound() }) { Text("Следующая раздача") }
                else Text("Ждём следующую раздачу", color = TextMuted, fontSize = 12.sp)
            }
            else -> Unit
        }
        Text(action, color = Color.White, fontSize = 11.sp, lineHeight = 14.sp, maxLines = 2,
            overflow = TextOverflow.Ellipsis)
    }
}
