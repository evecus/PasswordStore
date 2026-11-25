package com.mobile.passwordmanager.ui.screens

import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mobile.passwordmanager.BiometricAuth
import com.mobile.passwordmanager.ClipboardHelper
import com.mobile.passwordmanager.CryptoVault
import com.mobile.passwordmanager.Entry
import com.mobile.passwordmanager.FingerprintHelper
import com.mobile.passwordmanager.Group
import com.mobile.passwordmanager.R
import com.mobile.passwordmanager.ui.components.ActionSheet
import com.mobile.passwordmanager.ui.components.BrandIcon
import com.mobile.passwordmanager.ui.components.ConfirmDialog
import com.mobile.passwordmanager.ui.components.EmptyState
import com.mobile.passwordmanager.ui.components.SectionTitle
import com.mobile.passwordmanager.ui.components.SheetAction
import com.mobile.passwordmanager.ui.components.iconColor

/**
 * 主页：分组区 + 密码区。所有增删改都直接操作 CryptoVault 后落盘。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    revision: Int,
    onChanged: () -> Unit,
    onOpenGroup: (String) -> Unit,
    onViewEntry: (String) -> Unit,
    onEditEntry: (String) -> Unit,
    onLocked: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as FragmentActivity

    var showAddSheet by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var groupDialog by remember { mutableStateOf<Group?>(null) }
    var showGroupDialog by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<Entry?>(null) }
    var groupToDelete by remember { mutableStateOf<Group?>(null) }
    var sheetGroup by remember { mutableStateOf<Group?>(null) }
    var sheetEntry by remember { mutableStateOf<Entry?>(null) }
    var showChangePwd by remember { mutableStateOf(false) }
    var showFingerprintToggle by remember { mutableStateOf(false) }
    var showFingerprintEnroll by remember { mutableStateOf(false) }

    fun toast(msg: String) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    fun saveAndRefresh() { CryptoVault.save(context); onChanged() }

    // 读取最新数据（revision 变化触发重组）
    val groups = remember(revision) { CryptoVault.groups.sortedBy { it.name.lowercase() } }
    val homeEntries = remember(revision) {
        CryptoVault.entries.filter { it.groupId == null }.sortedBy { it.title.lowercase() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                        listOf(
                                            com.mobile.passwordmanager.ui.theme.GradientStart,
                                            com.mobile.passwordmanager.ui.theme.GradientEnd
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.VpnKey, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                stringResourceSafe(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                "${groups.size} 个分组 · ${CryptoVault.entries.size} 条密码",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResourceSafe(R.string.action_change_password)) },
                            onClick = { showMenu = false; showChangePwd = true }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResourceSafe(R.string.action_fingerprint)) },
                            onClick = { showMenu = false; showFingerprintToggle = true }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResourceSafe(R.string.action_lock)) },
                            onClick = {
                                showMenu = false
                                CryptoVault.lock()
                                onLocked()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResourceSafe(R.string.add_entry))
            }
        }
    ) { padding ->
        if (groups.isEmpty() && homeEntries.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        )
                    },
                    message = stringResourceSafe(R.string.empty_state)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                if (groups.isNotEmpty()) {
                    item { SectionTitle(stringResourceSafe(R.string.section_groups)) }
                    items(groups, key = { "g_${it.id}" }) { group ->
                        GroupCard(
                            group = group,
                            count = CryptoVault.entryCountInGroup(group.id),
                            onClick = { onOpenGroup(group.id) },
                            onLongClick = { sheetGroup = group }
                        )
                    }
                }

                item { SectionTitle(stringResourceSafe(R.string.section_passwords)) }
                items(homeEntries, key = { "e_${it.id}" }) { entry ->
                    EntryCard(
                        entry = entry,
                        onClick = { onViewEntry(entry.id) },
                        onLongClick = { sheetEntry = entry }
                    )
                }
            }
        }
    }

    // 选择创建类型
    if (showAddSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(onDismissRequest = { showAddSheet = false }, sheetState = sheetState) {
            Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 28.dp)) {
                Text(
                    stringResourceSafe(R.string.dialog_choose_add),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                ActionRow(
                    icon = Icons.Default.Folder,
                    title = stringResourceSafe(R.string.add_group),
                    color = Color(0xFF4D8DFF)
                ) {
                    showAddSheet = false
                    groupDialog = null
                    showGroupDialog = true
                }
                ActionRow(
                    icon = Icons.Default.Password,
                    title = stringResourceSafe(R.string.add_password),
                    color = Color(0xFF34C759)
                ) {
                    showAddSheet = false
                    onEditEntry("")
                }
            }
        }
    }

    // 新建/编辑分组
    if (showGroupDialog) {
        GroupDialog(
            existing = groupDialog,
            onDismiss = { showGroupDialog = false },
            onSave = { name, iconKey ->
                val now = System.currentTimeMillis()
                val g = Group(
                    id = groupDialog?.id ?: java.util.UUID.randomUUID().toString(),
                    name = name,
                    createdAt = groupDialog?.createdAt ?: now,
                    updatedAt = now,
                    iconKey = iconKey
                )
                CryptoVault.upsertGroup(g)
                saveAndRefresh()
                showGroupDialog = false
            }
        )
    }

    // 删除确认
    entryToDelete?.let { entry ->
        ConfirmDialog(
            title = stringResourceSafe(R.string.delete),
            message = context.getString(R.string.confirm_delete, entry.title.ifBlank { "未命名" }),
            confirmText = stringResourceSafe(R.string.delete),
            destructive = true,
            onConfirm = {
                CryptoVault.deleteEntry(entry.id)
                saveAndRefresh()
                entryToDelete = null
            },
            onDismiss = { entryToDelete = null }
        )
    }
    groupToDelete?.let { group ->
        ConfirmDialog(
            title = stringResourceSafe(R.string.delete),
            message = context.getString(R.string.confirm_delete_group, group.name),
            confirmText = stringResourceSafe(R.string.delete),
            destructive = true,
            onConfirm = {
                CryptoVault.deleteGroup(group.id)
                saveAndRefresh()
                groupToDelete = null
            },
            onDismiss = { groupToDelete = null }
        )
    }

    if (showChangePwd) {
        ChangePasswordDialog(onDismiss = { showChangePwd = false }, toast = { toast(it) })
    }

    if (showFingerprintToggle) {
        FingerprintToggleSheet(
            onDismiss = { showFingerprintToggle = false },
            activity = activity,
            toast = { toast(it) },
            onEnableRequest = { showFingerprintEnroll = true }
        )
    }

    if (showFingerprintEnroll) {
        FingerprintEnrollDialog(
            onDismiss = { showFingerprintEnroll = false },
            activity = activity,
            toast = { toast(it) }
        )
    }

    // 长按分组菜单
    sheetGroup?.let { g ->
        ActionSheet(
            title = g.name.ifBlank { "未命名分组" },
            subtitle = "${CryptoVault.entryCountInGroup(g.id)} 条密码",
            onDismiss = { sheetGroup = null },
            actions = listOf(
                SheetAction(Icons.Default.OpenInNew, "打开") {
                    sheetGroup = null; onOpenGroup(g.id)
                },
                SheetAction(Icons.Default.Edit, "编辑") {
                    sheetGroup = null
                    groupDialog = g
                    showGroupDialog = true
                },
                SheetAction(Icons.Default.Delete, "删除", destructive = true) {
                    sheetGroup = null
                    groupToDelete = g
                }
            )
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
                    sheetEntry = null; context.copyText("用户名", e.username)
                },
                SheetAction(Icons.Default.ContentCopy, "复制密码") {
                    sheetEntry = null; context.copyText("密码", e.password)
                },
                SheetAction(Icons.Default.Link, "复制网址") {
                    sheetEntry = null; context.copyText("网址", e.url)
                },
                SheetAction(Icons.Default.Delete, "删除", destructive = true) {
                    sheetEntry = null; entryToDelete = e
                }
            )
        )
    }
}

/** 复制文本并提示（供长按菜单使用）。 */
private fun android.content.Context.copyText(label: String, value: String) {
    if (value.isBlank()) {
        Toast.makeText(this, "${label}为空", Toast.LENGTH_SHORT).show()
        return
    }
    ClipboardHelper.copy(this, label, value)
    Toast.makeText(this, "已复制$label", Toast.LENGTH_SHORT).show()
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(color.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun GroupCard(
    group: Group,
    count: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val color = groupIconColor(group.iconKey)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                BrandIcon(iconKey = group.iconKey, size = 26)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    group.name.ifBlank { "未命名分组" },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "$count 条密码",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EntryCard(
    entry: Entry,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val color = groupIconColor(entry.iconKey)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
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

@Composable
private fun groupIconColor(iconKey: String?): Color = iconColor(iconKey)

/** 临时助手：避免未定义 stringResource 的编译问题（统一从此取字符串）。 */
@Composable
private fun stringResourceSafe(id: Int): String = androidx.compose.ui.res.stringResource(id)

/* -------------------- 分组对话框 -------------------- */
@Composable
private fun GroupDialog(
    existing: Group?,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name.orEmpty()) }
    var iconKey by remember { mutableStateOf(existing?.iconKey) }
    var error by remember { mutableStateOf<String?>(null) }
    var showIconPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (existing != null) "编辑分组" else "新建分组",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("分组名称") },
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showIconPicker = true }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        BrandIcon(iconKey = iconKey, size = 22)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text("选择图标", style = MaterialTheme.typography.bodyLarge)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isBlank()) {
                    error = "请输入分组名称"
                    return@TextButton
                }
                onSave(name.trim(), iconKey)
            }) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )

    if (showIconPicker) {
        IconPickerSheet(
            currentKey = iconKey,
            onDismiss = { showIconPicker = false },
            onPicked = { iconKey = it; showIconPicker = false }
        )
    }
}

/* -------------------- 修改主密码 -------------------- */
@Composable
private fun ChangePasswordDialog(onDismiss: () -> Unit, toast: (String) -> Unit) {
    val context = LocalContext.current
    val activity = context as FragmentActivity
    var old by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("修改主密码", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                OutlinedTextField(old, { old = it }, label = { Text("当前主密码") },
                    singleLine = true, visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(new, { new = it }, label = { Text("新主密码") },
                    singleLine = true, visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(confirm, { confirm = it }, label = { Text("确认新主密码") },
                    singleLine = true, visualTransformation = PasswordVisualTransformation(),
                    isError = error != null, supportingText = error?.let { { Text(it) } },
                    shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                when {
                    new.length < 6 -> { error = "新密码至少 6 位"; return@TextButton }
                    new != confirm -> { error = "两次输入不一致"; return@TextButton }
                }
                error = null
                busy = true
                Thread {
                    val ok = CryptoVault.changeMasterPassword(context, old, new)
                    activity.runOnUiThread {
                        busy = false
                        if (ok) {
                            FingerprintHelper.clearAll(context)
                            onDismiss()
                            toast("主密码已修改")
                        } else {
                            error = "当前主密码错误"
                        }
                    }
                }.start()
            }, enabled = !busy) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

/* -------------------- 指纹设置 -------------------- */
@Composable
private fun FingerprintToggleSheet(
    onDismiss: () -> Unit,
    activity: FragmentActivity,
    toast: (String) -> Unit,
    onEnableRequest: () -> Unit
) {
    val context = LocalContext.current
    val enabled = remember { FingerprintHelper.isEnabled(context) }
    val available = remember { BiometricAuth.canAuthenticate(context) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("指纹设置", style = MaterialTheme.typography.titleLarge) },
        text = {
            Text(
                when {
                    !available -> "此设备不支持指纹解锁"
                    enabled -> "当前已启用指纹解锁，点击下方按钮可关闭。"
                    else -> "启用后可使用指纹快速解锁应用，无需每次输入主密码。"
                }
            )
        },
        confirmButton = {
            TextButton(onClick = {
                if (!available) { onDismiss(); return@TextButton }
                if (enabled) {
                    FingerprintHelper.clearAll(context)
                    toast("已关闭指纹解锁")
                    onDismiss()
                } else {
                    onDismiss()
                    onEnableRequest()
                }
            }) { Text(if (enabled) "关闭指纹" else "启用指纹") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

/** 启用指纹：先验证当前主密码，再用生物认证加密保存。 */
@Composable
private fun FingerprintEnrollDialog(
    onDismiss: () -> Unit,
    activity: FragmentActivity,
    toast: (String) -> Unit
) {
    val context = LocalContext.current
    var pwd by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    fun startEnroll(pwd: String) {
        val cipher = FingerprintHelper.getEncryptCipher()
        if (cipher == null) {
            toast(context.getString(R.string.fingerprint_key_invalid))
            onDismiss()
            return
        }
        BiometricAuth.prompt(
            activity = activity,
            title = context.getString(R.string.fingerprint_setup_title),
            subtitle = context.getString(R.string.fingerprint_setup_subtitle),
            cipher = BiometricPrompt.CryptoObject(cipher),
            onSuccess = { result ->
                result?.cipher?.let {
                    if (FingerprintHelper.saveEncryptedPassword(context, it, pwd)) {
                        toast(context.getString(R.string.fingerprint_enabled_toast))
                    }
                }
                onDismiss()
            },
            onError = { _, _ -> onDismiss() }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("启用指纹解锁", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                Text("请输入当前主密码以启用：", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    pwd,
                    { pwd = it; error = null },
                    label = { Text("主密码") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (pwd.isEmpty()) { error = "请输入主密码"; return@TextButton }
                error = null
                busy = true
                Thread {
                    val ok = CryptoVault.unlock(context, pwd)
                    activity.runOnUiThread {
                        busy = false
                        if (ok) {
                            onDismiss()
                            startEnroll(pwd)
                        } else {
                            error = context.getString(R.string.err_wrong_password)
                        }
                    }
                }.start()
            }, enabled = !busy) { Text("验证并启用") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
