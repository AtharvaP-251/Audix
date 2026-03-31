package com.audix.app.ui.components

import kotlin.math.roundToInt

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.*
import androidx.compose.ui.graphics.*
import com.audix.app.ui.theme.InactiveGrey
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun EqEngineCard(
    isAutoEqEnabled: Boolean,
    onAutoEqChange: (Boolean) -> Unit,
    eqIntensity: Float,
    onIntensityChange: (Float) -> Unit,
    onIntensityChangeFinished: () -> Unit,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = androidx.compose.ui.platform.LocalView.current
    
    AudixCard(
        modifier = modifier,
        isHighlighted = isAutoEqEnabled
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Clickable Title Area with Premium Interaction
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { 
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                            onExpandedChange(!isExpanded) 
                        }
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                ) {
                    val iconRotation by animateFloatAsState(
                        targetValue = if (isExpanded) 90f else 0f,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "iconRotate"
                    )
                    
                    Box(modifier = Modifier.graphicsLayer { rotationZ = iconRotation }) {
                        AudixEqLogo(
                            color = if (isAutoEqEnabled) MaterialTheme.colorScheme.primary else InactiveGrey,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Audix EQ Engine",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isAutoEqEnabled) MaterialTheme.colorScheme.primary else InactiveGrey
                    )
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .width(1.dp)
                        .height(24.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                )

                // Toggle Area
                AudixSwitch(
                    checked = isAutoEqEnabled,
                    onCheckedChange = { newValue ->
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                        onAutoEqChange(newValue)
                    }
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + 
                        fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + 
                       fadeOut(animationSpec = tween(150))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                ) {
                    // Staggered Entrance Animation for Inner Content
                    val contentAlpha by animateFloatAsState(
                        targetValue = if (isExpanded) 1f else 0f,
                        animationSpec = tween(durationMillis = 400, delayMillis = 100),
                        label = "contentAlpha"
                    )
                    val contentOffsetY by animateFloatAsState(
                        targetValue = if (isExpanded) 0f else 20f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                        label = "contentOffset"
                    )

                    AudixInnerCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                alpha = contentAlpha
                                translationY = contentOffsetY
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "AutoEQ Intensity",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${(eqIntensity * 100).roundToInt()}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            AudixSlider(
                                value = eqIntensity.coerceAtLeast(0.1f),
                                onValueChange = onIntensityChange,
                                onValueChangeFinished = onIntensityChangeFinished,
                                valueRange = 0.1f..1.0f,
                                steps = 8,
                                dotPositions = listOf(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
