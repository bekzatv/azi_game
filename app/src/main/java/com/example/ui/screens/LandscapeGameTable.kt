package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.network.NetworkConnectionStatus
import com.example.ui.components.formatTenge
import com.example.ui.theme.*
import com.example.viewmodel.AziGameViewModel

/** Full-width felt with a contextual bottom action bar, never a permanent side rail. */
@Composable
fun LandscapeGameTable(viewModel: AziGameViewModel, onBack: () -> Unit, onMenu: (Int) -> Unit) {
    val players by viewModel.players.collectAsState()
    val userFallback by viewModel.userPlayer.collectAsState()
    val phase by viewModel.gamePhase.collectAsState()
    val turnIndex by viewModel.currentTurnIndex.collectAsState()
    val seconds by viewModel.turnTimerSeconds.collectAsState()
    val pot by viewModel.potTenge.collectAsState()
    val trump by viewModel.trumpCard.collectAsState()
    val played by viewModel.playedCardsInTrick.collectAsState()
    val trick by viewModel.currentTrick.collectAsState()
    val deck by viewModel.selectedDeck.collectAsState()
    val action by viewModel.lastActionText.collectAsState()
    val deal by viewModel.currentlyDealingCard.collectAsState()
    val online by viewModel.isOnlineMode.collectAsState()
    val host by viewModel.isRoomHost.collectAsState()
    val status by viewModel.networkStatus.collectAsState()
    val error by viewModel.networkError.collectAsState()
    val call by viewModel.currentCallAmount.collectAsState()
    val sound by viewModel.isSoundEnabled.collectAsState()
    val user = players.firstOrNull { it.isUser } ?: userFallback
    val currentId = if (phase == GamePhase.BETTING || phase == GamePhase.PLAYING_TRICKS)
        players.getOrNull(turnIndex)?.id else null
    val userTurn = currentId == user.id && !user.hasFolded
    val canSend = !online || status == NetworkConnectionStatus.CONNECTED
    var menu by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().testTag("landscape_table")) {
        Row(Modifier.fillMaxWidth().height(44.dp).background(KazakhNavy),
            verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("‹ Лобби", color = KazakhGold) }
            Text(if (online) "АЗИ · Друзья" else "АЗИ · Практика", color = KazakhGold,
                fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Text("${players.size} игроков", color = TextMuted, fontSize = 11.sp)
            TextButton(onClick = { viewModel.toggleSound() }) { Text(if (sound) "Звук: вкл" else "Звук: выкл", fontSize = 11.sp) }
            Box {
                TextButton(onClick = { menu = true }) { Text("Меню") }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    listOf("Рубашки карт", "Мой рейтинг", "Правила игры", "Об авторе", "Чат").forEachIndexed { index, title ->
                        DropdownMenuItem(text = { Text(title) }, onClick = { menu = false; onMenu(index) })
                    }
                }
            }
        }
        Column(Modifier.weight(1f).fillMaxWidth().padding(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            LandscapeTableSurface(
                state = LandscapeTableState(players, user, phase, currentId, seconds, pot, trump,
                    played, trick, deck, action, deal, canSend),
                modifier = Modifier.weight(1f).fillMaxWidth(),
                onPlayCard = viewModel::playUserCard
            )
            if (error != null) Text(error.orEmpty(), color = KazakhCrimsonLight, fontSize = 12.sp, lineHeight = 15.sp)
            if ((phase == GamePhase.BETTING && userTurn) || (phase == GamePhase.WINNER_CELEBRATION && (!online || host))) {
            Row(Modifier.fillMaxWidth().testTag("table_actions"), horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically) {
                if (phase == GamePhase.BETTING && userTurn) {
                    TableBettingActions((call - user.currentBet).coerceAtLeast(0), canSend,
                        viewModel.canStartTricksAfterUserCall(), viewModel::onUserAction)
                } else if (phase == GamePhase.WINNER_CELEBRATION && (!online || host)) {
                    LandscapeAction("Следующая раздача", canSend, KazakhGold) { viewModel.startNextRound() }
                }
            }
            }
        }
    }
}

@Composable
internal fun TableBettingActions(toCall: Long, canSend: Boolean, canStart: Boolean,
    onAction: (BetActionType, Long) -> Unit) {
    var raiseMenu by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().testTag("betting_actions"), horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        LandscapeAction("Пас", canSend, Color(0xFFDE9191)) { onAction(BetActionType.FOLD, 0) }
        LandscapeAction(if (toCall == 0L) "Чек" else "Уравнять ${formatTenge(toCall)}",
            canSend, KazakhTurquoise) { onAction(BetActionType.CALL, 0) }
        Box {
            LandscapeAction("Поднять ▾", canSend, KazakhGold) { raiseMenu = true }
            DropdownMenu(expanded = raiseMenu, onDismissRequest = { raiseMenu = false }) {
                listOf(500L, 1000L, 2000L, 5000L, 10000L).forEach { value ->
                    DropdownMenuItem(text = { Text("Поднять на ${formatTenge(value)}") }, onClick = {
                        raiseMenu = false
                        onAction(BetActionType.RAISE, value)
                    })
                }
            }
        }
        Spacer(Modifier.weight(1f))
        if (canStart) LandscapeAction("Начать розыгрыш", canSend, KazakhGold) { onAction(BetActionType.SHOWDOWN, 0) }
    }
}

@Composable
private fun LandscapeAction(label: String, enabled: Boolean, color: Color, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.heightIn(min = 44.dp),
        shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = KazakhNavyDark)) {
        Text(label, fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.Bold)
    }
}
