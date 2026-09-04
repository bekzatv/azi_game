package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Card
import com.example.model.DeckTheme
import com.example.model.OrnamentPattern
import com.example.model.Rank
import com.example.model.Suit
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardIvory
import com.example.ui.theme.KazakhGold
import com.example.ui.theme.KazakhGoldDark
import com.example.ui.theme.KazakhNavyDark

@Composable
fun KazakhCardView(
    card: Card?,
    isFaceUp: Boolean,
    deckTheme: DeckTheme,
    modifier: Modifier = Modifier,
    cardWidth: Dp = 80.dp,
    elevation: Dp = 6.dp,
    isHighlighted: Boolean = false,
    isTrump: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFaceUp) 0f else 180f,
        animationSpec = tween(durationMillis = 350),
        label = "cardFlip"
    )

    val shape = RoundedCornerShape(8.dp)

    Card(
        modifier = modifier
            .width(cardWidth)
            .aspectRatio(0.68f)
            .shadow(
                elevation = if (isHighlighted || isTrump) elevation + 4.dp else elevation,
                shape = shape
            )
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = when {
            isHighlighted -> androidx.compose.foundation.BorderStroke(2.dp, KazakhGold)
            isTrump && isFaceUp -> androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD54F))
            else -> null
        }
    ) {
        if (rotation <= 90f) {
            // Front of Card (Face Up)
            if (card != null) {
                CardFront(
                    card = card,
                    isHighlighted = isHighlighted,
                    isTrump = isTrump,
                    cardWidth = cardWidth
                )
            } else {
                CardBack(deckTheme = deckTheme)
            }
        } else {
            // Back of Card (Face Down with Kazakh Ornament)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
            ) {
                CardBack(deckTheme = deckTheme)
            }
        }
    }
}

@Composable
fun CardFront(
    card: Card,
    isHighlighted: Boolean,
    isTrump: Boolean = false,
    cardWidth: Dp = 80.dp
) {
    val isCompact = cardWidth < 55.dp
    val suitColor = if (card.suit.isRed) Color(0xFFC0292B) else Color(0xFF1E293B)
    val shape = RoundedCornerShape(if (isCompact) 6.dp else 8.dp)
    val paddingDp = if (isCompact) 2.5.dp else 4.dp

    val rankFontSize = if (isCompact) 11.5.sp else 15.sp
    val suitFontSize = if (isCompact) 10.5.sp else 14.sp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .background(CardIvory)
            .border(
                if (isCompact) 1.dp else 1.2.dp,
                if (isHighlighted || isTrump) KazakhGold else CardBorder,
                shape
            )
            .padding(paddingDp)
    ) {
        // Subtle watermark Kazakh ornament on background
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCornerOrnaments(KazakhGoldDark.copy(alpha = if (isCompact) 0.15f else 0.25f))
        }

        // Trump star badge if trump (only if not compact to avoid crowding)
        if (isTrump && !isCompact) {
            Text(
                text = "⭐",
                fontSize = 9.sp,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        // Top-left rank & suit
        Column(
            modifier = Modifier.align(Alignment.TopStart),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = card.rank.symbol,
                color = suitColor,
                fontSize = rankFontSize,
                fontWeight = FontWeight.Black,
                lineHeight = rankFontSize
            )
            Text(
                text = card.suit.symbol,
                color = suitColor,
                fontSize = suitFontSize,
                fontWeight = FontWeight.Bold,
                lineHeight = suitFontSize
            )
        }

        // Center content: Omitted on compact cards (e.g. trump card indicator) to prevent overlapping with corner rank/suit!
        if (!isCompact) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize(0.68f),
                contentAlignment = Alignment.Center
            ) {
                when (card.rank) {
                    Rank.ACE -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = card.suit.symbol,
                                color = suitColor,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ТУЗ",
                                color = KazakhGoldDark,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                    Rank.KING -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "👑", fontSize = 18.sp)
                            Text(
                                text = "КОРОЛЬ",
                                color = suitColor,
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Rank.QUEEN -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "👸", fontSize = 18.sp)
                            Text(
                                text = "ДАМА",
                                color = suitColor,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Rank.JACK -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "⚔️", fontSize = 18.sp)
                            Text(
                                text = "ВАЛЕТ",
                                color = suitColor,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = card.suit.symbol,
                            color = suitColor,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // Bottom-right flipped rank & suit
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .graphicsLayer { rotationZ = 180f },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = card.rank.symbol,
                color = suitColor,
                fontSize = rankFontSize,
                fontWeight = FontWeight.Black,
                lineHeight = rankFontSize
            )
            Text(
                text = card.suit.symbol,
                color = suitColor,
                fontSize = suitFontSize,
                fontWeight = FontWeight.Bold,
                lineHeight = suitFontSize
            )
        }
    }
}

@Composable
fun CardBack(
    deckTheme: DeckTheme
) {
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(deckTheme.primaryColor, deckTheme.secondaryColor)
                )
            )
            .border(1.5.dp, deckTheme.ornamentColor.copy(alpha = 0.8f), shape)
            .padding(3.dp)
    ) {
        // Kazakh national ornament drawn on back
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f

            // Inner border with gold dashed or solid line
            drawRoundRect(
                color = deckTheme.ornamentColor.copy(alpha = 0.4f),
                topLeft = Offset(4f, 4f),
                size = Size(w - 8f, h - 8f),
                style = Stroke(width = 1.5f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )

            // Four corners: Qoshqar myiz horns (for classic Kazakh decks)
            val isCustomGraphicDeck = deckTheme.pattern in listOf(
                OrnamentPattern.AZURE_DIAMOND,
                OrnamentPattern.BICYCLE_RIDER_RED,
                OrnamentPattern.VIOLET_GEOMETRIC,
                OrnamentPattern.OBSIDIAN_ACE_SPADES
            )

            if (!isCustomGraphicDeck) {
                drawQoshqarMyizCorner(Offset(8f, 8f), deckTheme.ornamentColor, 1f, 1f)
                drawQoshqarMyizCorner(Offset(w - 8f, 8f), deckTheme.ornamentColor, -1f, 1f)
                drawQoshqarMyizCorner(Offset(8f, h - 8f), deckTheme.ornamentColor, 1f, -1f)
                drawQoshqarMyizCorner(Offset(w - 8f, h - 8f), deckTheme.ornamentColor, -1f, -1f)

                // Center Medallion
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(deckTheme.ornamentColor.copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = w * 0.38f
                    ),
                    radius = w * 0.38f,
                    center = Offset(cx, cy)
                )
            }

            // Distinct ornament based on pattern
            when (deckTheme.pattern) {
                OrnamentPattern.QOSHQAR_MYIZ, OrnamentPattern.QOSH_DONGGELEK -> {
                    drawKazakhCenterOrnament(cx, cy, w * 0.28f, deckTheme.ornamentColor)
                }
                OrnamentPattern.TUMAR -> {
                    drawTumarOrnament(cx, cy, w * 0.32f, deckTheme.ornamentColor)
                }
                OrnamentPattern.SHANYRAQ -> {
                    drawShanyraqOrnament(cx, cy, w * 0.3f, deckTheme.ornamentColor)
                }
                OrnamentPattern.SYRGHA -> {
                    drawSyrghaOrnament(cx, cy, w * 0.28f, deckTheme.ornamentColor)
                }
                OrnamentPattern.AZURE_DIAMOND -> {
                    drawAzureDiamondCardBack(w, h, cx, cy)
                }
                OrnamentPattern.BICYCLE_RIDER_RED -> {
                    drawBicycleRiderRedCardBack(w, h, cx, cy)
                }
                OrnamentPattern.VIOLET_GEOMETRIC -> {
                    drawVioletMandalaCardBack(w, h, cx, cy)
                }
                OrnamentPattern.OBSIDIAN_ACE_SPADES -> {
                    drawObsidianAceCardBack(w, h, cx, cy)
                }
            }
        }
    }
}

// Kazakh Canvas Helper Functions
private fun DrawScope.drawCornerOrnaments(color: Color) {
    val size = 12f
    drawLine(color, Offset(4f, 4f), Offset(4f + size, 4f), strokeWidth = 1f)
    drawLine(color, Offset(4f, 4f), Offset(4f, 4f + size), strokeWidth = 1f)
}

private fun DrawScope.drawQoshqarMyizCorner(center: Offset, color: Color, scaleX: Float, scaleY: Float) {
    val path = Path().apply {
        moveTo(center.x, center.y)
        cubicTo(
            center.x + 8f * scaleX, center.y,
            center.x + 12f * scaleX, center.y + 4f * scaleY,
            center.x + 8f * scaleX, center.y + 8f * scaleY
        )
        cubicTo(
            center.x + 4f * scaleX, center.y + 10f * scaleY,
            center.x, center.y + 6f * scaleY,
            center.x, center.y
        )
        close()
    }
    drawPath(path, color = color.copy(alpha = 0.7f), style = Stroke(width = 1.2f))
}

private fun DrawScope.drawKazakhCenterOrnament(cx: Float, cy: Float, radius: Float, color: Color) {
    // Four-directional Qoshqar Myiz cross ornament
    val path = Path()
    val r = radius * 0.85f

    for (angle in 0 until 4) {
        val rad = Math.toRadians(angle * 90.0)
        val cos = Math.cos(rad).toFloat()
        val sin = Math.sin(rad).toFloat()

        val ox = cx + cos * r
        val oy = cy + sin * r

        drawCircle(
            color = color.copy(alpha = 0.85f),
            radius = r * 0.25f,
            center = Offset(ox, oy),
            style = Stroke(width = 1.6f)
        )
    }

    // Center jewel
    drawCircle(
        color = color,
        radius = r * 0.28f,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = KazakhNavyDark,
        radius = r * 0.14f,
        center = Offset(cx, cy)
    )
}

private fun DrawScope.drawTumarOrnament(cx: Float, cy: Float, radius: Float, color: Color) {
    // Sacred Kazakh triangular tumar amulet
    val path = Path().apply {
        moveTo(cx, cy - radius)
        lineTo(cx + radius * 0.86f, cy + radius * 0.5f)
        lineTo(cx - radius * 0.86f, cy + radius * 0.5f)
        close()
    }
    drawPath(path, color = color, style = Stroke(width = 1.8f))

    val inner = Path().apply {
        moveTo(cx, cy + radius * 0.5f)
        lineTo(cx + radius * 0.43f, cy - radius * 0.25f)
        lineTo(cx - radius * 0.43f, cy - radius * 0.25f)
        close()
    }
    drawPath(inner, color = color.copy(alpha = 0.5f), style = Stroke(width = 1.2f))
}

private fun DrawScope.drawShanyraqOrnament(cx: Float, cy: Float, radius: Float, color: Color) {
    // Kazakh Shanyraq (the crown of the yurta with crossing kuldesire)
    drawCircle(color = color, radius = radius, center = Offset(cx, cy), style = Stroke(width = 2f))
    drawCircle(color = color.copy(alpha = 0.6f), radius = radius * 0.5f, center = Offset(cx, cy), style = Stroke(width = 1.4f))

    // Cross bars (Күлдіреуіш)
    drawLine(color, Offset(cx - radius, cy), Offset(cx + radius, cy), strokeWidth = 1.5f)
    drawLine(color, Offset(cx, cy - radius), Offset(cx, cy + radius), strokeWidth = 1.5f)
    drawLine(color, Offset(cx - radius * 0.7f, cy - radius * 0.7f), Offset(cx + radius * 0.7f, cy + radius * 0.7f), strokeWidth = 1.2f)
    drawLine(color, Offset(cx - radius * 0.7f, cy + radius * 0.7f), Offset(cx + radius * 0.7f, cy - radius * 0.7f), strokeWidth = 1.2f)
}

private fun DrawScope.drawSyrghaOrnament(cx: Float, cy: Float, radius: Float, color: Color) {
    // Steppe floral drop ornament
    drawCircle(color = color, radius = radius * 0.4f, center = Offset(cx, cy - radius * 0.3f), style = Stroke(width = 1.6f))
    val path = Path().apply {
        moveTo(cx, cy - radius * 0.3f)
        lineTo(cx + radius * 0.5f, cy + radius * 0.6f)
        lineTo(cx, cy + radius * 0.9f)
        lineTo(cx - radius * 0.5f, cy + radius * 0.6f)
        close()
    }
    drawPath(path, color = color, style = Stroke(width = 1.6f))
}

// 1. Image 1: Azure Diamond & Blue Foliage Back (Лазурный Узор)
private fun DrawScope.drawAzureDiamondCardBack(w: Float, h: Float, cx: Float, cy: Float) {
    // Scalloped outer border frame (azure blue arcs)
    val framePad = 4f
    val arcSteps = 16
    val stepH = (h - framePad * 2) / arcSteps
    for (i in 0 until arcSteps) {
        val y = framePad + i * stepH + stepH / 2
        drawCircle(
            color = Color(0xFF0284C7),
            radius = stepH * 0.45f,
            center = Offset(framePad + 2f, y),
            style = Stroke(width = 1.2f)
        )
        drawCircle(
            color = Color(0xFF0284C7),
            radius = stepH * 0.45f,
            center = Offset(w - framePad - 2f, y),
            style = Stroke(width = 1.2f)
        )
    }

    // Corner ornate flourishes (Cyan & White)
    val cPad = 12f
    val cornerSize = w * 0.22f
    // Top-left flourish
    drawCircle(color = Color(0xFF38BDF8), radius = 3.5f, center = Offset(cPad + 8f, cPad + 8f))
    drawCircle(color = Color(0xFF38BDF8), radius = 3.5f, center = Offset(w - cPad - 8f, cPad + 8f))
    drawCircle(color = Color(0xFF38BDF8), radius = 3.5f, center = Offset(cPad + 8f, h - cPad - 8f))
    drawCircle(color = Color(0xFF38BDF8), radius = 3.5f, center = Offset(w - cPad - 8f, h - cPad - 8f))

    // Inner diamond border
    val dSizeX = w * 0.38f
    val dSizeY = h * 0.22f
    val diamondOuter = Path().apply {
        moveTo(cx, cy - dSizeY)
        lineTo(cx + dSizeX, cy)
        lineTo(cx, cy + dSizeY)
        lineTo(cx - dSizeX, cy)
        close()
    }
    drawPath(diamondOuter, color = Color(0xFF0284C7), style = Stroke(width = 3.2f))
    drawPath(diamondOuter, color = Color.White, style = Stroke(width = 1.2f))

    // Inner lattice grid in center
    val gridR = dSizeX * 0.55f
    drawCircle(
        color = Color(0xFF0284C7),
        radius = gridR,
        center = Offset(cx, cy)
    )
    val lines = 5
    val lStep = (gridR * 1.8f) / (lines + 1)
    for (i in 1..lines) {
        val offset = -gridR * 0.9f + i * lStep
        drawLine(
            color = Color.White,
            start = Offset(cx - gridR * 0.8f, cy + offset),
            end = Offset(cx + gridR * 0.8f, cy + offset),
            strokeWidth = 1f
        )
        drawLine(
            color = Color.White,
            start = Offset(cx + offset, cy - gridR * 0.8f),
            end = Offset(cx + offset, cy + gridR * 0.8f),
            strokeWidth = 1f
        )
    }

    // 4 Symmetrical Foliage curves extending from center
    val foliagePath = Path().apply {
        // Top curve
        moveTo(cx, cy - dSizeY)
        cubicTo(cx - w * 0.25f, cy - dSizeY * 1.3f, cx - w * 0.15f, cy - dSizeY * 1.8f, cx, cy - h * 0.38f)
        cubicTo(cx + w * 0.15f, cy - dSizeY * 1.8f, cx + w * 0.25f, cy - dSizeY * 1.3f, cx, cy - dSizeY)
        // Bottom curve
        moveTo(cx, cy + dSizeY)
        cubicTo(cx - w * 0.25f, cy + dSizeY * 1.3f, cx - w * 0.15f, cy + dSizeY * 1.8f, cx, cy + h * 0.38f)
        cubicTo(cx + w * 0.15f, cy + dSizeY * 1.8f, cx + w * 0.25f, cy + dSizeY * 1.3f, cx, cy + dSizeY)
    }
    drawPath(foliagePath, color = Color.White, style = Stroke(width = 2.2f))
    drawPath(foliagePath, color = Color(0xFF38BDF8), style = Stroke(width = 1f))
}

// 2. Image 2: Classic Red Bicycle Rider Back (Красный Райдер)
private fun DrawScope.drawBicycleRiderRedCardBack(w: Float, h: Float, cx: Float, cy: Float) {
    val white = Color.White
    val red = Color(0xFFD32F2F)

    // Classic white border frame with fine inner line
    drawRect(color = white, topLeft = Offset(4f, 4f), size = Size(w - 8f, h - 8f), style = Stroke(width = 1.6f))
    drawRect(color = white, topLeft = Offset(7f, 7f), size = Size(w - 14f, h - 14f), style = Stroke(width = 0.8f))

    // Two circular medallions (Top & Bottom symmetrical circles with cherub / rider on bicycle)
    val circleR = w * 0.24f
    val topCircleY = cy - h * 0.22f
    val botCircleY = cy + h * 0.22f

    listOf(topCircleY, botCircleY).forEach { circleY ->
        // Outer beaded circle
        drawCircle(color = white, radius = circleR, center = Offset(cx, circleY), style = Stroke(width = 1.8f))
        drawCircle(color = white, radius = circleR - 3f, center = Offset(cx, circleY), style = Stroke(width = 0.8f))

        // Cherub Rider Wings
        val wingY = circleY - circleR * 0.25f
        val wingPath = Path().apply {
            moveTo(cx, wingY)
            cubicTo(cx - circleR * 0.55f, wingY - circleR * 0.4f, cx - circleR * 0.65f, wingY + circleR * 0.2f, cx - circleR * 0.2f, wingY + circleR * 0.15f)
            moveTo(cx, wingY)
            cubicTo(cx + circleR * 0.55f, wingY - circleR * 0.4f, cx + circleR * 0.65f, wingY + circleR * 0.2f, cx + circleR * 0.2f, wingY + circleR * 0.15f)
        }
        drawPath(wingPath, color = white, style = Stroke(width = 1.4f))

        // Cherub Head & Body
        drawCircle(color = white, radius = 4f, center = Offset(cx, circleY - circleR * 0.28f))
        drawLine(color = white, start = Offset(cx, circleY - circleR * 0.18f), end = Offset(cx, circleY + circleR * 0.2f), strokeWidth = 2f)

        // Bicycle Wheel & Fork
        drawCircle(color = white, radius = circleR * 0.35f, center = Offset(cx, circleY + circleR * 0.35f), style = Stroke(width = 1.3f))
        drawLine(color = white, start = Offset(cx, circleY + circleR * 0.05f), end = Offset(cx, circleY + circleR * 0.35f), strokeWidth = 1.3f)
    }

    // Center Propeller / Figure-8 Hub
    val hubR = 5.5f
    drawCircle(color = white, radius = hubR, center = Offset(cx, cy))
    drawCircle(color = red, radius = 2.5f, center = Offset(cx, cy))

    // Diagonal propeller blades
    val propPath = Path().apply {
        moveTo(cx - 14f, cy - 8f)
        lineTo(cx + 14f, cy + 8f)
        moveTo(cx - 14f, cy + 8f)
        lineTo(cx + 14f, cy - 8f)
    }
    drawPath(propPath, color = white, style = Stroke(width = 2.2f))

    // 4 Corner Cherubs & Acanthus Vines
    val cornerOffset = 18f
    val vineSize = 14f
    listOf(
        Offset(cornerOffset, cornerOffset),
        Offset(w - cornerOffset, cornerOffset),
        Offset(cornerOffset, h - cornerOffset),
        Offset(w - cornerOffset, h - cornerOffset)
    ).forEach { pos ->
        drawCircle(color = white, radius = 3f, center = pos)
        val leaf = Path().apply {
            moveTo(pos.x, pos.y)
            cubicTo(pos.x + vineSize, pos.y, pos.x + vineSize, pos.y + vineSize, pos.x, pos.y + vineSize)
            cubicTo(pos.x - vineSize, pos.y + vineSize, pos.x - vineSize, pos.y, pos.x, pos.y)
        }
        drawPath(leaf, color = white.copy(alpha = 0.8f), style = Stroke(width = 1f))
    }
}

// 3. Image 3: Violet Geometric Mandala Back (Сиреневая Мандала)
private fun DrawScope.drawVioletMandalaCardBack(w: Float, h: Float, cx: Float, cy: Float) {
    val white = Color.White
    val violet = Color(0xFF7C3AED)

    // Checkered / Mosaic Outer Border (Шахматный кант)
    val borderPad = 4f
    val bWidth = w - borderPad * 2
    val bHeight = h - borderPad * 2
    drawRect(color = white, topLeft = Offset(borderPad, borderPad), size = Size(bWidth, bHeight), style = Stroke(width = 1.4f))

    val checkCols = 20
    val checkStep = bWidth / checkCols
    for (i in 0 until checkCols step 2) {
        drawRect(color = white, topLeft = Offset(borderPad + i * checkStep, borderPad), size = Size(checkStep, 3f))
        drawRect(color = white, topLeft = Offset(borderPad + (i + 1) * checkStep, h - borderPad - 3f), size = Size(checkStep, 3f))
    }

    // Inner rectangular boundary
    drawRect(color = white, topLeft = Offset(11f, 11f), size = Size(w - 22f, h - 22f), style = Stroke(width = 1.2f))

    // Central Multi-ring Circle
    val outerCircleR = w * 0.38f
    drawCircle(color = white, radius = outerCircleR, center = Offset(cx, cy), style = Stroke(width = 1.8f))
    drawCircle(color = white, radius = outerCircleR * 0.88f, center = Offset(cx, cy), style = Stroke(width = 0.9f))
    drawCircle(color = white, radius = outerCircleR * 0.58f, center = Offset(cx, cy), style = Stroke(width = 0.9f))

    // 8-Petal Geometric Lotus / Mandala (Лепестки с шевронной штриховкой)
    val petalCount = 8
    val petalLen = outerCircleR * 0.82f
    for (p in 0 until petalCount) {
        val angle = Math.toRadians(p * 45.0)
        val cos = Math.cos(angle).toFloat()
        val sin = Math.sin(angle).toFloat()
        val normCos = -sin
        val normSin = cos

        val tipX = cx + cos * petalLen
        val tipY = cy + sin * petalLen
        val midX = cx + cos * (petalLen * 0.5f)
        val midY = cy + sin * (petalLen * 0.5f)
        val wPetal = 7.5f

        val petalPath = Path().apply {
            moveTo(cx, cy)
            cubicTo(midX + normCos * wPetal, midY + normSin * wPetal, tipX + normCos * 2f, tipY + normSin * 2f, tipX, tipY)
            cubicTo(tipX - normCos * 2f, tipY - normSin * 2f, midX - normCos * wPetal, midY - normSin * wPetal, cx, cy)
            close()
        }
        drawPath(petalPath, color = white, style = Stroke(width = 1.2f))
        // Central vein
        drawLine(color = white, start = Offset(cx, cy), end = Offset(tipX, tipY), strokeWidth = 0.9f)
    }

    // Top & Bottom Arch Semi-circles (Semi-lotus fans)
    val archR = w * 0.22f
    listOf(cy - h * 0.33f, cy + h * 0.33f).forEach { archY ->
        drawCircle(color = white, radius = archR, center = Offset(cx, archY), style = Stroke(width = 1.4f))
        // Radiating 3 leaf fan
        for (a in -30..30 step 30) {
            val rad = Math.toRadians(a.toDouble() + (if (archY < cy) -90 else 90))
            drawLine(
                color = white,
                start = Offset(cx, archY),
                end = Offset(cx + Math.cos(rad).toFloat() * archR, archY + Math.sin(rad).toFloat() * archR),
                strokeWidth = 1f
            )
        }
    }
}

// 4. Image 4: Obsidian Royal Ace of Spades Back (Пиковый Туз VIP)
private fun DrawScope.drawObsidianAceCardBack(w: Float, h: Float, cx: Float, cy: Float) {
    val silver = Color(0xFFE2E8F0)
    val darkSilver = Color(0xFF94A3B8)
    val black = Color(0xFF0A0A0A)

    // Intricate interlocking gothic border frame
    val bPad = 6f
    drawRect(color = silver, topLeft = Offset(bPad, bPad), size = Size(w - bPad * 2, h - bPad * 2), style = Stroke(width = 1.8f))
    drawRect(color = darkSilver, topLeft = Offset(bPad + 3f, bPad + 3f), size = Size(w - (bPad + 3f) * 2, h - (bPad + 3f) * 2), style = Stroke(width = 0.8f))

    // Celtic knot corners & top/bottom badges
    val badgeW = w * 0.22f
    val knotPath = Path().apply {
        // Top knot
        moveTo(cx - badgeW * 0.5f, bPad + 2f)
        lineTo(cx, bPad + 12f)
        lineTo(cx + badgeW * 0.5f, bPad + 2f)
        lineTo(cx, bPad)
        close()
        // Bottom knot
        moveTo(cx - badgeW * 0.5f, h - bPad - 2f)
        lineTo(cx, h - bPad - 12f)
        lineTo(cx + badgeW * 0.5f, h - bPad - 2f)
        lineTo(cx, h - bPad)
        close()
    }
    drawPath(knotPath, color = silver, style = Stroke(width = 1.4f))

    // Central Starburst & Oval Medallion
    val ovalRX = w * 0.36f
    val ovalRY = h * 0.28f

    // Radiating sunburst rays behind Ace
    val rays = 36
    for (i in 0 until rays) {
        val angle = Math.toRadians(i * (360.0 / rays))
        val cos = Math.cos(angle).toFloat()
        val sin = Math.sin(angle).toFloat()
        drawLine(
            color = darkSilver.copy(alpha = 0.35f),
            start = Offset(cx, cy),
            end = Offset(cx + cos * ovalRX * 0.95f, cy + sin * ovalRY * 0.95f),
            strokeWidth = 0.9f
        )
    }

    // Oval Ring Frame with "A A A A" or Beaded Border
    drawOval(
        color = silver,
        topLeft = Offset(cx - ovalRX, cy - ovalRY),
        size = Size(ovalRX * 2, ovalRY * 2),
        style = Stroke(width = 2.4f)
    )
    drawOval(
        color = darkSilver,
        topLeft = Offset(cx - ovalRX * 0.92f, cy - ovalRY * 0.92f),
        size = Size(ovalRX * 1.84f, ovalRY * 1.84f),
        style = Stroke(width = 1f)
    )

    // Royal Crown on Top of Oval
    val crownY = cy - ovalRY - 4f
    val crownW = w * 0.18f
    val crownH = 10f
    val crownPath = Path().apply {
        moveTo(cx - crownW * 0.5f, crownY)
        lineTo(cx - crownW * 0.45f, crownY - crownH)
        lineTo(cx - crownW * 0.2f, crownY - crownH * 0.4f)
        lineTo(cx, crownY - crownH * 1.2f)
        lineTo(cx + crownW * 0.2f, crownY - crownH * 0.4f)
        lineTo(cx + crownW * 0.45f, crownY - crownH)
        lineTo(cx + crownW * 0.5f, crownY)
        close()
    }
    drawPath(crownPath, color = silver, style = Stroke(width = 1.4f))

    // Majestic Centered Filigree Ace of Spades (Пиковый туз)
    val spadeW = w * 0.32f
    val spadeH = h * 0.17f
    val spadeCenterY = cy - spadeH * 0.05f

    val spadePath = Path().apply {
        // Point at top
        moveTo(cx, spadeCenterY - spadeH * 0.55f)
        // Right bulb
        cubicTo(
            cx + spadeW * 0.32f, spadeCenterY - spadeH * 0.45f,
            cx + spadeW * 0.65f, spadeCenterY + spadeH * 0.05f,
            cx + spadeW * 0.35f, spadeCenterY + spadeH * 0.35f
        )
        // Inward right indent
        cubicTo(
            cx + spadeW * 0.2f, spadeCenterY + spadeH * 0.45f,
            cx + spadeW * 0.05f, spadeCenterY + spadeH * 0.35f,
            cx, spadeCenterY + spadeH * 0.25f
        )
        // Inward left indent
        cubicTo(
            cx - spadeW * 0.05f, spadeCenterY + spadeH * 0.35f,
            cx - spadeW * 0.2f, spadeCenterY + spadeH * 0.45f,
            cx - spadeW * 0.35f, spadeCenterY + spadeH * 0.35f
        )
        // Left bulb
        cubicTo(
            cx - spadeW * 0.65f, spadeCenterY + spadeH * 0.05f,
            cx - spadeW * 0.32f, spadeCenterY - spadeH * 0.45f,
            cx, spadeCenterY - spadeH * 0.55f
        )
        close()
    }
    drawPath(spadePath, color = silver)
    drawPath(spadePath, color = black, style = Stroke(width = 1.5f))

    // Spade Stem
    val stemPath = Path().apply {
        moveTo(cx - 3f, spadeCenterY + spadeH * 0.2f)
        lineTo(cx + 3f, spadeCenterY + spadeH * 0.2f)
        cubicTo(cx + 4f, spadeCenterY + spadeH * 0.35f, cx + spadeW * 0.2f, spadeCenterY + spadeH * 0.52f, cx + spadeW * 0.25f, spadeCenterY + spadeH * 0.55f)
        lineTo(cx - spadeW * 0.25f, spadeCenterY + spadeH * 0.55f)
        cubicTo(cx - spadeW * 0.2f, spadeCenterY + spadeH * 0.52f, cx - 4f, spadeCenterY + spadeH * 0.35f, cx - 3f, spadeCenterY + spadeH * 0.2f)
        close()
    }
    drawPath(stemPath, color = silver)

    // Inner Victorian filigree on the spade
    val innerFlourish = Path().apply {
        moveTo(cx, spadeCenterY - spadeH * 0.3f)
        cubicTo(cx + 8f, spadeCenterY - 6f, cx + 12f, spadeCenterY + 4f, cx, spadeCenterY + 10f)
        cubicTo(cx - 12f, spadeCenterY + 4f, cx - 8f, spadeCenterY - 6f, cx, spadeCenterY - spadeH * 0.3f)
    }
    drawPath(innerFlourish, color = black, style = Stroke(width = 1.3f))
}
