package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LeaderboardEntry
import com.example.model.MatchHistoryItem
import com.example.model.Player
import com.example.model.PlayerRank
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import com.example.ui.theme.KazakhCrimson
import com.example.ui.theme.KazakhGold
import com.example.ui.theme.KazakhGoldLight
import com.example.ui.theme.KazakhNavy
import com.example.ui.theme.KazakhNavyDark
import com.example.ui.theme.KazakhTurquoise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardSheet(
    currentUser: Player,
    leaderboardEntries: List<LeaderboardEntry>,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    matchHistory: List<MatchHistoryItem> = emptyList(),
    onResetStats: (() -> Unit)? = null
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Топ игроков, 1: История матчей (5)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = KazakhNavy,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🏆", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Рейтинг игроков",
                            color = KazakhGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Лидеры лиги казахской Ази",
                            color = Color(0xFFB0BEC5),
                            fontSize = 11.sp
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onResetStats != null) {
                        OutlinedButton(
                            onClick = onResetStats,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = KazakhGold),
                            border = BorderStroke(1.dp, KazakhGold.copy(alpha = 0.6f)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Сброс",
                                tint = KazakhGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Сбросить", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User's own Rank Progress Card
            val currentRank = currentUser.rank
            val allRanks = PlayerRank.values()
            val nextRankIndex = allRanks.indexOf(currentRank) + 1
            val nextRank = if (nextRankIndex < allRanks.size) allRanks[nextRankIndex] else null
            val progress = if (nextRank != null) {
                val span = nextRank.minPoints - currentRank.minPoints
                val earned = currentUser.ratingPoints - currentRank.minPoints
                (earned.toFloat() / span).coerceIn(0f, 1f)
            } else 1.0f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(KazakhNavyDark)
                    .border(1.5.dp, KazakhGold.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = currentRank.iconName, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = currentRank.titleRu,
                                    color = KazakhGoldLight,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "${currentUser.ratingPoints} RP (Очки рейтинга)",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Баланс: ${formatTenge(currentUser.tengeBalance)}",
                                color = Color(0xFF81C784),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Побед: ${currentUser.handsWon} / ${currentUser.totalGamesPlayed}",
                                color = Color(0xFF90A4AE),
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (nextRank != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Следующий ранг: ${nextRank.titleRu}",
                                color = Color(0xFFB0BEC5),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Осталось: ${nextRank.minPoints - currentUser.ratingPoints} RP",
                                color = KazakhGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = KazakhGold,
                            trackColor = Color(0xFF263238)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tabs: Топ игроков | История матчей (5)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(KazakhNavyDark)
                    .border(1.dp, KazakhGold.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == 0) KazakhGold else Color.Transparent)
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🏆 Топ игроков",
                        color = if (selectedTab == 0) KazakhNavyDark else Color(0xFFB0BEC5),
                        fontSize = 12.sp,
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == 1) KazakhGold else Color.Transparent)
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⏱ Последние 5 матчей",
                        color = if (selectedTab == 1) KazakhNavyDark else Color(0xFFB0BEC5),
                        fontSize = 12.sp,
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (selectedTab == 0) {
                // Leaderboard list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(leaderboardEntries, key = { it.rankPosition }) { entry ->
                        LeaderboardRow(entry = entry)
                    }
                }
            } else {
                // Match History Section
                Box(modifier = Modifier.weight(1f)) {
                    MatchHistorySection(matchHistory = matchHistory)
                }
            }
        }
    }
}

@Composable
fun LeaderboardRow(entry: LeaderboardEntry) {
    val medalColor = when (entry.rankPosition) {
        1 -> KazakhGold
        2 -> Color(0xFFE0E0E0)
        3 -> Color(0xFFCD7F32)
        else -> Color(0xFF78909C)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (entry.isCurrentUser) KazakhTurquoise.copy(alpha = 0.25f) else KazakhNavyDark)
            .border(
                1.dp,
                if (entry.isCurrentUser) KazakhTurquoise else Color(0xFF1E293B),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Position Badge
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(medalColor.copy(alpha = 0.2f))
                    .border(1.dp, medalColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${entry.rankPosition}",
                    color = medalColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Avatar & Name
            Text(text = entry.avatarEmoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.name,
                        color = if (entry.isCurrentUser) KazakhGoldLight else Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = entry.rank.titleRu,
                        color = Color(entry.rank.badgeColorHex),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "Побед: ${entry.winRatePercent}% • ${formatTenge(entry.tengeTotalWon)}",
                    color = Color(0xFF90A4AE),
                    fontSize = 11.sp
                )
            }

            // Points
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${entry.ratingPoints}",
                    color = KazakhGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "RP",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

