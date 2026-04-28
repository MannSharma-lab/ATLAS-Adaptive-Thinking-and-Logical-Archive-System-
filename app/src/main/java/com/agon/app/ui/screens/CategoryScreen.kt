package com.agon.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import android.net.Uri
import com.agon.app.ui.components.GradientBackground
import com.agon.app.viewmodel.AtlasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    categoryId: String,
    viewModel: AtlasViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPreview: (String) -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val allScreenshots by viewModel.screenshots.collectAsState()
    
    val category = categories.find { it.id == categoryId }
    val categoryScreenshots = allScreenshots.filter { it.categoryId == categoryId }
    
    var showMenu by remember { mutableStateOf(false) }

    GradientBackground(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(category?.name ?: "Category", fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Rename") },
                                onClick = { showMenu = false /* TODO: Implement rename */ }
                            )
                            if (category?.isSystem == false) {
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    onClick = {
                                        viewModel.deleteCategory(categoryId)
                                        showMenu = false
                                        onNavigateBack()
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                        actionIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        ) { paddingValues ->
            if (categoryScreenshots.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("No screenshots found.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(
                        start = 16.dp, 
                        end = 16.dp, 
                        top = paddingValues.calculateTopPadding() + 16.dp, 
                        bottom = 16.dp
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(categoryScreenshots, key = { it.id }) { screenshot ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    // Encode URI because it contains slashes which breaks navigation routes
                                    val encodedId = Uri.encode(screenshot.id)
                                    onNavigateToPreview(encodedId) 
                                }
                        ) {
                            AsyncImage(
                                model = screenshot.imageUrl,
                                contentDescription = "Screenshot",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.7f) // Vertical rectangle
                                    .clip(RoundedCornerShape(16.dp))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = screenshot.extractedText,
                                maxLines = 2,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
