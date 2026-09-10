package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.KazakhCardView
import com.example.ui.components.formatTenge
import com.example.ui.theme.*
import kotlin.math.roundToInt

internal data class LandscapeTableState(
    val players: List<Player>,
    val user: Player,
    val phase: GamePhase,
    val currentId: String?,
    val seconds: Int,
    val pot: Long,
    val trump: Card?,
    val played: List<PlayedCard>,
    val trick: Int,
    val deck: DeckTheme,
    val action: String,
    val deal: DealingCardAnimationState?,
    val canSend: Boolean
)

/** All anchors use root coordinates, so flights still reach seats after rotation or resizing. */
@Composable
internal fun LandscapeTableSurface(state: LandscapeTableState, modifier: Modifier = Modifier, onPlayCard: (Card) -> Unit) {
    val opponents = state.players.filterNot { it.id == state.user.id }
    val anchors = remember { mutableStateMapOf<String, Offset>() }
    var tableOrigin by remember { mutableStateOf(Offset.Zero) }
    var deckCenter by remember { mutableStateOf<Offset?>(null) }
    var showAction by remember { mutableStateOf(false) }
    val dealing = state.phase == GamePhase.DEALING
    BoxWithConstraints(modifier.testTag("table_surface").clipToBounds()
        .onGloballyPositioned { tableOrigin = it.boundsInRoot().topLeft }
        .background(Brush.radialGradient(listOf(Color(0xFF193D43), TableFeltDark)), RoundedCornerShape(24.dp))
        .border(2.dp, KazakhGoldDark, RoundedCornerShape(24.dp))) {
        val handWidth = if (maxHeight < 260.dp) 48.dp else 52.dp
        Column(Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            DealingSeats(opponents, state, Modifier.fillMaxWidth()) { id, center -> anchors[id] = center }
            Row(Modifier.weight(1f).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(Modifier.width(88.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("БАНК", color = TextMuted, fontSize = 11.sp, letterSpacing = 1.sp)
                    Text(formatTenge(state.pot), color = KazakhGoldLight, fontWeight = FontWeight.Bold,
                        fontSize = 17.sp, lineHeight = 21.sp, maxLines = 2)
                }
                Column(Modifier.weight(1f).testTag("trick_area"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)) {
                    if (dealing) {
                        Box(Modifier.padding(4.dp).testTag("dealing_deck")
                            .onGloballyPositioned { deckCenter = it.boundsInRoot().center }) {
                            KazakhCardView(null, false, state.deck, cardWidth = 32.dp, elevation = 2.dp)
                        }
                    } else {
                        if (state.played.isNotEmpty()) PlayedTrickCards(state.played, state.deck)
                        else Text(when (state.phase) {
                            GamePhase.SVARA -> "АЗИ!"
                            GamePhase.BETTING -> "Торги"
                            GamePhase.WINNER_CELEBRATION -> "Итоги раздачи"
                            else -> "Ожидаем карту"
                        },
                            color = if (state.phase == GamePhase.SVARA) KazakhGold else Color.White,
                            fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Column(Modifier.width(72.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("КОЗЫРЬ", color = TextMuted, fontSize = 11.sp, letterSpacing = 1.sp)
                    Text(state.trump?.let { "${it.rank.symbol}${it.suit.symbol}" } ?: "—",
                        color = KazakhTurquoise, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    if (state.phase == GamePhase.PLAYING_TRICKS || state.phase == GamePhase.TRICK_RESULT)
                        Text("Взятка ${state.trick}/3", color = Color.White, fontSize = 11.sp)
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(Modifier.weight(1f).testTag("user_profile")) {
                    Text(state.user.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(formatTenge(state.user.tengeBalance), color = KazakhGoldLight, fontSize = 14.sp)
                    Text("Взяток: ${state.user.tricksWonCount}", color = TextMuted, fontSize = 12.sp)
                }
                // Stable slots during dealing: a face appears only when the view model delivers that card.
                Row(Modifier.height(handWidth / 0.68f).testTag("user_hand"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    val count = if (dealing) 3 else state.user.cards.size
                    repeat(count) { index ->
                        val card = state.user.cards.getOrNull(index)
                        key(if (dealing) "slot_$index" else card?.id) {
                            Box(Modifier.width(handWidth).aspectRatio(0.68f).testTag("hand_slot_$index")
                                .onGloballyPositioned { anchors["${state.user.id}:$index"] = it.boundsInRoot().center }) {
                                if (card == null) Box(Modifier.fillMaxSize()
                                    .border(1.dp, KazakhGoldDark.copy(alpha = 0.25f), RoundedCornerShape(7.dp)))
                                else {
                                    val legal = AziEvaluator.isValidMove(card, state.user.cards, state.played.firstOrNull()?.card?.suit)
                                    val playable = state.phase == GamePhase.PLAYING_TRICKS && state.currentId == state.user.id &&
                                        !state.user.hasFolded && legal && state.canSend
                                    KazakhCardView(card, true, state.deck, cardWidth = handWidth, elevation = 3.dp,
                                        isHighlighted = playable || state.user.isWinner, isTrump = card.suit == state.trump?.suit,
                                        modifier = Modifier.testTag("hand_card_${card.id}")
                                            .semantics { contentDescription = "${card.rank.ruTitle}, ${card.suit.ruName}" },
                                        onClick = if (playable) ({ onPlayCard(card) }) else null)
                                }
                            }
                        }
                    }
                }
                Column(Modifier.weight(1f).clickable { showAction = true }, horizontalAlignment = Alignment.End) {
                    Text(if (state.currentId == state.user.id) "ВАШ ХОД · ${state.seconds}с"
                        else if (dealing) "РАЗДАЧА" else if (state.user.hasFolded) "ВЫ СПАСОВАЛИ" else "АЗИ",
                        color = KazakhTurquoise, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(if (state.phase == GamePhase.SVARA) "Банк остаётся на кону" else state.action,
                        color = Color.White, fontSize = 12.sp, lineHeight = 15.sp,
                        maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        val deal = state.deal
        val source = deckCenter
        val destination = deal?.let { anchors[if (it.isUser) "${it.targetPlayerId}:${it.cardIndex}" else it.targetPlayerId] }
        if (dealing && deal != null && source != null && destination != null) {
            key(deal.targetPlayerId, deal.cardIndex, deal.card.id) {
                TableDealFlight(deal, state.deck, source - tableOrigin, destination - tableOrigin)
            }
        }
    }
    if (showAction) AlertDialog(onDismissRequest = { showAction = false },
        title = { Text("Событие за столом") }, text = { Text(state.action) },
        confirmButton = { TextButton(onClick = { showAction = false }) { Text("Закрыть") } })
}

/** Cards never receive row weight: only the available maximum width controls their size. */
@Composable
internal fun PlayedTrickCards(played: List<PlayedCard>, deck: DeckTheme) {
    BoxWithConstraints(Modifier.fillMaxWidth().testTag("played_cards"), contentAlignment = Alignment.Center) {
        // Reserve room for six cards even when just one is on the table, avoiding size jumps.
        val cellWidth = minOf(68.dp, ((maxWidth - 25.dp) / 6f).coerceAtLeast(24.dp))
        val cardWidth = minOf(40.dp, cellWidth)
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.Top) {
            played.forEach { play ->
                key(play.playerId, play.card.id) {
                    Column(Modifier.width(cellWidth), horizontalAlignment = Alignment.CenterHorizontally) {
                        KazakhCardView(play.card, true, deck, cardWidth = cardWidth, elevation = 2.dp,
                            modifier = Modifier.testTag("played_card_${play.playerId}")
                                .semantics { contentDescription = "${play.playerName}: ${play.card.rank.ruTitle}, ${play.card.suit.ruName}" })
                        Text(play.playerName, maxLines = 1, overflow = TextOverflow.Ellipsis,
                            fontSize = 10.sp, lineHeight = 12.sp, color = Color.White, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DealingSeats(players: List<Player>, state: LandscapeTableState, modifier: Modifier, onAnchor: (String, Offset) -> Unit) {
    var selected by remember { mutableStateOf<Player?>(null) }
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)) {
        players.forEach { player ->
            key(player.id) {
                val active = player.id == state.currentId
                Column(Modifier.weight(1f, fill = false).widthIn(max = 176.dp).fillMaxWidth()
                    .testTag("seat_${player.id}")
                    .onGloballyPositioned { onAnchor(player.id, it.boundsInRoot().center) }
                    .background(KazakhNavyDark.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                    .border(if (active) 2.dp else 1.dp, if (active) KazakhTurquoise else KazakhGoldDark.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                    .clickable { selected = player }.padding(horizontal = 7.dp, vertical = 5.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(player.avatarEmoji, fontSize = 14.sp, lineHeight = 17.sp)
                        Text(player.name, color = Color.White, fontSize = 13.sp, lineHeight = 16.sp,
                            fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Text(formatTenge(player.tengeBalance), color = KazakhGoldLight, fontSize = 12.sp, lineHeight = 15.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(if (player.hasFolded) "Пас" else if (active) "Ход · ${state.seconds}с"
                        else "${player.cards.size} карт · ${player.tricksWonCount} вз.",
                        color = if (active) KazakhTurquoise else TextMuted,
                        fontSize = 11.sp, lineHeight = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
    selected?.let { player ->
        AlertDialog(onDismissRequest = { selected = null }, title = { Text("${player.avatarEmoji} ${player.name}") },
            text = { Text("Баланс: ${formatTenge(player.tengeBalance)}\nКарт: ${player.cards.size}\nВзяток: ${player.tricksWonCount}") },
            confirmButton = { TextButton(onClick = { selected = null }) { Text("Закрыть") } })
    }
}

@Composable
internal fun TableDealFlight(deal: DealingCardAnimationState, deck: DeckTheme, source: Offset, destination: Offset) {
    val progress = remember(deal) { Animatable(0f) }
    LaunchedEffect(deal) {
        // Finish before the fastest (online, 250 ms) hand update. Never change engine timing for an animation.
        progress.animateTo(1f, tween(220, easing = FastOutSlowInEasing))
    }
    val density = LocalDensity.current
    val width: Dp = 30.dp
    val halfWidth = with(density) { width.toPx() / 2f }
    val halfHeight = halfWidth / 0.68f
    val position = source + (destination - source) * progress.value
    Box(Modifier.offset { IntOffset((position.x - halfWidth).roundToInt(), (position.y - halfHeight).roundToInt()) }
        .graphicsLayer {
            rotationZ = 12f * kotlin.math.sin(progress.value * Math.PI).toFloat()
            alpha = if (progress.value >= 1f) 0f else 1f
        }.testTag("dealing_flight")
        .semantics { contentDescription = "Раздача карты игроку ${deal.targetPlayerName}" }) {
        // All flights are face down. Only the recipient's hand reveals the dealt face on arrival.
        KazakhCardView(null, false, deck, cardWidth = width, elevation = 4.dp)
    }
}
