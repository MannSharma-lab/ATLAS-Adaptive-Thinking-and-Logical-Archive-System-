package com.agon.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agon.app.data.Category
import com.agon.app.ui.components.GradientBackground
import com.agon.app.viewmodel.AtlasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AtlasViewModel,
    onNavigateBack: () -> Unit
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isSystemDefaultTheme by viewModel.isSystemDefaultTheme.collectAsState()
    val categories by viewModel.categories.collectAsState()
    
    var showClearDataDialog by remember { mutableStateOf(false) }
    var categoryToRename by remember { mutableStateOf<Category?>(null) }
    var renameText by remember { mutableStateOf("") }

    val systemBlue = Color(0xFF007AFF)

    GradientBackground(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Appearance
                SettingsSectionTitle("🎨 Appearance")
                SettingsCard {
                    SettingsRow(
                        title = "System Default",
                        icon = "📱",
                        onClick = { viewModel.setSystemDefaultTheme() },
                        trailing = if (isSystemDefaultTheme) { { Text("✅", fontSize = 16.sp) } } else null
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                    SettingsRow(
                        title = "Light Mode",
                        icon = "☀️",
                        onClick = { viewModel.toggleDarkMode(false) },
                        trailing = if (!isSystemDefaultTheme && !isDarkMode) { { Text("✅", fontSize = 16.sp) } } else null
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                    SettingsRow(
                        title = "Dark Mode",
                        icon = "🌙",
                        onClick = { viewModel.toggleDarkMode(true) },
                        trailing = if (!isSystemDefaultTheme && isDarkMode) { { Text("✅", fontSize = 16.sp) } } else null
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // Smart Folders
                SettingsSectionTitle("📁 Smart Folders")
                SettingsCard {
                    categories.forEachIndexed { index, category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(category.name, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                                Text(
                                    text = if (category.isSystem) "System" else "User Created",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { 
                                    categoryToRename = category
                                    renameText = category.name
                                }) {
                                    Text("✏️", fontSize = 18.sp)
                                }
                                if (!category.isSystem) {
                                    IconButton(onClick = { viewModel.deleteCategory(category.id) }) {
                                        Text("🗑️", fontSize = 18.sp)
                                    }
                                }
                            }
                        }
                        if (index < categories.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Data
                SettingsSectionTitle("🗄️ Data")
                SettingsCard {
                    SettingsRow(
                        title = "Re-scan Screenshots",
                        subtitle = "Scan and categorize all screenshots again",
                        icon = "🔄",
                        onClick = { viewModel.triggerScan(forceRescanAll = true) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                    SettingsRow(
                        title = "Clear Database",
                        subtitle = "Remove all scanned data",
                        icon = "🗑️",
                        titleColor = MaterialTheme.colorScheme.error,
                        onClick = { showClearDataDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Privacy
                SettingsSectionTitle("🛡️ Privacy")
                SettingsCard {
                    SettingsRow(
                        title = "All data stored locally",
                        subtitle = "Your screenshots never leave your device",
                        icon = "🔒",
                        iconColor = systemBlue
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                    SettingsRow(
                        title = "No Analytics",
                        subtitle = "Zero tracking, zero telemetry",
                        icon = "🛡️",
                        iconColor = systemBlue
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // About
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ATLAS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        letterSpacing = 2.sp,
                        color = systemBlue
                    )
                    Text(
                        text = "version 1.0.0",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Your Intelligent Screenshot Vault",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Created & Developed by",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Mann Sharma & Manish Sharma",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = systemBlue
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear Database") },
            text = { Text("Are you sure you want to clear all data? This will remove all screenshots and user-created folders. System folders will remain.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearDatabase()
                        showClearDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    categoryToRename?.let { category ->
        var renameError by remember { mutableStateOf<String?>(null) }
        
        AlertDialog(
            onDismissRequest = { 
                categoryToRename = null 
                renameError = null
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
                                categoryToRename = null
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
                    categoryToRename = null 
                    renameError = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val surfaceColor = if (isDark) Color(0x1AFFFFFF) else Color(0x0D000000)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceColor)
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingsRow(
    title: String,
    subtitle: String? = null,
    icon: String,
    titleColor: Color = MaterialTheme.colorScheme.onBackground,
    iconColor: Color? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(32.dp), contentAlignment = Alignment.CenterStart) {
            if (iconColor != null) {
                // If we had a specific color requirement for emojis, we'd use a tinted Icon here.
                // Since they are text emojis, we just display them. The user requested "system blue color" for some logos.
                // We'll simulate it by just using the emoji for now, or you could use a colored icon.
                // For emojis, color tinting doesn't work well on Text, so we leave it as is.
                Text(text = icon, fontSize = 20.sp)
            } else {
                Text(text = icon, fontSize = 20.sp)
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = titleColor, fontSize = 16.sp)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }
        if (trailing != null) {
            trailing()
        }
    }
}
