package com.passmanager.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.passmanager.data.model.Group
import com.passmanager.data.model.PasswordEntry
import com.passmanager.ui.MainViewModel
import com.passmanager.ui.Screen
import com.passmanager.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf<PasswordEntry?>(null) }

    Scaffold(
        containerColor = Background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Background)
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "密码管理器",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        Text(
                            text = "${uiState.passwords.size} 个密码",
                            fontSize = 13.sp,
                            color = OnSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { navController.navigate(Screen.Groups.route) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SurfaceVariant)
                    ) {
                        Icon(
                            Icons.Outlined.FolderOpen,
                            contentDescription = "分组",
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search bar
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("搜索密码…", color = OnSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OnSurfaceVariant) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "清除", tint = OnSurfaceVariant)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Secondary,
                        unfocusedBorderColor = Divider,
                        focusedContainerColor = Background,
                        unfocusedContainerColor = Background
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Group filter chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    item {
                        FilterChipItem(
                            label = "全部",
                            selected = uiState.selectedGroupId == null,
                            onClick = { viewModel.selectGroup(null) }
                        )
                    }
                    item {
                        FilterChipItem(
                            label = "⭐ 收藏",
                            selected = uiState.selectedGroupId == -1L,
                            onClick = { viewModel.selectGroup(-1L) }
                        )
                    }
                    items(uiState.groups) { group ->
                        FilterChipItem(
                            label = group.name,
                            selected = uiState.selectedGroupId == group.id,
                            color = Color(group.color),
                            onClick = { viewModel.selectGroup(group.id) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.EditPassword.createRoute(groupId = uiState.selectedGroupId?.takeIf { it > 0 })) },
                containerColor = Primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加密码")
            }
        }
    ) { paddingValues ->
        if (uiState.passwords.isEmpty()) {
            EmptyState(
                modifier = Modifier.padding(paddingValues),
                hasSearch = uiState.searchQuery.isNotEmpty()
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.passwords, key = { it.id }) { entry ->
                    PasswordCard(
                        entry = entry,
                        group = uiState.groups.find { it.id == entry.groupId },
                        onClick = { navController.navigate(Screen.PasswordDetail.createRoute(entry.id)) },
                        onEdit = { navController.navigate(Screen.EditPassword.createRoute(id = entry.id)) },
                        onToggleFavorite = { viewModel.toggleFavorite(entry) },
                        onDelete = { showDeleteDialog = entry }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    showDeleteDialog?.let { entry ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("删除密码") },
            text = { Text("确定要删除 \"${entry.name}\" 吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deletePassword(entry); showDeleteDialog = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = Error)
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("取消") }
            }
        )
    }
}

@Composable
fun FilterChipItem(
    label: String,
    selected: Boolean,
    color: Color = Secondary,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 13.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.12f),
            selectedLabelColor = color
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            selectedBorderColor = color.copy(alpha = 0.3f),
            borderColor = Divider
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordCard(
    entry: PasswordEntry,
    group: Group?,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Divider)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(group?.let { Color(it.color) }?.copy(alpha = 0.15f) ?: SecondaryLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = entry.name.firstOrNull()?.uppercase() ?: "?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = group?.let { Color(it.color) } ?: Secondary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (entry.account.isNotEmpty() || entry.siteName.isNotEmpty()) {
                    Text(
                        text = entry.account.ifEmpty { entry.siteName },
                        fontSize = 13.sp,
                        color = OnSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (group != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(group.color))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = group.name,
                            fontSize = 11.sp,
                            color = Color(group.color)
                        )
                    }
                }
            }

            Row {
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        if (entry.isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                        contentDescription = "收藏",
                        tint = if (entry.isFavorite) Color(0xFFF59E0B) else OnSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box {
                    IconButton(
                        onClick = { expanded = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "更多",
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("编辑") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = { expanded = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = { Text("删除", color = Error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Error) },
                            onClick = { expanded = false; onDelete() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier, hasSearch: Boolean) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                if (hasSearch) Icons.Outlined.SearchOff else Icons.Outlined.LockOpen,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Divider
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (hasSearch) "没有找到相关密码" else "还没有密码",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = OnSurfaceVariant
            )
            Text(
                text = if (hasSearch) "换个关键词试试" else "点击 + 添加第一个密码",
                fontSize = 13.sp,
                color = Divider
            )
        }
    }
}
