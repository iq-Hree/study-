package com.example.studyquest.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.studyquest.data.model.CompanionType
import kotlinx.coroutines.launch

@Composable
fun CompanionBanner(
    companionType: CompanionType,
    userName: String,
    level: Int,
    streakDays: Int,
    onCompanionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val quotes = remember(companionType, streakDays) {
        when (companionType) {
            CompanionType.CAT -> listOf(
                "Purr-fect focus today! Let's conquer molecular genetics together, $userName!",
                "Books and quiet study time make for the best adventures.",
                "Remember to hydrate and take deep breaths between chapters!",
                "Every page you highlight brings you closer to your exam goals!"
            )
            CompanionType.BUNNY -> listOf(
                "Hop to it! You've got amazing study energy today, $userName!",
                "Taking organized notes is my absolute favorite pastime!",
                "Consistent daily habits create monumental leaps of understanding!",
                "Don't worry if a topic feels tough, we'll break it down together!"
            )
            CompanionType.FROG -> listOf(
                "Leap into knowledge! Calm, deep focus leads to true scientific insight.",
                "Patience and repetition turn complex mechanisms into second nature.",
                "Ribbit! Another study session completed is another milestone conquered.",
                "Steady as the pond lily. You're doing splendidly, $userName."
            )
        }
    }

    var quoteIndex by remember { mutableIntStateOf(0) }
    val currentQuote = quotes[quoteIndex % quotes.size]

    val drawableRes = when (companionType) {
        CompanionType.CAT -> R.drawable.companion_cat
        CompanionType.BUNNY -> R.drawable.companion_bunny
        CompanionType.FROG -> R.drawable.companion_frog
    }

    val companionName = when (companionType) {
        CompanionType.CAT -> "Prof. Whisker"
        CompanionType.BUNNY -> "Pip the Scribe"
        CompanionType.FROG -> "Barnaby the Sage"
    }

    // Gentle breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "companion_breathe")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe_scale"
    )

    // Cute interactive tap bounce animation
    val tapScale = remember { Animatable(1f) }
    val heartAnim = remember { Animatable(0f) }
    var currentPetEmoji by remember { mutableStateOf("💖") }
    val petEmojis = listOf("💖", "✨", "⭐", "🌸", "🐾")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Companion Avatar with Cute Bounce and Floating Reaction
            Box(
                modifier = Modifier.size(84.dp),
                contentAlignment = Alignment.Center
            ) {
                // Floating heart/sparkle emoji when petted
                if (heartAnim.value > 0f) {
                    Text(
                        text = currentPetEmoji,
                        fontSize = (18 + (heartAnim.value * 12)).sp,
                        modifier = Modifier
                            .offset(y = (-36 * heartAnim.value).dp)
                            .alpha(1f - heartAnim.value)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .scale(breatheScale * tapScale.value)
                        .clip(CircleShape)
                        .border(2.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable {
                            quoteIndex++
                            currentPetEmoji = petEmojis.random()
                            coroutineScope.launch {
                                launch {
                                    tapScale.snapTo(0.85f)
                                    tapScale.animateTo(
                                        targetValue = 1f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                }
                                launch {
                                    heartAnim.snapTo(0f)
                                    heartAnim.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
                                }
                            }
                            onCompanionClick()
                        }
                ) {
                    Image(
                        painter = painterResource(id = drawableRes),
                        contentDescription = companionName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Speech Bubble
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = companionName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Lvl $level",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "\"$currentQuote\"",
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable { quoteIndex++ }
                )
            }
        }
    }
}
