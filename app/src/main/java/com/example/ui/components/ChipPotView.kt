package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KazakhCrimson
import com.example.ui.theme.KazakhEmerald
import com.example.ui.theme.KazakhGold
import com.example.ui.theme.KazakhGoldDark
import com.example.ui.theme.KazakhGoldLight
import com.example.ui.theme.KazakhNavyDark
import com.example.ui.theme.KazakhTurquoise
import java.text.NumberFormat
import java.util.Locale

fun formatTenge(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    return "${formatter.format(amount)} ₸"
}

@Composable
fun PotView(
    totalPotTenge: Long,
    currentCallAmount: Long,
    isSvara: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "potGlow")

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isSvara) {
            Box(
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(KazakhCrimson)
                    .border(1.dp, KazakhGold, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "⚡ СВАРА! ҚОР САҚТАЛДЫ",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Main Pot Badge
        Box(
            modifier = Modifier
                .shadow(10.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1B2A38),
                            KazakhNavyDark
                        )
                    )
                )
                .border(
                    1.5.dp,
                    if (isSvara) KazakhCrimson else KazakhGold,
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Stacked visual chip icon
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(KazakhGold)
                        .border(1.5.dp, KazakhGoldDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "₸",
                        color = KazakhNavyDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "БАНК (ҚОР)",
                        color = KazakhGoldLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = formatTenge(totalPotTenge),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        if (currentCallAmount > 0) {
            Text(
                text = "Ағымдағы бәс: ${formatTenge(currentCallAmount)}",
                color = KazakhGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun TengeChip(
    amount: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    val (bgColor, borderColor) = when {
        amount >= 25000L -> Pair(Color(0xFF2C103D), Color(0xFFCE93D8))
        amount >= 10000L -> Pair(KazakhCrimson, KazakhGold)
        amount >= 5000L -> Pair(KazakhTurquoise, Color(0xFF80DEEA))
        amount >= 2000L -> Pair(Color(0xFFE65100), KazakhGold)
        amount >= 1000L -> Pair(KazakhEmerald, KazakhGold)
        else -> Pair(Color(0xFF37474F), Color(0xFFB0BEC5))
    }

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        label = "chipScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .size(48.dp)
            .shadow(if (isSelected) 8.dp else 3.dp, CircleShape)
            .clip(CircleShape)
            .background(bgColor)
            .border(
                width = if (isSelected) 3.dp else 1.5.dp,
                color = if (isSelected) KazakhGoldLight else borderColor,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val label = when {
                amount >= 1000000L -> "${amount / 1000000}M"
                amount >= 1000L -> "${amount / 1000}k"
                else -> "$amount"
            }
            Text(
                text = label,
                color = Color.White,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "₸",
                color = if (isSelected) KazakhGoldLight else KazakhGold,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 9.5.sp
            )
        }
    }
}
