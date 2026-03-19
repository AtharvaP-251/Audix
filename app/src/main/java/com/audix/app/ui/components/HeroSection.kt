package com.audix.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.audix.app.R

@Composable
fun HeroSection(
    title: String,
    artist: String,
    genre: String?,
    isPlaying: Boolean,
    isAutoEqEnabled: Boolean,
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
            VinylRecord(modifier = Modifier.size(160.dp))
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
            text = if (isPlaying && artist.isNotBlank()) "by $artist" else artist,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Always reserve space for the genre pill
        Box(
            modifier = Modifier.height(36.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!isAutoEqEnabled) {
                // AudixEQ is off — show greyed-out pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "AudixEQ Off",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else if (genre != null) {
                // AudixEQ on + genre detected
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Genre: $genre",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            // else: AudixEQ on but no genre yet — space reserved, nothing shown
        }
    }
}

@Composable
fun VinylRecord(modifier: Modifier = Modifier) {
    // Circular clip + scale up 1.25x to zoom into disc, hiding most of the white PNG background
    Box(
        modifier = modifier
            .clip(CircleShape),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.vinyl_logo),
            contentDescription = "Vinyl Record",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize(1.25f)
                .scale(1.25f)
        )
    }
}

@Composable
fun AnimatedWaveform(isPlaying: Boolean, reverse: Boolean = false, modifier: Modifier = Modifier) {
    // If playing, use these durations to animate
    val animationSpecs = if (reverse) listOf(350, 600, 400, 500, 300) else listOf(300, 500, 400, 600, 350)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until 5) {
            val scale = remember { Animatable(0.2f) }
            
            androidx.compose.runtime.LaunchedEffect(isPlaying) {
                if (isPlaying) {
                    while (true) {
                        scale.animateTo(1f, tween(durationMillis = animationSpecs[i], easing = FastOutSlowInEasing))
                        scale.animateTo(0.2f, tween(durationMillis = animationSpecs[i], easing = FastOutSlowInEasing))
                    }
                } else {
                    scale.animateTo(0.2f, tween(300))
                }
            }
            
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight(scale.value)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
