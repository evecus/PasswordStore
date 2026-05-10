package com.passmanager.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.passmanager.data.model.PasswordEntry
import com.passmanager.ui.PasswordEditViewModel
import com.passmanager.ui.Screen
import com.passmanager.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordDetailScreen(
    id: Long,
    navController: NavController,
    viewModel: PasswordEditViewModel = hiltViewModel()
) {
    val entry by viewModel.entry.collectAsStateWithLifecycle()
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    var showPassword by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var snackbarMessage by remember { mutableStateOf("") }

    LaunchedEffect(id) { viewModel.loadPassword(id) }

    val currentEntry = entry ?: return

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        snackbarMessage = "已复制 $label"
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text(currentEntry.name, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.EditPassword.createRoute(id = id)) }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除", tint = Error)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header card
            val group = groups.find { it.id == currentEntry.groupId }
            val accentColor = group?.let { Color(it.color) } ?: Secondary
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.08f)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentEntry.name.firstOrNull()?.uppercase() ?: "?",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(currentEntry.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Primary)
                    if (currentEntry.siteName.isNotEmpty()) {
                        Text(currentEntry.siteName, fontSize = 14.sp, color = OnSurfaceVariant)
                    }
                    if (group != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        AssistChip(
                            onClick = {},
                            label = { Text(group.name, fontSize = 12.sp) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(accentColor)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(containerColor = accentColor.copy(alpha = 0.12f)),
                            border = AssistChipDefaults.assistChipBorder(enabled = false)
                        )
                    }
                }
            }

            // Fields
            if (currentEntry.account.isNotEmpty()) {
                DetailField(
                    icon = Icons.Outlined.Person,
                    label = "账号",
                    value = currentEntry.account,
                    onCopy = { copyToClipboard("账号", currentEntry.account) }
                )
            }

            if (currentEntry.encryptedPassword.isNotEmpty()) {
                val decrypted = remember(currentEntry.encryptedPassword) {
                    viewModel.decryptPassword(currentEntry.encryptedPassword)
                }
                DetailField(
                    icon = Icons.Outlined.Lock,
                    label = "密码",
                    value = if (showPassword) decrypted else "••••••••••",
                    onCopy = { copyToClipboard("密码", decrypted) },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }, modifier = Modifier.size(36.dp)) {
                            Icon(
                                if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = if (showPassword) "隐藏" else "显示",
                                tint = OnSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    monospace = showPassword
                )
            }

            if (currentEntry.siteName.isNotEmpty()) {
                DetailField(
                    icon = Icons.Outlined.Language,
                    label = "网站/应用",
                    value = currentEntry.siteName,
                    onCopy = { copyToClipboard("网站", currentEntry.siteName) }
                )
            }

            if (currentEntry.notes.isNotEmpty()) {
                DetailField(
                    icon = Icons.Outlined.Notes,
                    label = "备注",
                    value = currentEntry.notes,
                    onCopy = { copyToClipboard("备注", currentEntry.notes) }
                )
            }

            if (snackbarMessage.isNotEmpty()) {
                LaunchedEffect(snackbarMessage) {
                    kotlinx.coroutines.delay(2000)
                    snackbarMessage = ""
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Primary)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp, 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(snackbarMessage, color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除密码") },
            text = { Text("确定要删除 \"${currentEntry.name}\" 吗？") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deletePassword(currentEntry) { navController.popBackStack() } },
                    colors = ButtonDefaults.textButtonColors(contentColor = Error)
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
            }
        )
    }
}

@Composable
fun DetailField(
    icon: ImageVector,
    label: String,
    value: String,
    onCopy: () -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null,
    monospace: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Divider)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(icon, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(18.dp).padding(top = 2.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 11.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    fontSize = 15.sp,
                    color = Primary,
                    fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default
                )
            }
            trailingIcon?.invoke()
            IconButton(onClick = onCopy, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Outlined.ContentCopy, contentDescription = "复制", tint = OnSurfaceVariant, modifier = Modifier.size(18.dp))
            }
        }
    }
}
