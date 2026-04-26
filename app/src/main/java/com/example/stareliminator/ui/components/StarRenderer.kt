package com.example.stareliminator.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun DrawScope.drawStar(
    center: Offset,
    radius: Float,
    color: Color,
    alpha: Float = 1f,
    strokeWidth: Float = 0f
) {
    val starPath = createStarPath(center, radius)

    if (strokeWidth > 0) {
        drawPath(starPath, color.copy(alpha = alpha), style = Stroke(width = strokeWidth))
    } else {
        // Glow layer
        val glowRadius = radius * 1.35f
        val glowPath = createStarPath(center, glowRadius)
        drawPath(glowPath, color.copy(alpha = 0.15f * alpha))

        // Gradient fill
        val lightColor = lerpColor(color, Color.White, 0.5f)
        val darkColor = lerpColor(color, Color.Black, 0.35f)
        val gradient = Brush.radialGradient(
            colors = listOf(lightColor, color, darkColor),
            center = center,
            radius = radius * 1.1f
        )
        drawPath(starPath, gradient, alpha = alpha)

        // Specular highlight
        val highlightCenter = Offset(center.x - radius * 0.15f, center.y - radius * 0.2f)
        val highlightRadius = radius * 0.3f
        drawCircle(
            color = Color.White.copy(alpha = 0.25f * alpha),
            radius = highlightRadius,
            center = highlightCenter
        )
    }
}

private fun createStarPath(center: Offset, radius: Float): Path {
    val path = Path()
    val innerRadius = radius * 0.38f
    val numPoints = 5
    val startAngle = -PI / 2

    for (i in 0 until numPoints * 2) {
        val angle = startAngle + (i * PI / numPoints)
        val r = if (i % 2 == 0) radius else innerRadius
        val x = center.x + r * cos(angle).toFloat()
        val y = center.y + r * sin(angle).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

private fun lerpColor(start: Color, end: Color, fraction: Float): Color {
    return Color(
        red = start.red + (end.red - start.red) * fraction,
        green = start.green + (end.green - start.green) * fraction,
        blue = start.blue + (end.blue - start.blue) * fraction,
        alpha = 1f
    )
}
