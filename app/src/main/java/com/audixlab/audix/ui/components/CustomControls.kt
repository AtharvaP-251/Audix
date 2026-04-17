package com.audixlab.audix.ui.components

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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.platform.LocalView
import androidx.compose.foundation.Canvas
import android.view.HapticFeedbackConstants

@Composable
fun AudixSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true,
    isBipolar: Boolean = false,
    dotPositions: List<Float>? = null,
    modifier: Modifier = Modifier
) {

    val view = LocalView.current
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)

    val customTrackModifier = Modifier.drawBehind {
        val trackHeight = 4.dp.toPx()
        val thumbRadius = 10.dp.toPx()
        val trackStart = thumbRadius
        val trackEnd = size.width - thumbRadius
        val trackWidth = trackEnd - trackStart

        val rangeSpan = valueRange.endInclusive - valueRange.start

        // Draw background dots if provided
        dotPositions?.forEach { dotVal ->
            val fraction = (dotVal - valueRange.start) / rangeSpan
            val x = trackStart + (trackWidth * fraction)
            
            // Draw a small dot at the specified position
            drawCircle(
                color = inactiveColor.copy(alpha = 0.6f),
                radius = 2.dp.toPx(),
                center = Offset(x, size.height / 2)
            )
        }

        if (isBipolar) {
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

            // Draw distinct zero marker tick
            val isZero = value == 0f
            drawLine(
                color = if (isZero) activeColor else inactiveColor.copy(alpha = 0.5f),
                start = Offset(zeroX, size.height / 2 - if (isZero) 6.dp.toPx() else 4.dp.toPx()),
                end = Offset(zeroX, size.height / 2 + if (isZero) 6.dp.toPx() else 4.dp.toPx()),
                strokeWidth = if (isZero) 3.dp.toPx() else 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }

    Slider(
        value = value,
        onValueChange = {
            if (it != value) {
                // Trigger haptic feedback for significant changes or just any change for a "tick" feel
                // Specifically for bipolar sliders, tick at 0.
                if (isBipolar && ((value < 0 && it >= 0) || (value > 0 && it <= 0))) {
                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK) 
                } else {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                }
                onValueChange(it)
            }
        },
        onValueChangeFinished = onValueChangeFinished,

        valueRange = valueRange,
        steps = steps,
        enabled = enabled,
        colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.primary,
            activeTrackColor = if (isBipolar) Color.Transparent else MaterialTheme.colorScheme.primary,
            inactiveTrackColor = if (isBipolar) Color.Transparent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
            activeTickColor = Color.Transparent,
            inactiveTickColor = Color.Transparent,
            disabledThumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            disabledActiveTrackColor = if (isBipolar) Color.Transparent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            disabledInactiveTrackColor = if (isBipolar) Color.Transparent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
        ),
        modifier = modifier.then(customTrackModifier)
    )
}

@Composable
fun AudixSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    Switch(
        checked = checked,
        onCheckedChange = {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            onCheckedChange?.invoke(it)
        },
        enabled = enabled,


        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = MaterialTheme.colorScheme.primary,
            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
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

