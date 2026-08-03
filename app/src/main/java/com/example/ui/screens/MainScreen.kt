package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ClipItem
import com.example.ui.viewmodel.ClipViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ClipViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clips by viewModel.clips.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val detectedClipboardText by viewModel.detectedClipboardText.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    var showAddEditSheet by remember { mutableStateOf(false) }
    var clipToEdit by remember { mutableStateOf<ClipItem?>(null) }
    var isSearchVisible by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Periodically check clipboard on launch or resume
    LaunchedEffect(Unit) {
        viewModel.checkSystemClipboard(context)
    }

    // Show snackbar feedback messages
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        if (isSearchVisible) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                placeholder = { Text("Search snippets...") },
                                singleLine = true,
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                        }
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 8.dp)
                                    .testTag("search_text_input")
                            )
                        } else {
                            Text(
                                text = "Copy Inbox",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                isSearchVisible = !isSearchVisible
                                if (!isSearchVisible) viewModel.setSearchQuery("")
                            },
                            modifier = Modifier.testTag("toggle_search_button")
                        ) {
                            Icon(
                                imageVector = if (isSearchVisible) Icons.Default.Clear else Icons.Default.Search,
                                contentDescription = if (isSearchVisible) "Close search" else "Search"
                            )
                        }

                        IconButton(
                            onClick = { viewModel.checkSystemClipboard(context) },
                            modifier = Modifier.testTag("check_clipboard_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = "Check system clipboard"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    clipToEdit = null
                    showAddEditSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_clip_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add snippet"
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Stats Overview Header
            ClipStatsHeader(stats = stats)

            // Detected System Clipboard Quick Banner
            QuickClipboardBanner(
                detectedText = detectedClipboardText,
                onSave = { viewModel.quickSaveDetectedClipboard() },
                onDismiss = { viewModel.dismissDetectedClipboard() }
            )

            // Category Filter Row
            FilterCategoryRow(
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.setSelectedCategory(it) }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Main Snippets List
            if (clips.isEmpty()) {
                EmptyInboxState(
                    isSearching = searchQuery.isNotBlank(),
                    onAddClip = {
                        clipToEdit = null
                        showAddEditSheet = true
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = clips,
                        key = { it.id }
                    ) { clip ->
                        ClipCard(
                            clip = clip,
                            onCopy = { viewModel.copyClipToClipboard(context, clip) },
                            onTogglePin = { viewModel.togglePin(clip) },
                            onEdit = {
                                clipToEdit = clip
                                showAddEditSheet = true
                            },
                            onDelete = { viewModel.deleteClip(clip) }
                        )
                    }
                }
            }
        }

        // Add / Edit Modal Sheet
        if (showAddEditSheet) {
            AddEditClipDialog(
                clipToEdit = clipToEdit,
                onDismiss = { showAddEditSheet = false },
                onSave = { title, content, category, colorTagIndex, id ->
                    viewModel.saveClip(title, content, category, colorTagIndex, id)
                }
            )
        }
    }
}
