package io.github.ln.apnsettingshelper.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A small circular avatar for a carrier: up to two initials from [name] on a deterministic
 * brand-tinted background (picked from [AvatarColors] by the name hash, so the same carrier always
 * gets the same colour). Generated locally — no trademark logos — so it stays FOSS / store-safe.
 */
@Composable
fun CarrierAvatar(
    name: String,
    modifier: Modifier = Modifier,
) {
    val color = AvatarColors[((name.hashCode() % AvatarColors.size) + AvatarColors.size) % AvatarColors.size]
    Box(
        modifier =
            modifier
                .size(AVATAR_SIZE_DP.dp)
                .clip(CircleShape)
                .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initialsOf(name),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

/** Up to two initials: the first letters of the first two words, or the first two letters of one. */
private fun initialsOf(name: String): String {
    val words = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        words.isEmpty() -> "?"
        words.size == 1 -> words[0].take(2).uppercase()
        else -> (words[0].take(1) + words[1].take(1)).uppercase()
    }
}

private const val AVATAR_SIZE_DP = 40

// Dark, white-text-legible tones (brand teal first, then a spread for visual variety across carriers).
private val AvatarColors =
    listOf(
        Color(0xFF006A68),
        Color(0xFF4A6361),
        Color(0xFF7A5A2E),
        Color(0xFF2E5A88),
        Color(0xFF5A3E7A),
        Color(0xFF3E6A4A),
        Color(0xFF8A4A3E),
    )
