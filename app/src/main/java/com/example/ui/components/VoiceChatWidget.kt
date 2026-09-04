package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KazakhCrimson
import com.example.ui.theme.KazakhEmerald
import com.example.ui.theme.KazakhGold
import com.example.ui.theme.KazakhNavyDark

@Composable
fun VoiceChatControlBar(
    isMicEnabled: Boolean,
    isSpeakerEnabled: Boolean,
    isUserSpeaking: Boolean,
    audioAmplitude: Float,
    onToggleMic: () -> Unit,
    onToggleSpeaker: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(KazakhNavyDark.copy(alpha = 0.85f))
            .border(1.dp, KazakhGold.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Live Microphone Button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            !isMicEnabled -> Color(0xFF455A64)
                            isUserSpeaking -> KazakhEmerald
                            else -> KazakhGold
                        }
                    )
                    .clickable { onToggleMic() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isMicEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                    contentDescription = if (isMicEnabled) "Микрофон включен" else "Микрофон выключен",
                    tint = if (!isMicEnabled) Color.White else KazakhNavyDark,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Real-time audio waveform / visualizer bars
            if (isMicEnabled) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.height(18.dp)
                ) {
                    val bars = 5
                    for (i in 0 until bars) {
                        val factor = (i + 1).toFloat() / bars
                        val heightDp = (4 + (audioAmplitude * 14 * factor)).coerceIn(4f, 18f).dp
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(heightDp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isUserSpeaking) KazakhEmerald else KazakhGold)
                        )
                    }
                }
                Text(
                    text = if (isUserSpeaking) "Вы говорите..." else "В эфире",
                    color = if (isUserSpeaking) Color(0xFF81C784) else Color(0xFFB0BEC5),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "Микрофон выкл.",
                    color = Color(0xFF90A4AE),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(2.dp))

            // Speaker Button
            IconButton(
                onClick = onToggleSpeaker,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isSpeakerEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                    contentDescription = "Звук",
                    tint = if (isSpeakerEnabled) KazakhGold else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SpeakingPulseEffect(
    isSpeaking: Boolean,
    modifier: Modifier = Modifier,
    pulseColor: Color = KazakhEmerald
) {
    if (!isSpeaking) return

    val infiniteTransition = rememberInfiniteTransition(label = "voicePulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .border(2.dp, pulseColor.copy(alpha = alpha), CircleShape)
    )
}
