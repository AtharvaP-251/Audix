package com.audix.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun HeroSection(
    title: String,
    artist: String,
    genre: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        // Vinyl & Waveforms Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().height(160.dp)
        ) {
            AnimatedWaveform(isPlaying = isPlaying, modifier = Modifier.weight(1f).height(60.dp))
            VinylRecord(isPlaying = isPlaying, modifier = Modifier.size(160.dp))
            AnimatedWaveform(isPlaying = isPlaying, reverse = true, modifier = Modifier.weight(1f).height(60.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Now Playing",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Text(
            text = "by $artist",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (genre != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Detected: $genre",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun VinylRecord(isPlaying: Boolean, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val currentRotation = if (isPlaying) rotation else 0f

    Canvas(modifier = modifier.rotate(currentRotation)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width / 2

        // Base vinyl
        drawCircle(
            color = Color(0xFF111111),
            radius = radius,
            center = center
        )

        // Grooves
        for (i in 1..4) {
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = radius - (i * 12.dp.toPx()),
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Center label
        drawCircle(
            color = Color(0xFFE53935), // Primary red hint
            radius = radius * 0.3f,
            center = center
        )
        // Center hole
        drawCircle(
            color = Color.Black,
            radius = radius * 0.05f,
            center = center
        )
    }
}

@Composable
fun AnimatedWaveform(isPlaying: Boolean, reverse: Boolean = false, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    // If playing, use these durations to animate
    val animationSpecs = if (reverse) listOf(350, 600, 400, 500, 300) else listOf(300, 500, 400, 600, 350)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until 5) {
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = if (isPlaying) 1f else 0.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = animationSpecs[i], easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight(scale)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
