package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.MatchHistoryItem
import com.example.model.MatchResult
import com.example.model.Player
import com.example.model.PlayerRank
import com.example.ui.theme.KazakhCrimson
import com.example.ui.theme.KazakhGold
import com.example.ui.theme.KazakhGoldDark
import com.example.ui.theme.KazakhGoldLight
import com.example.ui.theme.KazakhNavy
import com.example.ui.theme.KazakhNavyDark
import com.example.ui.theme.KazakhTurquoise
import com.example.ui.theme.KazakhTurquoiseDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modern Kazakh-styled Match History Block displaying up to the last 5 matches
 * with distinct Win / Loss badges, rating adjustments (+/- RP), pot changes, and details.
 */
@Composable
fun MatchHistorySection(
    matchHistory: List<MatchHistoryItem>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .testTag("match_history_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = KazakhNavy),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, KazakhGold.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "История матчей",
                        tint = KazakhGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "История последних 5 матчей",
                        color = KazakhGold,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Win/Loss quick summary pill
                if (matchHistory.isNotEmpty()) {
                    val wins = matchHistory.count { it.result == MatchResult.WIN }
                    val losses = matchHistory.count { it.result == MatchResult.LOSS }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(KazakhNavyDark)
                            .border(0.8.dp, KazakhGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "W: $wins • L: $losses",
                            color = KazakhGoldLight,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (matchHistory.isEmpty()) {
                // Empty state when player hasn't played matches yet
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(KazakhNavyDark.copy(alpha = 0.6f))
                        .border(1.dp, Color(0xFF37474F), RoundedCornerShape(12.dp))
                        .padding(vertical = 16.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🃏", fontSize = 26.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Матчей еще нет",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Сыграйте первую партию за столом, чтобы увидеть результаты здесь!",
                            color = Color(0xFF90A4AE),
                            fontSize = 10.5.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Display up to 5 matches
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    matchHistory.take(5).forEachIndexed { index, match ->
                        MatchHistoryRow(match = match, index = index)
                    }
                }
            }
        }
    }
}

@Composable
fun MatchHistoryRow(
    match: MatchHistoryItem,
    index: Int
) {
    val isWin = match.result == MatchResult.WIN
    val accentColor = if (isWin) Color(0xFF4CAF50) else KazakhCrimson
    val badgeBg = if (isWin) Color(0x2E4CAF50) else Color(0x2ED32F2F)
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val formattedTime = remember(match.timestamp) { timeFormatter.format(Date(match.timestamp)) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(KazakhNavyDark)
            .border(1.dp, if (isWin) accentColor.copy(alpha = 0.5f) else Color(0xFF37474F), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Match result badge + Room/Opponents info
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Result badge (ПОБЕДА / ПОРАЖЕНИЕ)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .border(1.dp, accentColor.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isWin) "ПОБЕДА 🏆" else "ПОРАЖЕНИЕ",
                        color = if (isWin) Color(0xFF81C784) else Color(0xFFEF9A9A),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = match.roomName,
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (match.isAzi) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "🔥 Ази!",
                                color = KazakhGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    Text(
                        text = "${match.opponentCount} соп. • ${match.handDescription} • $formattedTime",
                        color = Color(0xFF90A4AE),
                        fontSize = 9.5.sp
                    )
                }
            }

            // Right: Rating points change & Bank
            Column(horizontalAlignment = Alignment.End) {
                // RP Change (+60 / -25)
                val rpText = if (match.ratingChange > 0) "+${match.ratingChange} RP" else "${match.ratingChange} RP"
                Text(
                    text = rpText,
                    color = if (isWin) KazakhGold else Color(0xFFCFD8DC),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                // Pot change
                val potText = if (isWin) "+${formatTengeCompact(match.potWonOrLost)}" else "-${formatTengeCompact(match.potWonOrLost)}"
                Text(
                    text = potText,
                    color = if (isWin) Color(0xFF81C784) else Color(0xFFE57373),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Full User Profile Dialog that displays:
 * 1. Avatar, Name and Title Rank
 * 2. Balance, Rating Points (RP) & Rank progress bar
 * 3. General Statistics (Total games, win-rate %, Azi hands, tricks won)
 * 4. Recent 5 Match History with detailed outcomes
 * 5. Actions: Edit name, reset statistics
 */
@Composable
fun UserProfileDialog(
    userPlayer: Player,
    matchHistory: List<MatchHistoryItem>,
    onDismiss: () -> Unit,
    onEditName: () -> Unit,
    onResetStats: () -> Unit
) {
    var showConfirmReset by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(KazakhNavyDark)
                .border(1.5.dp, KazakhGold, RoundedCornerShape(22.dp))
                .padding(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "👤", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Профиль игрока",
                            color = KazakhGold,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Profile Hero Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(KazakhNavy, KazakhTurquoiseDark)
                            )
                        )
                        .border(1.dp, KazakhGold.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar Circle
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(KazakhTurquoise)
                                .border(1.5.dp, KazakhGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = userPlayer.avatarEmoji, fontSize = 26.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userPlayer.name,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = onEditName,
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Изменить имя",
                                        tint = KazakhGold,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${userPlayer.rank.iconName} ${userPlayer.rank.titleRu}",
                                    color = KazakhGoldLight,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = " • ${userPlayer.ratingPoints} RP",
                                    color = Color(0xFFFFD54F),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Balance Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(KazakhNavyDark)
                                .border(1.dp, KazakhGold.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${formatTengeCompact(userPlayer.tengeBalance)}₸",
                                color = Color(0xFF81C784),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stats Overview Row (3 counters)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val winRate = if (userPlayer.totalGamesPlayed > 0) {
                        (userPlayer.handsWon * 100) / userPlayer.totalGamesPlayed
                    } else 0

                    MiniStatCard(
                        title = "Матчей",
                        value = "${userPlayer.totalGamesPlayed}",
                        subtitle = "Побед: ${userPlayer.handsWon}",
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatCard(
                        title = "Винрейт",
                        value = "$winRate%",
                        subtitle = "Успех",
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatCard(
                        title = "Ази",
                        value = "${userPlayer.aziCount}",
                        subtitle = "Комбинаций",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // MATCH HISTORY SECTION (5 last games)
                MatchHistorySection(matchHistory = matchHistory)

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Actions: Reset stats & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showConfirmReset) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onResetStats()
                                    showConfirmReset = false
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = KazakhCrimson),
                                border = androidx.compose.foundation.BorderStroke(1.dp, KazakhCrimson),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Да, сбросить", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { showConfirmReset = false },
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Отмена", fontSize = 10.5.sp, color = Color.Gray)
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { showConfirmReset = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB0BEC5)),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFF455A64)),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Сброс",
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Сбросить статистику", fontSize = 10.5.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = KazakhGold),
                        border = androidx.compose.foundation.BorderStroke(1.dp, KazakhGold),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Закрыть", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniStatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(KazakhNavy)
            .border(0.8.dp, KazakhGold.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, color = Color(0xFF90A4AE), fontSize = 9.5.sp)
            Text(text = value, color = KazakhGoldLight, fontSize = 13.5.sp, fontWeight = FontWeight.Black)
            Text(text = subtitle, color = Color(0xFFB0BEC5), fontSize = 8.5.sp)
        }
    }
}

private fun formatTengeCompact(amount: Long): String {
    return when {
        amount >= 1_000_000 -> String.format(Locale.getDefault(), "%.1fM", amount / 1_000_000f)
        amount >= 1_000 -> String.format(Locale.getDefault(), "%dK", amount / 1_000)
        else -> "$amount"
    }
}

