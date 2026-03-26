package com.audix.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.audix.app.R
import com.audix.app.audio.SpatialProfileLibrary

@Composable
fun SpatialAudioCard(
    isSpatialEnabled: Boolean,
    onSpatialEnabledChange: (Boolean) -> Unit,
    spatialLevel: Int,
    onSpatialLevelChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Track whether this is the first composition to skip the initial animation
    var isFirstComposition by remember { mutableStateOf(true) }

    val profile = SpatialProfileLibrary.getProfile(
        if (isSpatialEnabled) spatialLevel.coerceIn(1, 5) else 0
    )

    AudixCard(modifier = modifier) {
        // Active glow indicator when ON
        if (isSpatialEnabled) {
            val infiniteTransition = rememberInfiniteTransition(label = "spatial_glow")
            val glowAlpha by infiniteTransition.animateFloat(
                initialValue = 0.04f,
                targetValue = 0.12f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "glow_alpha"
            )
            val glowColor = MaterialTheme.colorScheme.primary
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .drawBehind {
                        drawRoundRect(
                            color = glowColor.copy(alpha = glowAlpha),
                            cornerRadius = CornerRadius(24.dp.toPx())
                        )
                    }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Header row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SpatialIcon(
                        color = if (isSpatialEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Spatial Audio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSpatialEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Best with headphones",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                AudixSwitch(
                    checked = isSpatialEnabled,
                    onCheckedChange = { newValue ->
                        isFirstComposition = false
                        onSpatialEnabledChange(newValue)
                    }
                )
            }

            // Expanded body with discrete slider
            AnimatedVisibility(
                visible = isSpatialEnabled,
                enter = if (isFirstComposition) {
                    fadeIn(animationSpec = spring(stiffness = Spring.StiffnessHigh)) +
                    expandVertically(animationSpec = spring(stiffness = Spring.StiffnessHigh))
                } else {
                    fadeIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + expandVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                },
                exit = fadeOut(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            ) {
                Column(modifier = Modifier.padding(top = 24.dp)) {
                    AudixInnerCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Slider label row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Spatial Level",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${spatialLevel.coerceIn(1, 5)} / 5",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Discrete 5-step slider (1..5, 4 steps between = 3 intermediate)
                            AudixSlider(
                                value = spatialLevel.coerceIn(1, 5).toFloat(),
                                onValueChange = { onSpatialLevelChange(kotlin.math.round(it).toInt()) },
                                valueRange = 1f..5f,
                                steps = 3, // 3 intermediate steps => snaps at 1, 2, 3, 4, 5
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Level name + description
                            val displayProfile = SpatialProfileLibrary.getProfile(
                                spatialLevel.coerceIn(1, 5)
                            )
                            Text(
                                text = displayProfile.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = displayProfile.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom spatial audio icon — concentric arcs resembling a headphone/spatial waveform.
 */
@Composable
private fun SpatialIcon(color: Color, modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val strokeW = 2.dp.toPx()

        // Center dot
        drawCircle(
            color = color,
            radius = 2.dp.toPx(),
            center = Offset(cx, cy)
        )

        // Arc 1 (inner)
        drawArc(
            color = color,
            startAngle = -60f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = Offset(cx - size.width * 0.25f, cy - size.height * 0.25f),
            size = Size(size.width * 0.5f, size.height * 0.5f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )

        // Arc 2 (outer)
        drawArc(
            color = color,
            startAngle = -60f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = Offset(cx - size.width * 0.42f, cy - size.height * 0.42f),
            size = Size(size.width * 0.84f, size.height * 0.84f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )

        // Mirror arcs on left side
        drawArc(
            color = color,
            startAngle = 120f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = Offset(cx - size.width * 0.25f, cy - size.height * 0.25f),
            size = Size(size.width * 0.5f, size.height * 0.5f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )

        drawArc(
            color = color,
            startAngle = 120f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = Offset(cx - size.width * 0.42f, cy - size.height * 0.42f),
            size = Size(size.width * 0.84f, size.height * 0.84f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }
}
