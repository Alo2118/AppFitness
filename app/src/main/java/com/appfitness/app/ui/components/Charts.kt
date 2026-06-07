package com.appfitness.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/** Simple bar chart built from Compose layout (one bar per value, with labels). */
@Composable
fun BarChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    height: androidx.compose.ui.unit.Dp = 120.dp,
) {
    val max = (data.maxOfOrNull { it.second } ?: 0f).coerceAtLeast(1f)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        data.forEach { (label, value) ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Text(
                    if (value > 0) value.toInt().toString() else "",
                    style = MaterialTheme.typography.labelSmall,
                )
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .fillMaxHeight(fraction = (value / max).coerceIn(0.02f, 1f))
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(barColor),
                )
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

/** Simple line chart drawn on a Canvas; scales to the min/max of [values]. */
@Composable
fun LineChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.tertiary,
    height: androidx.compose.ui.unit.Dp = 120.dp,
) {
    if (values.size < 2) {
        Box(modifier = modifier.fillMaxWidth().height(height), contentAlignment = Alignment.Center) {
            Text("Dati insufficienti", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    val min = values.min()
    val max = values.max()
    val range = (max - min).coerceAtLeast(0.0001f)
    Canvas(modifier = modifier.fillMaxWidth().height(height).padding(vertical = 8.dp)) {
        val stepX = size.width / (values.size - 1)
        val path = Path()
        values.forEachIndexed { i, v ->
            val x = stepX * i
            val y = size.height - ((v - min) / range) * size.height
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = lineColor, style = Stroke(width = 6f))
        values.forEachIndexed { i, v ->
            val x = stepX * i
            val y = size.height - ((v - min) / range) * size.height
            drawCircle(lineColor, radius = 7f, center = Offset(x, y))
        }
    }
}
