package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TableEngraving(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val ink = Color(0xFFDEC28A).copy(alpha = 0.08f)
        val r = size.width * 0.36f
        drawCircle(ink, r, center, style = Stroke(1.5f))
        drawCircle(ink, r * 0.87f, center, style = Stroke(1f))
        repeat(32) { i ->
            val angle = i * Math.PI / 16
            val inner = Offset(center.x + cos(angle).toFloat() * r * 0.87f,
                center.y + sin(angle).toFloat() * r * 0.87f)
            val outer = Offset(center.x + cos(angle).toFloat() * r,
                center.y + sin(angle).toFloat() * r)
            drawLine(ink, inner, outer, 1f)
        }
        repeat(22) { i ->
            val y = size.height * i / 21
            drawLine(ink.copy(alpha = 0.025f), Offset(0f, y), Offset(size.width, y + size.width), 1f)
        }
    }
}
