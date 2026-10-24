package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = QuestDarkPrimary,
    onPrimary = QuestDarkOnPrimary,
    primaryContainer = QuestDarkPrimaryContainer,
    onPrimaryContainer = QuestDarkOnPrimaryContainer,
    secondary = QuestDarkSecondary,
    onSecondary = QuestDarkOnSecondary,
    secondaryContainer = QuestDarkSecondaryContainer,
    onSecondaryContainer = QuestDarkOnSecondaryContainer,
    tertiary = QuestDarkTertiary,
    tertiaryContainer = QuestDarkTertiaryContainer,
    onTertiaryContainer = QuestDarkOnTertiaryContainer,
    background = QuestDarkBackground,
    onBackground = QuestDarkTextPrimary,
    surface = QuestDarkSurface,
    onSurface = QuestDarkTextPrimary,
    surfaceVariant = QuestDarkSurfaceVariant,
    onSurfaceVariant = QuestDarkTextSecondary,
    outline = QuestDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = QuestPurplePrimary,
    onPrimary = QuestOnPurple,
    primaryContainer = QuestPurpleContainer,
    onPrimaryContainer = QuestOnPurpleContainer,
    secondary = QuestSageSecondary,
    onSecondary = QuestOnSage,
    secondaryContainer = QuestSageContainer,
    onSecondaryContainer = QuestOnSageContainer,
    tertiary = QuestPinkTertiary,
    tertiaryContainer = QuestPinkContainer,
    onTertiaryContainer = QuestOnPinkContainer,
    background = QuestBackgroundWarm,
    onBackground = QuestTextPrimary,
    surface = QuestSurfaceWarm,
    onSurface = QuestTextPrimary,
    surfaceVariant = QuestSurfaceVariant,
    onSurfaceVariant = QuestTextSecondary,
    outline = QuestOutline
)

@Composable
fun StudyQuestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
