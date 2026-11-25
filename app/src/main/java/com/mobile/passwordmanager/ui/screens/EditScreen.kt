package com.mobile.passwordmanager.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.mobile.passwordmanager.ClipboardHelper
import com.mobile.passwordmanager.CryptoVault
import com.mobile.passwordmanager.Entry
import com.mobile.passwordmanager.PasswordGenerator
import com.mobile.passwordmanager.R
import com.mobile.passwordmanager.ui.components.BrandIcon
import com.mobile.passwordmanager.ui.components.iconColor
import java.util.UUID

/**
 * 新增 / 编辑一条密码记录。entryId 为 null 表示新建。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    entryId: String?,
    groupId: String?,
    revision: Int,
    onChanged: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val existing = remember(entryId, revision) {
        entryId?.let { id -> CryptoVault.entries.firstOrNull { it.id == id } }
    }

    var title by remember { mutableStateOf(existing?.title.orEmpty()) }
    var username by remember { mutableStateOf(existing?.username.orEmpty()) }
    var password by remember { mutableStateOf(existing?.password.orEmpty()) }
    var url by remember { mutableStateOf(existing?.url.orEmpty()) }
    var notes by remember { mutableStateOf(existing?.notes.orEmpty()) }
    var iconKey by remember { mutableStateOf(existing?.iconKey) }
    var currentGroupId by remember { mutableStateOf(existing?.groupId ?: groupId) }
    var showPassword by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }
    var showGroupPicker by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val groups = remember(revision) { CryptoVault.groups.sortedBy { it.name.lowercase() } }
    val groupName = groups.firstOrNull { it.id == currentGroupId }?.name ?: "主页（未分组）"

    fun toast(msg: String) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    fun save() {
        if (title.isBlank()) {
            titleError = "请填写标题"
            return
        }
        val entry = Entry(
            id = existing?.id ?: UUID.randomUUID().toString(),
            title = title.trim(),
            username = username,
            password = password,
            url = url,
            notes = notes,
            groupId = currentGroupId,
            updatedAt = System.currentTimeMillis(),
            iconKey = iconKey
        )
        CryptoVault.upsert(entry)
        CryptoVault.save(context)
        onChanged()
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (existing != null) "编辑密码" else "新建密码",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(onClick = { save() }) {
                        Text("保存", color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 图标 + 标题预览
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showIconPicker = true }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val color = iconColor(iconKey)
                Box(
                    Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(color.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    BrandIcon(iconKey = iconKey, size = 34)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("选择图标", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "点击更换应用/网站图标",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                title,
                { title = it; titleError = null },
                label = { Text("标题（如网站名）") },
                singleLine = true,
                isError = titleError != null,
                supportingText = titleError?.let { { Text(it) } },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                username,
                { username = it },
                label = { Text("用户名 / 邮箱") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                trailingIcon = {
                    IconButton(onClick = {
                        if (username.isNotEmpty()) {
                            ClipboardHelper.copy(context, "用户名", username)
                            toast("已复制用户名")
                        }
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "复制用户名")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                password,
                { password = it },
                label = { Text("密码") },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                shape = RoundedCornerShape(14.dp),
                trailingIcon = {
                    Row {
                        IconButton(onClick = {
                            password = PasswordGenerator.generate()
                            toast("已生成随机密码")
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "生成随机密码")
                        }
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "显示/隐藏密码"
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))
            FilledTonalButton(
                onClick = { password = PasswordGenerator.generate(); toast("已生成随机密码") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("生成随机密码")
            }
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                url,
                { url = it },
                label = { Text("网址（可选）") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            // 分组选择
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showGroupPicker = true }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("所属分组", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Text(groupName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                notes,
                { notes = it },
                label = { Text("备注（可选）") },
                maxLines = 4,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            if (existing != null) {
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("删除此记录")
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }

    if (showIconPicker) {
        IconPickerSheet(
            currentKey = iconKey,
            onDismiss = { showIconPicker = false },
            onPicked = { iconKey = it; showIconPicker = false }
        )
    }

    if (showGroupPicker) {
        AlertDialog(
            onDismissRequest = { showGroupPicker = false },
            title = { Text("选择分组", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    GroupOption("主页（未分组）", currentGroupId == null) {
                        currentGroupId = null; showGroupPicker = false
                    }
                    groups.forEach { g ->
                        GroupOption(g.name, currentGroupId == g.id) {
                            currentGroupId = g.id; showGroupPicker = false
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGroupPicker = false }) { Text("关闭") }
            }
        )
    }

    if (showDeleteConfirm && existing != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("删除") },
            text = { Text("确定删除这条记录吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(onClick = {
                    CryptoVault.deleteEntry(existing.id)
                    CryptoVault.save(context)
                    onChanged()
                    onBack()
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("取消") } }
        )
    }
}

@Composable
private fun GroupOption(name: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            name,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Text("✓", color = MaterialTheme.colorScheme.primary)
        }
    }
}
