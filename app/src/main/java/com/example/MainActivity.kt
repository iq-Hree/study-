package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyquest.ui.components.CelebrationConfetti
import com.example.studyquest.ui.components.FloatingXpGain
import com.example.studyquest.ui.screens.habits.HabitsScreen
import com.example.studyquest.ui.screens.home.HomeScreen
import com.example.studyquest.ui.screens.library.LibraryScreen
import com.example.studyquest.ui.screens.pdf.PdfReaderScreen
import com.example.studyquest.ui.screens.progress.ProgressScreen
import com.example.studyquest.ui.screens.routine.RoutineScreen
import com.example.studyquest.ui.screens.settings.SettingsScreen
import com.example.studyquest.ui.screens.study.StudyTimerScreen
import com.example.studyquest.ui.screens.syllabus.SyllabusScreen
import com.example.studyquest.ui.viewmodel.AppDestination
import com.example.studyquest.ui.viewmodel.StudyQuestViewModel
import com.example.ui.theme.StudyQuestTheme

class MainActivity : ComponentActivity() {
    private val viewModel: StudyQuestViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> systemDark
            }

            StudyQuestTheme(darkTheme = isDark) {
                StudyQuestApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyQuestApp(viewModel: StudyQuestViewModel) {
    val context = LocalContext.current
    val currentDestination by viewModel.currentDestination.collectAsState()
    val timerState by viewModel.timerState.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val celebrationActive by viewModel.celebrationActive.collectAsState()
    val recentXpGain by viewModel.recentXpGain.collectAsState()

    var showMoreSheet by remember { mutableStateOf(false) }

    LaunchedEffect(toastMessage) {
        if (!toastMessage.isNullOrBlank()) {
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Handle back button on sub-screens
    if (currentDestination != AppDestination.HOME) {
        BackHandler {
            if (currentDestination == AppDestination.PDF_READER) {
                viewModel.navigateTo(AppDestination.LIBRARY)
            } else {
                viewModel.navigateTo(AppDestination.HOME)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            // Hide bottom bar when in immersive PDF reader
            if (currentDestination != AppDestination.PDF_READER) {
                NavigationBar(
                    tonalElevation = 6.dp,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    NavigationBarItem(
                        selected = currentDestination == AppDestination.HOME,
                        onClick = { viewModel.navigateTo(AppDestination.HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home", fontSize = 11.sp) }
                    )

                    NavigationBarItem(
                        selected = currentDestination == AppDestination.SYLLABUS,
                        onClick = { viewModel.navigateTo(AppDestination.SYLLABUS) },
                        icon = { Icon(Icons.Default.AccountTree, contentDescription = "Syllabus") },
                        label = { Text("Syllabus", fontSize = 11.sp) }
                    )

                    NavigationBarItem(
                        selected = currentDestination == AppDestination.STUDY_TIMER,
                        onClick = { viewModel.navigateTo(AppDestination.STUDY_TIMER) },
                        icon = {
                            BadgedBox(badge = {
                                if (timerState.isRunning) {
                                    Badge { Text("LIVE") }
                                }
                            }) {
                                Icon(Icons.Default.Timer, contentDescription = "Study")
                            }
                        },
                        label = { Text("Study", fontSize = 11.sp) }
                    )

                    NavigationBarItem(
                        selected = currentDestination == AppDestination.LIBRARY,
                        onClick = { viewModel.navigateTo(AppDestination.LIBRARY) },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = "Library") },
                        label = { Text("Library", fontSize = 11.sp) }
                    )

                    NavigationBarItem(
                        selected = currentDestination == AppDestination.HABITS,
                        onClick = { viewModel.navigateTo(AppDestination.HABITS) },
                        icon = { Icon(Icons.Default.Checklist, contentDescription = "Habits") },
                        label = { Text("Habits", fontSize = 11.sp) }
                    )

                    NavigationBarItem(
                        selected = currentDestination in listOf(AppDestination.PROGRESS, AppDestination.ROUTINE, AppDestination.SETTINGS),
                        onClick = { showMoreSheet = true },
                        icon = { Icon(Icons.Default.GridView, contentDescription = "More") },
                        label = { Text("More", fontSize = 11.sp) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentDestination, label = "screen_transition") { destination ->
                when (destination) {
                    AppDestination.HOME -> HomeScreen(viewModel = viewModel)
                    AppDestination.SYLLABUS -> SyllabusScreen(viewModel = viewModel)
                    AppDestination.STUDY_TIMER -> StudyTimerScreen(viewModel = viewModel)
                    AppDestination.LIBRARY -> LibraryScreen(viewModel = viewModel)
                    AppDestination.PDF_READER -> PdfReaderScreen(viewModel = viewModel)
                    AppDestination.HABITS -> HabitsScreen(viewModel = viewModel)
                    AppDestination.PROGRESS -> ProgressScreen(viewModel = viewModel)
                    AppDestination.ROUTINE -> RoutineScreen(viewModel = viewModel)
                    AppDestination.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }

            // Floating XP Pop-up Animation
            if (recentXpGain != null) {
                FloatingXpGain(
                    xpAmount = recentXpGain!!,
                    onDismiss = { viewModel.dismissXpGain() },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                )
            }

            // Celebration Confetti Animation
            CelebrationConfetti(
                isActive = celebrationActive,
                onFinished = { viewModel.dismissCelebration() }
            )
        }
    }

    // More destinations modal bottom sheet
    if (showMoreSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "StudyQuest Sections",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                MoreMenuTile(
                    title = "Progress & Analytics",
                    subtitle = "XP milestones, study stats, and scholar badges",
                    icon = Icons.Default.BarChart,
                    onClick = {
                        showMoreSheet = false
                        viewModel.navigateTo(AppDestination.PROGRESS)
                    }
                )

                MoreMenuTile(
                    title = "Weekly Study Routine",
                    subtitle = "Smart schedule planner & balanced study blocks",
                    icon = Icons.Default.CalendarMonth,
                    onClick = {
                        showMoreSheet = false
                        viewModel.navigateTo(AppDestination.ROUTINE)
                    }
                )

                MoreMenuTile(
                    title = "Settings & Companion",
                    subtitle = "Choose companion, display theme & JSON export",
                    icon = Icons.Default.Settings,
                    onClick = {
                        showMoreSheet = false
                        viewModel.navigateTo(AppDestination.SETTINGS)
                    }
                )

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

@Composable
fun MoreMenuTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                Text(subtitle, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}
