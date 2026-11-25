package com.mobile.passwordmanager.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mobile.passwordmanager.ClipboardHelper
import com.mobile.passwordmanager.CryptoVault
import com.mobile.passwordmanager.Entry
import com.mobile.passwordmanager.ui.components.ActionSheet
import com.mobile.passwordmanager.ui.components.BrandIcon
import com.mobile.passwordmanager.ui.components.ConfirmDialog
import com.mobile.passwordmanager.ui.components.EmptyState
import com.mobile.passwordmanager.ui.components.SheetAction
import com.mobile.passwordmanager.ui.components.iconColor

/**
 * 分组详情：展示某个分组内的密码列表。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(
    groupId: String,
    revision: Int,
    onChanged: () -> Unit,
    onBack: () -> Unit,
    onViewEntry: (String) -> Unit,
    onEditEntry: (String) -> Unit
) {
    val context = LocalContext.current
    val group = remember(revision) { CryptoVault.groups.firstOrNull { it.id == groupId } }
    val entries = remember(revision) {
        CryptoVault.entriesInGroup(groupId).sortedBy { it.title.lowercase() }
    }
    var entryToDelete by remember { mutableStateOf<Entry?>(null) }
    var sheetEntry by remember { mutableStateOf<Entry?>(null) }

    fun saveAndRefresh() { CryptoVault.save(context); onChanged() }
    fun copyText(label: String, value: String) {
        if (value.isBlank()) {
            Toast.makeText(context, "${label}为空", Toast.LENGTH_SHORT).show()
            return
        }
        ClipboardHelper.copy(context, label, value)
        Toast.makeText(context, "已复制$label", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            group?.name ?: "分组",
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${entries.size} 条密码",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEditEntry("") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加密码")
            }
        }
    ) { padding ->
        if (entries.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        )
                    },
                    message = "此分组下还没有密码\n点击右下角的 + 添加"
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    val color = iconColor(entry.iconKey)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .combinedClickable(
                                onClick = { onViewEntry(entry.id) },
                                onLongClick = { sheetEntry = entry }
                            ),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(color.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                BrandIcon(iconKey = entry.iconKey, size = 24)
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    entry.title.ifBlank { "未命名" },
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    entry.username.ifBlank { "(无用户名)" },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    entryToDelete?.let { entry ->
        ConfirmDialog(
            title = "删除",
            message = "确定删除「${entry.title.ifBlank { "未命名" }}」吗？此操作不可撤销。",
            confirmText = "删除",
            destructive = true,
            onConfirm = {
                CryptoVault.deleteEntry(entry.id)
                saveAndRefresh()
                entryToDelete = null
            },
            onDismiss = { entryToDelete = null }
        )
    }

    // 长按密码菜单
    sheetEntry?.let { e ->
        ActionSheet(
            title = e.title.ifBlank { "未命名" },
            subtitle = e.username.ifBlank { null },
            onDismiss = { sheetEntry = null },
            actions = listOf(
                SheetAction(Icons.Default.OpenInNew, "打开（预览）") {
                    sheetEntry = null; onViewEntry(e.id)
                },
                SheetAction(Icons.Default.Edit, "编辑") {
                    sheetEntry = null; onEditEntry(e.id)
                },
                SheetAction(Icons.Default.Person, "复制用户名") {
                    sheetEntry = null; copyText("用户名", e.username)
                },
                SheetAction(Icons.Default.ContentCopy, "复制密码") {
                    sheetEntry = null; copyText("密码", e.password)
                },
                SheetAction(Icons.Default.Link, "复制网址") {
                    sheetEntry = null; copyText("网址", e.url)
                },
                SheetAction(Icons.Default.Delete, "删除", destructive = true) {
                    sheetEntry = null; entryToDelete = e
                }
            )
        )
    }
}
