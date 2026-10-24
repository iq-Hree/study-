package com.example.studyquest.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

data class Particle(
    val x: Float,
    val y: Float,
    val size: Float,
    val color: Color,
    val speedX: Float,
    val speedY: Float,
    val rotationSpeed: Float
)

@Composable
fun CelebrationConfetti(
    isActive: Boolean,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    val colors = listOf(
        Color(0xFFFFB2C9),
        Color(0xFFC7B1F7),
        Color(0xFF8CD8A7),
        Color(0xFFFFF176),
        Color(0xFF90CAF9),
        Color(0xFFFFCC80)
    )

    val particles = remember {
        List(65) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * 0.2f, // start near top
                size = Random.nextFloat() * 14f + 8f,
                color = colors[Random.nextInt(colors.size)],
                speedX = (Random.nextFloat() - 0.5f) * 0.6f,
                speedY = Random.nextFloat() * 0.6f + 0.4f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 360f
            )
        }
    }

    val progress = remember { Animatable(0f) }

    LaunchedEffect(isActive) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2600, easing = LinearEasing)
        )
        onFinished()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val currentP = progress.value
        val canvasWidth = size.width
        val canvasHeight = size.height

        particles.forEach { p ->
            val curX = (p.x * canvasWidth + p.speedX * canvasWidth * currentP).mod(canvasWidth)
            val curY = p.y * canvasHeight + p.speedY * canvasHeight * currentP
            val alpha = (1f - currentP * 0.85f).coerceIn(0f, 1f)

            if (curY < canvasHeight) {
                drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = p.size / 2,
                    center = Offset(curX, curY)
                )
            }
        }
    }
}

@Composable
fun FloatingXpGain(
    xpAmount: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(xpAmount) {
        visible = true
        delay(1800)
        visible = false
        onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                slideInVertically(initialOffsetY = { 80 }, animationSpec = spring(dampingRatio = 0.6f)),
        exit = fadeOut(tween(400)) + slideOutVertically(targetOffsetY = { -100 }),
        modifier = modifier
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✨ +$xpAmount XP! ✨",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun AnimatedThemeToggle(
    isDark: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isDark) 360f else 0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow),
        label = "theme_rotation"
    )
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = 0.4f),
        label = "theme_scale"
    )

    Surface(
        onClick = onToggle,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        modifier = modifier
            .size(42.dp)
            .scale(scale)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDescription = if (isDark) "Switch to Light Mode" else "Switch to Dark Mode",
                tint = if (isDark) Color(0xFFFFD54F) else MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(22.dp)
                    .rotate(rotation)
            )
        }
    }
}
