package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SportGold
import com.example.ui.theme.SportOrange
import com.example.ui.theme.TraditionalTeal

/**
 * Custom Jetpack Compose Drawing of a Futuristic Sports Stadium / Arena (X SPORT Arena).
 * Draws dynamic upward spotlights, curved stadium rings, field lights, and athletic track lines.
 */
@Composable
fun AleppoCitadelDraw(
    modifier: Modifier = Modifier,
    primaryColor: Color = SportOrange,
    secondaryColor: Color = SportGold
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Draw glowing background spotlights (Beams of light shooting up)
        drawPath(
            path = Path().apply {
                moveTo(w * 0.1f, h)
                lineTo(w * 0.35f, 0f)
                lineTo(w * 0.55f, 0f)
                lineTo(w * 0.22f, h)
                close()
            },
            brush = Brush.linearGradient(
                colors = listOf(primaryColor.copy(alpha = 0.18f), Color.Transparent),
                start = Offset(w * 0.16f, h),
                end = Offset(w * 0.45f, 0f)
            )
        )

        drawPath(
            path = Path().apply {
                moveTo(w * 0.9f, h)
                lineTo(w * 0.65f, 0f)
                lineTo(w * 0.45f, 0f)
                lineTo(w * 0.78f, h)
                close()
            },
            brush = Brush.linearGradient(
                colors = listOf(secondaryColor.copy(alpha = 0.12f), Color.Transparent),
                start = Offset(w * 0.84f, h),
                end = Offset(w * 0.55f, 0f)
            )
        )

        // 2. Draw modern curved stadium tiers/stands
        val stadiumArch1 = Path().apply {
            moveTo(0f, h * 0.9f)
            cubicTo(w * 0.25f, h * 0.42f, w * 0.75f, h * 0.42f, w, h * 0.9f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = stadiumArch1,
            brush = Brush.verticalGradient(
                colors = listOf(primaryColor.copy(alpha = 0.25f), Color.Transparent)
            )
        )
        
        // Stadium track outline
        val stadiumOutline = Path().apply {
            moveTo(0f, h * 0.9f)
            cubicTo(w * 0.25f, h * 0.42f, w * 0.75f, h * 0.42f, w, h * 0.9f)
        }
        drawPath(
            path = stadiumOutline,
            color = primaryColor.copy(alpha = 0.5f),
            style = Stroke(width = 2.dp.toPx())
        )

        val stadiumArch2 = Path().apply {
            moveTo(w * 0.12f, h * 0.95f)
            cubicTo(w * 0.32f, h * 0.58f, w * 0.68f, h * 0.58f, w * 0.88f, h * 0.95f)
        }
        drawPath(
            path = stadiumArch2,
            color = secondaryColor.copy(alpha = 0.35f),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // Central athletic field glow
        drawCircle(
            color = primaryColor.copy(alpha = 0.12f),
            radius = w * 0.15f,
            center = Offset(w * 0.5f, h * 0.85f)
        )
        
        // Top scoreboard or banner badge
        drawRect(
            color = secondaryColor.copy(alpha = 0.5f),
            topLeft = Offset(w * 0.44f, h * 0.5f),
            size = Size(w * 0.12f, h * 0.05f),
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}

/**
 * Modern Athletic Star / Soccer Championship badge design.
 * Features speed blades, soccer geometric pentagons, and gold champion rings.
 */
@Composable
fun AleppoGeometricStar(
    modifier: Modifier = Modifier,
    starColor: Color = SportGold.copy(alpha = 0.7f),
    strokeWidth: Float = 3f
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = kotlin.math.min(w, h) * 0.4f

        // Draw 3-layered sport championship rings
        drawCircle(
            color = starColor.copy(alpha = 0.3f),
            radius = r,
            style = Stroke(width = strokeWidth)
        )
        drawCircle(
            color = starColor.copy(alpha = 0.15f),
            radius = r * 0.8f,
            style = Stroke(width = strokeWidth * 0.7f)
        )

        // Draw an 8-pointed athletic star / speed blades
        val path1 = Path()
        val path2 = Path()

        path1.moveTo(cx - r, cy)
        path1.lineTo(cx, cy - r)
        path1.lineTo(cx + r, cy)
        path1.lineTo(cx, cy + r)
        path1.close()

        val r45 = r * 0.7071f
        path2.moveTo(cx - r45, cy - r45)
        path2.lineTo(cx + r45, cy - r45)
        path2.lineTo(cx + r45, cy + r45)
        path2.lineTo(cx - r45, cy + r45)
        path2.close()

        drawPath(path = path1, color = starColor.copy(alpha = 0.75f), style = Stroke(width = strokeWidth))
        drawPath(path = path2, color = starColor.copy(alpha = 0.4f), style = Stroke(width = strokeWidth))

        // Center soccer ball panel geometry
        val rCenter = r * 0.35f
        drawCircle(
            color = starColor,
            radius = rCenter,
            style = Stroke(width = strokeWidth)
        )

        // Pentagon lines inside center
        for (i in 0 until 5) {
            val angle = (i * 2 * kotlin.math.PI / 5).toFloat()
            val startX = cx + (rCenter * 0.4f * kotlin.math.cos(angle)).toFloat()
            val startY = cy + (rCenter * 0.4f * kotlin.math.sin(angle)).toFloat()
            val endX = cx + (rCenter * kotlin.math.cos(angle)).toFloat()
            val endY = cy + (rCenter * kotlin.math.sin(angle)).toFloat()
            drawLine(
                color = starColor.copy(alpha = 0.8f),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = strokeWidth * 0.8f
            )
        }
    }
}

/**
 * Draws a sleek, aerodynamic speed-wave arch representing digital satellite broadcast waves.
 */
@Composable
fun SyrianArchLine(
    modifier: Modifier = Modifier,
    color: Color = TraditionalTeal.copy(alpha = 0.4f)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(0f, h)
            cubicTo(
                w * 0.25f, h * 0.8f,
                w * 0.35f, 0f,
                w * 0.5f, 0f
            )
            cubicTo(
                w * 0.65f, 0f,
                w * 0.75f, h * 0.8f,
                w, h
            )
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
