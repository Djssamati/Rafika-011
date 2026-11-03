package com.camera.ta3bia

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate

// Small icons drawn in code (no emoji, so they look the same on every phone).

/** Camera: white body, `hole` is the colour of the button behind it. */
@Composable
fun CameraIcon(modifier: Modifier, color: Color, hole: Color) {
    Canvas(modifier) {
        val u = size.minDimension / 24f
        drawRoundRect(color, Offset(7.5f * u, 3.5f * u), Size(9f * u, 5f * u), CornerRadius(1.8f * u))
        drawRoundRect(color, Offset(1.5f * u, 6.5f * u), Size(21f * u, 15f * u), CornerRadius(3.5f * u))
        val c = Offset(12f * u, 14f * u)
        drawCircle(hole, 5.6f * u, c)
        drawCircle(color, 4.0f * u, c)
        drawCircle(hole, 2.2f * u, c)
    }
}

/** Settings gear: 8 teeth and a ring. */
@Composable
fun GearIcon(modifier: Modifier, color: Color) {
    Canvas(modifier) {
        val u = size.minDimension / 24f
        val c = Offset(12f * u, 12f * u)
        for (i in 0 until 8) {
            rotate(i * 45f, c) {
                drawRoundRect(
                    color, Offset(c.x - 1.9f * u, c.y - 11.5f * u),
                    Size(3.8f * u, 5.5f * u), CornerRadius(0.8f * u)
                )
            }
        }
        drawCircle(color, 6.6f * u, c, style = Stroke(width = 3.6f * u))
    }
}

/** Glass phone with a curved arrow (title icon). Width : height = 54 : 44. */
@Composable
fun PhoneArrowIcon(modifier: Modifier) {
    Canvas(modifier) {
        val u = size.height / 44f
        rotate(-12f, Offset(15f * u, 22f * u)) {
            drawRoundRect(Color(0x59FFFFFF), Offset(5f * u, 3f * u), Size(20f * u, 37f * u), CornerRadius(5f * u))
            drawRoundRect(
                Color(0xE6FFFFFF), Offset(5f * u, 3f * u), Size(20f * u, 37f * u),
                CornerRadius(5f * u), style = Stroke(width = 1.8f * u)
            )
            drawRoundRect(Color(0x66BFE3FF), Offset(7.8f * u, 8f * u), Size(14.4f * u, 24f * u), CornerRadius(2f * u))
            drawRoundRect(Color(0xE6FFFFFF), Offset(11f * u, 5f * u), Size(8f * u, 1.4f * u), CornerRadius(0.7f * u))
            drawCircle(Color(0xE6FFFFFF), 1.6f * u, Offset(15f * u, 36f * u))
        }
        val arrow = Path().apply {
            moveTo(24f * u, 30f * u)
            cubicTo(27f * u, 14f * u, 34f * u, 12f * u, 44f * u, 15f * u)
        }
        drawPath(arrow, Color(0xFFCDE9FF), style = Stroke(width = 4f * u, cap = StrokeCap.Round))
        val head = Path().apply {
            moveTo(40f * u, 7f * u)
            lineTo(52f * u, 15f * u)
            lineTo(40f * u, 23f * u)
            close()
        }
        drawPath(head, Color(0xFFCDE9FF))
    }
}
