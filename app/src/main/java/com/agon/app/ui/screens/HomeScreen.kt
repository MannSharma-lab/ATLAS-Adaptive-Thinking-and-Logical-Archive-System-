package com.agon.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agon.app.data.Category
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import com.agon.app.ui.theme.*
import kotlin.math.abs

import com.agon.app.ui.components.GlassCard
import com.agon.app.ui.components.GradientBackground
import com.agon.app.viewmodel.AtlasViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: AtlasViewModel,
    onNavigateToCategory: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val screenshots by viewModel.screenshots.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    
    val totalScreenshots = screenshots.size

    var searchQuery by remember { mutableStateOf("") }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var selectedCategoryForAction by remember { mutableStateOf<Category?>(null) }

    val pullRefreshState = rememberPullToRefreshState()
    
    LaunchedEffect(isScanning) {
        if (!isScanning) {
            // No equivalent in new API, state is handled by PullToRefreshBox
        }
    }

    if (isScanning) {
        // viewModel.triggerScan() is already called to start scanning
    }

    GradientBackground(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { 
                        Column {
                            Text("ATLAS", fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                            Text(
                                text = "$totalScreenshots screenshots organized",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        actionIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showCreateFolderDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create Smart Folder")
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Smart Folder")
                    }
                }
            }
        ) { paddingValues ->
            PullToRefreshBox(
                isRefreshing = isScanning,
                onRefresh = { viewModel.triggerScan(forceRescanAll = true) },
                state = pullRefreshState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // Search Bar
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search anything from screenshots...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Categories",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val filteredCategories = categories.filter {
                        if (searchQuery.isBlank()) true else {
                            // Check if category name matches search query
                            if (it.name.contains(searchQuery, ignoreCase = true)) true
                            else {
                                // Check if any screenshot inside this category contains the search query in its extracted text
                                val categoryScreenshots = screenshots.filter { s -> s.categoryId == it.id }
                                categoryScreenshots.any { s -> s.extractedText.contains(searchQuery, ignoreCase = true) }
                            }
                        }
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredCategories, key = { it.id }) { category ->
                            val categoryScreenshotsCount = screenshots.count { it.categoryId == category.id }
                            CategoryCard(
                                category = category,
                                count = categoryScreenshotsCount,
                                onClick = { onNavigateToCategory(category.id) },
                                onLongClick = { selectedCategoryForAction = category }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateFolderDialog) {
        var newFolderName by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text("Create Smart Folder") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { 
                            newFolderName = it 
                            errorMessage = null
                        },
                        label = { Text("Folder Name") },
                        singleLine = true,
                        isError = errorMessage != null
                    )
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            val success = viewModel.createSmartFolder(newFolderName)
                            if (success) {
                                showCreateFolderDialog = false
                            } else {
                                errorMessage = "Folder with this name already exists"
                            }
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    selectedCategoryForAction?.let { category ->
        var showRenameDialog by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { selectedCategoryForAction = null },
            title = { Text("Folder Options") },
            text = { Text("What would you like to do with '${category.name}'?") },
            confirmButton = {
                if (!category.isSystem) {
                    Button(
                        onClick = {
                            viewModel.deleteCategory(category.id)
                            selectedCategoryForAction = null
                        }
                    ) {
                        Text("Delete")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRenameDialog = true
                    }
                ) {
                    Text("Rename")
                }
            }
        )

        if (showRenameDialog) {
            var renameText by remember { mutableStateOf(category.name) }
            var renameError by remember { mutableStateOf<String?>(null) }
            
            AlertDialog(
                onDismissRequest = { 
                    showRenameDialog = false 
                    selectedCategoryForAction = null
                },
                title = { Text("Rename Folder") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = renameText,
                            onValueChange = { 
                                renameText = it 
                                renameError = null
                            },
                            singleLine = true,
                            isError = renameError != null
                        )
                        if (renameError != null) {
                            Text(
                                text = renameError!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (renameText.isNotBlank()) {
                                val success = viewModel.renameCategory(category.id, renameText)
                                if (success) {
                                    showRenameDialog = false
                                    selectedCategoryForAction = null
                                } else {
                                    renameError = "Folder with this name already exists"
                                }
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showRenameDialog = false 
                        selectedCategoryForAction = null
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryCard(
    category: Category,
    count: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val (startColor, endColor) = when (category.name) {
        "Shopping" -> Pair(ShoppingGradientStart, ShoppingGradientEnd)
        "Payments" -> Pair(PaymentsGradientStart, PaymentsGradientEnd)
        "Study" -> Pair(StudyGradientStart, StudyGradientEnd)
        "Social" -> Pair(SocialGradientStart, SocialGradientEnd)
        "Others" -> Pair(OthersGradientStart, OthersGradientEnd)
        else -> {
            // Pick a random consistent gradient based on the category ID
            val index = abs(category.id.hashCode()) % customGradients.size
            customGradients[index]
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(startColor, endColor)
                )
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = category.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = if (category.isSystem) "system" else "custom",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Text(
                text = "$count screenshots",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}
