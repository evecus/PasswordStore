package com.passmanager.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.passmanager.data.model.Group
import com.passmanager.ui.GroupViewModel
import com.passmanager.ui.Screen
import com.passmanager.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    navController: NavController,
    viewModel: GroupViewModel = hiltViewModel()
) {
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf<Group?>(null) }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("分组管理", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.EditGroup.createRoute()) },
                containerColor = Primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加分组")
            }
        }
    ) { paddingValues ->
        if (groups.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Divider
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("还没有分组", fontSize = 16.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Medium)
                    Text("点击 + 创建分组", fontSize = 13.sp, color = Divider)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(groups, key = { it.id }) { group ->
                    GroupCard(
                        group = group,
                        viewModel = viewModel,
                        onEdit = { navController.navigate(Screen.EditGroup.createRoute(group.id)) },
                        onDelete = { showDeleteDialog = group }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    showDeleteDialog?.let { group ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("删除分组") },
            text = { Text("删除分组 \"${group.name}\" 后，该分组内的密码将变为无分组状态。") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteGroup(group) {}; showDeleteDialog = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = Error)
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("取消") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupCard(
    group: Group,
    viewModel: GroupViewModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val count by viewModel.getPasswordCountForGroup(group.id).collectAsStateWithLifecycle(0)
    var expanded by remember { mutableStateOf(false) }
    val groupColor = Color(group.color)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Divider)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(groupColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Folder,
                    contentDescription = null,
                    tint = groupColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    group.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )
                Text("$count 个密码", fontSize = 13.sp, color = OnSurfaceVariant)
            }
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "更多", tint = OnSurfaceVariant)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGroupScreen(
    id: Long?,
    navController: NavController,
    viewModel: GroupViewModel = hiltViewModel()
) {
    val currentGroup by viewModel.currentGroup.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(groupColors.first()) }
    var isLoaded by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }

    LaunchedEffect(id) {
        if (id != null) viewModel.loadGroup(id)
        else isLoaded = true
    }

    LaunchedEffect(currentGroup) {
        if (id != null && !isLoaded) {
            currentGroup?.let { g ->
                name = g.name
                selectedColor = Color(g.color)
                isLoaded = true
            }
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text(if (id == null) "添加分组" else "编辑分组", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (name.isBlank()) { nameError = true; return@TextButton }
                            val group = Group(
                                id = id ?: 0L,
                                name = name.trim(),
                                color = selectedColor.value.toLong()
                            )
                            viewModel.saveGroup(group) { navController.popBackStack() }
                        }
                    ) {
                        Text("保存", fontWeight = FontWeight.SemiBold, color = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Preview icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(selectedColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Folder,
                        contentDescription = null,
                        tint = selectedColor,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            // Name field
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("分组名称 *") },
                    placeholder = {
                        Text(
                            "例如：社交媒体、工作",
                            color = OnSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Outlined.Label, contentDescription = null, tint = OnSurfaceVariant)
                    },
                    isError = nameError,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = outlinedFieldColors()
                )
                if (nameError) {
                    Text(
                        "名称不能为空",
                        color = Error,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                    )
                }
            }

            // Color picker
            Text("选择颜色", fontSize = 13.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Medium)

            val colorRows = groupColors.chunked(5)
            colorRows.forEach { rowColors ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    rowColors.forEach { color ->
                        ColorDot(
                            color = color,
                            selected = color == selectedColor,
                            onClick = { selectedColor = color }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ColorDot(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
