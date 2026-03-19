package com.audix.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size

@Composable
fun AudixSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true,
    isBipolar: Boolean = false,
    modifier: Modifier = Modifier
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)

    val bipolarModifier = if (isBipolar) {
        Modifier.drawBehind {
            val trackHeight = 4.dp.toPx()
            val thumbRadius = 10.dp.toPx() // Standard M3 thumb is ~20dp width
            val trackStart = thumbRadius
            val trackEnd = size.width - thumbRadius
            val trackWidth = trackEnd - trackStart

            val rangeSpan = valueRange.endInclusive - valueRange.start
            // Zero point position
            val zeroFraction = (0f - valueRange.start) / rangeSpan
            val zeroX = trackStart + (trackWidth * zeroFraction)

            // Current value position
            val valueFraction = (value - valueRange.start) / rangeSpan
            val valueX = trackStart + (trackWidth * valueFraction)

            // Draw full background track
            drawLine(
                color = inactiveColor,
                start = Offset(trackStart, size.height / 2),
                end = Offset(trackEnd, size.height / 2),
                strokeWidth = trackHeight,
                cap = StrokeCap.Round
            )

            // Draw active bipolar track (from 0 to value)
            if (value != 0f) {
                drawLine(
                    color = activeColor,
                    start = Offset(zeroX, size.height / 2),
                    end = Offset(valueX, size.height / 2),
                    strokeWidth = trackHeight,
                    cap = StrokeCap.Round
                )
            }

            // Draw zero marker tick
            drawLine(
                color = inactiveColor,
                start = Offset(zeroX, size.height / 2 - 4.dp.toPx()),
                end = Offset(zeroX, size.height / 2 + 4.dp.toPx()),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    } else Modifier

    Slider(
        value = value,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        valueRange = valueRange,
        steps = steps,
        enabled = enabled,
        colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.primary,
            activeTrackColor = if (isBipolar) Color.Transparent else MaterialTheme.colorScheme.primary,
            inactiveTrackColor = if (isBipolar) Color.Transparent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            activeTickColor = if (isBipolar) Color.Transparent else SliderDefaults.colors().activeTickColor,
            inactiveTickColor = if (isBipolar) Color.Transparent else SliderDefaults.colors().inactiveTickColor,
            disabledThumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            disabledActiveTrackColor = if (isBipolar) Color.Transparent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            disabledInactiveTrackColor = if (isBipolar) Color.Transparent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
        ),
        modifier = modifier.then(bipolarModifier)
    )
}

@Composable
fun AudixSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = MaterialTheme.colorScheme.primary,
            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            uncheckedBorderColor = Color.Transparent
        ),
        modifier = modifier
    )
}

@Composable
fun AudixEqLogo(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val barWidth = size.width / 5f
        val gap = barWidth * 0.4f
        val actualBarWidth = barWidth - gap
        val maxH = size.height
        
        val heights = listOf(0.342f, 0.598f, 0.855f, 0.598f, 0.342f)
        var currentX = gap / 2f
        for (hMultiplier in heights) {
            val h = maxH * hMultiplier
            val topY = (maxH - h) / 2f
            drawRoundRect(
                color = color,
                topLeft = Offset(currentX, topY),
                size = Size(actualBarWidth, h),
                cornerRadius = CornerRadius(actualBarWidth / 2f)
            )
            currentX += barWidth
        }
    }
}

