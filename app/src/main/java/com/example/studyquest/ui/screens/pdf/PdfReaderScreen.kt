package com.example.studyquest.ui.screens.pdf

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyquest.data.model.PdfBookmarkEntity
import com.example.studyquest.data.model.PdfHighlightEntity
import com.example.studyquest.ui.viewmodel.AppDestination
import com.example.studyquest.ui.viewmodel.StudyQuestViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    viewModel: StudyQuestViewModel,
    modifier: Modifier = Modifier
) {
    val pdfId by viewModel.activePdfId.collectAsState()
    val allPdfs by viewModel.pdfDocuments.collectAsState()
    val currentPdf = remember(pdfId, allPdfs) { allPdfs.firstOrNull { it.id == pdfId } }

    BackHandler {
        viewModel.navigateTo(AppDestination.LIBRARY)
    }

    if (currentPdf == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No document selected.")
        }
        return
    }

    val highlights by viewModel.getHighlightsForPdf(currentPdf.id).collectAsState(initial = emptyList())
    val bookmarks by viewModel.getBookmarksForPdf(currentPdf.id).collectAsState(initial = emptyList())

    var currentPageIndex by remember { mutableIntStateOf((currentPdf.lastReadPage - 1).coerceAtLeast(0)) }
    var totalPages by remember { mutableIntStateOf(currentPdf.pageCount.coerceAtLeast(1)) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoadingPage by remember { mutableStateOf(true) }

    // Zoom and pan
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Highlight creation mode
    var isHighlightMode by remember { mutableStateOf(false) }
    var selectedHighlightColor by remember { mutableStateOf("#FFF176") } // Yellow default
    val highlightPalette = listOf("#FFF176", "#F48FB1", "#A5D6A7", "#90CAF9")

    // Dialogs
    var showAddBookmarkDialog by remember { mutableStateOf(false) }
    var showBookmarksSheet by remember { mutableStateOf(false) }
    var showHighlightsSheet by remember { mutableStateOf(false) }
    var showNewHighlightDialog by remember { mutableStateOf(false) }
    var pendingHighlightCoords by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var tappedHighlight by remember { mutableStateOf<PdfHighlightEntity?>(null) }

    // View dimensions
    var pageSize by remember { mutableStateOf(IntSize.Zero) }

    // Native PdfRenderer loading
    LaunchedEffect(currentPdf.localFilePath, currentPageIndex) {
        isLoadingPage = true
        withContext(Dispatchers.IO) {
            try {
                val file = File(currentPdf.localFilePath)
                if (file.exists()) {
                    val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                    val renderer = PdfRenderer(pfd)
                    totalPages = renderer.pageCount
                    val safeIndex = currentPageIndex.coerceIn(0, totalPages - 1)
                    val page = renderer.openPage(safeIndex)

                    // Render at 2x density for crisp vector text
                    val bmpWidth = page.width * 2
                    val bmpHeight = page.height * 2
                    val bitmap = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    renderer.close()
                    pfd.close()

                    withContext(Dispatchers.Main) {
                        currentBitmap = bitmap
                        isLoadingPage = false
                        viewModel.updatePdfReadingProgress(currentPdf.id, safeIndex + 1, totalPages)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    isLoadingPage = false
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentPdf.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Page ${currentPageIndex + 1} of $totalPages",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppDestination.LIBRARY) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Library")
                    }
                },
                actions = {
                    // Bookmarks list button
                    IconButton(onClick = { showBookmarksSheet = true }) {
                        BadgedBox(badge = {
                            if (bookmarks.isNotEmpty()) {
                                Badge { Text("${bookmarks.size}") }
                            }
                        }) {
                            Icon(Icons.Default.BookmarkBorder, contentDescription = "Bookmarks")
                        }
                    }

                    // Highlights list button
                    IconButton(onClick = { showHighlightsSheet = true }) {
                        BadgedBox(badge = {
                            if (highlights.isNotEmpty()) {
                                Badge { Text("${highlights.size}") }
                            }
                        }) {
                            Icon(Icons.Default.BorderColor, contentDescription = "Highlights")
                        }
                    }

                    // Add Bookmark on current page
                    IconButton(onClick = { showAddBookmarkDialog = true }) {
                        Icon(Icons.Default.AddLocationAlt, contentDescription = "Add Bookmark")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    // Highlight color selection toolbar (when highlight mode is toggled)
                    AnimatedVisibility(visible = isHighlightMode) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tap text to highlight:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                highlightPalette.forEach { hex ->
                                    val isSelected = selectedHighlightColor == hex
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(android.graphics.Color.parseColor(hex)))
                                            .border(
                                                width = if (isSelected) 2.5.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                                shape = CircleShape
                                            )
                                            .clickable { selectedHighlightColor = hex },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Reader Bottom Control Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Page
                        IconButton(
                            onClick = {
                                if (currentPageIndex > 0) {
                                    currentPageIndex--
                                }
                            },
                            enabled = currentPageIndex > 0
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Page")
                        }

                        // Page indicator
                        Text(
                            text = "${currentPageIndex + 1} / $totalPages",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Next Page
                        IconButton(
                            onClick = {
                                if (currentPageIndex < totalPages - 1) {
                                    currentPageIndex++
                                }
                            },
                            enabled = currentPageIndex < totalPages - 1
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Page")
                        }

                        // Zoom Out
                        IconButton(
                            onClick = { scale = (scale - 0.25f).coerceAtLeast(1f) },
                            enabled = scale > 1f
                        ) {
                            Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out")
                        }

                        // Zoom In
                        IconButton(
                            onClick = { scale = (scale + 0.25f).coerceAtMost(3f) },
                            enabled = scale < 3f
                        ) {
                            Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In")
                        }

                        // Toggle Highlighting Mode
                        FilledIconToggleButton(
                            checked = isHighlightMode,
                            onCheckedChange = { isHighlightMode = it },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Highlight Mode")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFEBE8E3)),
            contentAlignment = Alignment.Center
        ) {
            if (isLoadingPage) {
                CircularProgressIndicator()
            } else if (currentBitmap != null) {
                val state = rememberTransformableState { zoomChange, offsetChange, _ ->
                    scale = (scale * zoomChange).coerceIn(1f, 3f)
                    offset += offsetChange
                }

                val currentPageHighlights = remember(highlights, currentPageIndex) {
                    highlights.filter { it.pageNumber == (currentPageIndex + 1) }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { coordinates ->
                            pageSize = coordinates.size
                        }
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .transformable(state = state)
                        .pointerInput(isHighlightMode) {
                            if (isHighlightMode) {
                                detectTapGestures { tapOffset ->
                                    if (pageSize.width > 0 && pageSize.height > 0) {
                                        val normX = (tapOffset.x / pageSize.width).coerceIn(0f, 1f)
                                        val normY = (tapOffset.y / pageSize.height).coerceIn(0f, 1f)
                                        pendingHighlightCoords = Pair(normX, normY)
                                        showNewHighlightDialog = true
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // PDF Page Rendered Image
                    Image(
                        bitmap = currentBitmap!!.asImageBitmap(),
                        contentDescription = "PDF Page ${currentPageIndex + 1}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Fit
                    )

                    // Render Persistent Highlights Overlay for current page
                    if (pageSize.width > 0 && pageSize.height > 0) {
                        currentPageHighlights.forEach { h ->
                            val leftPx = h.normX * pageSize.width
                            val topPx = h.normY * pageSize.height
                            val widthPx = (h.normWidth * pageSize.width).coerceAtLeast(40f)
                            val heightPx = (h.normHeight * pageSize.height).coerceAtLeast(24f)

                            val highlightColor = try {
                                Color(android.graphics.Color.parseColor(h.colorHex))
                            } catch (e: Exception) {
                                Color(0xFFFFEB3B)
                            }

                            Box(
                                modifier = Modifier
                                    .offset(x = (leftPx / density).dp, y = (topPx / density).dp)
                                    .size(width = (widthPx / density).dp, height = (heightPx / density).dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(highlightColor.copy(alpha = 0.45f))
                                    .border(1.dp, highlightColor.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                                    .clickable {
                                        tappedHighlight = h
                                    }
                            )
                        }
                    }
                }
            } else {
                Text("Failed to render page.")
            }
        }
    }

    // New Highlight Note Dialog
    if (showNewHighlightDialog && pendingHighlightCoords != null) {
        var highlightText by remember { mutableStateOf("") }
        var highlightNote by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = {
                showNewHighlightDialog = false
                pendingHighlightCoords = null
            },
            title = { Text("Add Highlight & Study Note", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = highlightText,
                        onValueChange = { highlightText = it },
                        label = { Text("Highlighted Passage / Concept") },
                        placeholder = { Text("e.g. Chargaff's rule A=T, G=C") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = highlightNote,
                        onValueChange = { highlightNote = it },
                        label = { Text("Personal Study Note") },
                        placeholder = { Text("Remember for biochemistry exam!") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val (x, y) = pendingHighlightCoords!!
                        viewModel.addHighlight(
                            pdfId = currentPdf.id,
                            page = currentPageIndex + 1,
                            colorHex = selectedHighlightColor,
                            text = highlightText.ifBlank { "Highlighted passage" },
                            note = highlightNote,
                            x = (x - 0.15f).coerceAtLeast(0.05f),
                            y = (y - 0.02f).coerceAtLeast(0.02f),
                            w = 0.75f,
                            h = 0.045f
                        )
                        showNewHighlightDialog = false
                        pendingHighlightCoords = null
                    }
                ) {
                    Text("Save Highlight")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showNewHighlightDialog = false
                        pendingHighlightCoords = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Tapped Existing Highlight Dialog
    if (tappedHighlight != null) {
        val h = tappedHighlight!!
        AlertDialog(
            onDismissRequest = { tappedHighlight = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(h.colorHex)))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Highlight Note (Page ${h.pageNumber})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (h.selectedText.isNotBlank()) {
                        Text(
                            text = "\"${h.selectedText}\"",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (h.note.isNotBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Note: ${h.note}",
                                fontSize = 13.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { tappedHighlight = null }) {
                    Text("Close")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteHighlight(h)
                        tappedHighlight = null
                    }
                ) {
                    Text("Delete Highlight", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }

    // Add Bookmark Dialog
    if (showAddBookmarkDialog) {
        var bookmarkTitle by remember { mutableStateOf("Page ${currentPageIndex + 1} Reference") }
        var bookmarkNote by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddBookmarkDialog = false },
            title = { Text("Bookmark Page ${currentPageIndex + 1}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = bookmarkTitle,
                        onValueChange = { bookmarkTitle = it },
                        label = { Text("Bookmark Label") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = bookmarkNote,
                        onValueChange = { bookmarkNote = it },
                        label = { Text("Optional Note") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addBookmark(currentPdf.id, currentPageIndex + 1, bookmarkTitle, bookmarkNote)
                        showAddBookmarkDialog = false
                    }
                ) {
                    Text("Save Bookmark")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBookmarkDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Bookmarks Modal Sheet
    if (showBookmarksSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBookmarksSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Text("Saved Bookmarks (${bookmarks.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))

                if (bookmarks.isEmpty()) {
                    Text(
                        "No bookmarks saved yet for this textbook.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(bookmarks) { b ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        currentPageIndex = (b.pageNumber - 1).coerceIn(0, totalPages - 1)
                                        showBookmarksSheet = false
                                    },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(b.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Page ${b.pageNumber}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        if (b.note.isNotBlank()) {
                                            Text(b.note, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    IconButton(onClick = { viewModel.deleteBookmark(b) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Highlights Modal Sheet
    if (showHighlightsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHighlightsSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Text("Document Highlights (${highlights.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))

                if (highlights.isEmpty()) {
                    Text(
                        "No highlights created yet. Tap the pencil icon at the bottom to start highlighting!",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(highlights) { h ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        currentPageIndex = (h.pageNumber - 1).coerceIn(0, totalPages - 1)
                                        showHighlightsSheet = false
                                    },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(android.graphics.Color.parseColor(h.colorHex)))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = h.selectedText.ifBlank { "Highlight" },
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                        Text("Page ${h.pageNumber}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.primary)
                                        if (h.note.isNotBlank()) {
                                            Text("Note: ${h.note}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    IconButton(onClick = { viewModel.deleteHighlight(h) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

private val density: Float
    @Composable
    get() = androidx.compose.ui.platform.LocalDensity.current.density
