package com.example.ui.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.DeckTheme
import com.example.ui.theme.KazakhGold
import com.example.ui.theme.KazakhGoldLight
import com.example.ui.theme.KazakhNavy
import com.example.ui.theme.KazakhNavyDark
import com.example.ui.theme.KazakhTurquoise

@Composable
fun CustomDeckDialog(
    availableDecks: List<DeckTheme>,
    selectedDeck: DeckTheme,
    userTenge: Long = 0L,
    onSelectDeck: (DeckTheme) -> Unit,
    onBuyDeck: (DeckTheme) -> Unit = {},
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            shape = RoundedCornerShape(20.dp),
            color = KazakhNavy,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, KazakhGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Кастомизация колоды",
                            color = KazakhGold,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Эксклюзивные рубашки и узоры карт",
                            color = Color(0xFFB0BEC5),
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // List of Kazakh deck themes (all free and unlocked)
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .heightIn(max = 420.dp)
                        .fillMaxWidth()
                ) {
                    items(availableDecks, key = { it.id }) { deck ->
                        DeckThemeItem(
                            deck = deck,
                            isSelected = deck.id == selectedDeck.id,
                            onSelect = { onSelectDeck(deck) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KazakhTurquoise),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Готово",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DeckThemeItem(
    deck: DeckTheme,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) KazakhNavyDark else Color(0xFF132238)
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, KazakhGold) else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF263238))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Deck Back Mini Preview
            Box(
                modifier = Modifier
                    .width(42.dp)
                    .height(60.dp)
            ) {
                KazakhCardView(
                    card = null,
                    isFaceUp = false,
                    deckTheme = deck,
                    cardWidth = 42.dp,
                    elevation = 2.dp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = deck.name,
                        color = if (isSelected) KazakhGoldLight else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(KazakhGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Выбрано",
                                tint = KazakhNavyDark,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
                Text(
                    text = deck.description,
                    color = Color(0xFF90A4AE),
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action: Select freely
            Button(
                onClick = onSelect,
                enabled = !isSelected,
                colors = ButtonDefaults.buttonColors(
                    containerColor = KazakhTurquoise,
                    disabledContainerColor = Color(0xFF2E7D32)
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isSelected) "Активна" else "Выбрать",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
