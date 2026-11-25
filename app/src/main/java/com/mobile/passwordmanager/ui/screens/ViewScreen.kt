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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mobile.passwordmanager.ClipboardHelper
import com.mobile.passwordmanager.CryptoVault
import com.mobile.passwordmanager.Entry
import com.mobile.passwordmanager.ui.components.BrandIcon
import com.mobile.passwordmanager.ui.components.iconColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 只读预览页：“打开”入口。与编辑页区分——这里不修改任何数据。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewScreen(
    entryId: String,
    revision: Int,
    onBack: () -> Unit,
    onEdit: (String) -> Unit
) {
    val context = LocalContext.current
    val entry = remember(revision, entryId) { CryptoVault.entries.firstOrNull { it.id == entryId } }
    var passwordVisible by remember { mutableStateOf(false) }

    fun toast(msg: String) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    fun copy(label: String, value: String) {
        if (value.isBlank()) { toast("内容为空"); return }
        ClipboardHelper.copy(context, label, value)
        toast("已复制$label")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("查看详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (entry != null) {
                        IconButton(onClick = { onEdit(entry.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "编辑")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (entry == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("该密码不存在或已被删除", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // 图标 + 标题
            Row(verticalAlignment = Alignment.CenterVertically) {
                val color = iconColor(entry.iconKey)
                Box(
                    Modifier.size(64.dp).clip(RoundedCornerShape(18.dp))
                        .background(color.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    BrandIcon(iconKey = entry.iconKey, size = 34)
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(entry.title.ifBlank { "未命名" }, style = MaterialTheme.typography.headlineSmall)
                    val time = remember(entry.updatedAt) {
                        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(entry.updatedAt))
                    }
                    Text("更新于 $time", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(24.dp))

            FieldCard(label = "用户名", value = entry.username.ifBlank { "(无)" }, onCopy = { copy("用户名", entry.username) })
            Spacer(Modifier.height(12.dp))
            FieldCard(
                label = "密码",
                value = if (passwordVisible) entry.password else "••••••••••",
                onCopy = { copy("密码", entry.password) },
                trailing = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "隐藏密码" else "显示密码"
                        )
                    }
                }
            )
            Spacer(Modifier.height(12.dp))
            FieldCard(label = "网址", value = entry.url.ifBlank { "(无)" }, onCopy = { copy("网址", entry.url) })
            Spacer(Modifier.height(12.dp))
            FieldCard(label = "备注", value = entry.notes.ifBlank { "(无)" }, onCopy = { copy("备注", entry.notes) })

            Spacer(Modifier.height(28.dp))
            Button(
                onClick = { onEdit(entry.id) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("编辑此密码")
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) { Text("关闭") }
        }
    }
}

@Composable
private fun FieldCard(
    label: String,
    value: String,
    onCopy: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text(
                    value,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.clickable { onCopy() }
                )
            }
            if (trailing != null) trailing()
            IconButton(onClick = onCopy) {
                Icon(Icons.Default.ContentCopy, contentDescription = "复制$label",
                    tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
