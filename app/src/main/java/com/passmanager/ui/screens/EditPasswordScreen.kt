package com.passmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.passmanager.data.model.PasswordEntry
import com.passmanager.ui.PasswordEditViewModel
import com.passmanager.ui.theme.*
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPasswordScreen(
    id: Long?,
    initialGroupId: Long?,
    navController: NavController,
    viewModel: PasswordEditViewModel = hiltViewModel()
) {
    val entry by viewModel.entry.collectAsStateWithLifecycle()
    val groups by viewModel.groups.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var siteName by remember { mutableStateOf("") }
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedGroupId by remember { mutableStateOf(initialGroupId) }
    var showPassword by remember { mutableStateOf(false) }
    var groupExpanded by remember { mutableStateOf(false) }
    var isLoaded by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }

    LaunchedEffect(id) {
        if (id != null) viewModel.loadPassword(id)
        else isLoaded = true
    }

    LaunchedEffect(entry) {
        if (id != null && !isLoaded) {
            entry?.let { e ->
                name = e.name
                siteName = e.siteName
                account = e.account
                password = if (e.encryptedPassword.isNotEmpty()) viewModel.decryptPassword(e.encryptedPassword) else ""
                notes = e.notes
                selectedGroupId = e.groupId
                isLoaded = true
            }
        }
    }

    fun generatePassword(): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#\$%^&*"
        return (1..16).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text(if (id == null) "添加密码" else "编辑密码", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (name.isBlank()) { nameError = true; return@TextButton }
                            val newEntry = PasswordEntry(
                                id = id ?: 0L,
                                name = name.trim(),
                                siteName = siteName.trim(),
                                account = account.trim(),
                                encryptedPassword = password,
                                notes = notes.trim(),
                                groupId = selectedGroupId,
                                isFavorite = entry?.isFavorite ?: false,
                                createdAt = entry?.createdAt ?: System.currentTimeMillis()
                            )
                            viewModel.savePassword(newEntry) { navController.popBackStack() }
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Name field
            EditField(
                value = name,
                onValueChange = { name = it; nameError = false },
                label = "名称 *",
                placeholder = "例如：Gmail",
                icon = Icons.Outlined.Label,
                isError = nameError,
                errorMessage = "名称不能为空"
            )

            EditField(
                value = siteName,
                onValueChange = { siteName = it },
                label = "网站/应用名称",
                placeholder = "例如：gmail.com",
                icon = Icons.Outlined.Language
            )

            EditField(
                value = account,
                onValueChange = { account = it },
                label = "账号",
                placeholder = "邮箱或用户名",
                icon = Icons.Outlined.Person
            )

            // Password field with generator
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("密码") },
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = OnSurfaceVariant) },
                trailingIcon = {
                    Row {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = null,
                                tint = OnSurfaceVariant
                            )
                        }
                        IconButton(onClick = { password = generatePassword(); showPassword = true }) {
                            Icon(Icons.Outlined.AutoAwesome, contentDescription = "生成密码", tint = Secondary)
                        }
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = outlinedFieldColors()
            )

            // Group selector
            if (groups.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = groupExpanded,
                    onExpandedChange = { groupExpanded = it }
                ) {
                    OutlinedTextField(
                        value = groups.find { it.id == selectedGroupId }?.name ?: "无分组",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("分组") },
                        leadingIcon = { Icon(Icons.Outlined.FolderOpen, contentDescription = null, tint = OnSurfaceVariant) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = outlinedFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = groupExpanded,
                        onDismissRequest = { groupExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("无分组") },
                            onClick = { selectedGroupId = null; groupExpanded = false }
                        )
                        groups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group.name) },
                                onClick = { selectedGroupId = group.id; groupExpanded = false },
                                leadingIcon = {
                                    androidx.compose.foundation.Canvas(modifier = Modifier.size(10.dp)) {
                                        drawCircle(androidx.compose.ui.graphics.Color(group.color))
                                    }
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                label = { Text("备注") },
                leadingIcon = { Icon(Icons.Outlined.Notes, contentDescription = null, tint = OnSurfaceVariant) },
                maxLines = 6,
                shape = RoundedCornerShape(12.dp),
                colors = outlinedFieldColors()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun EditField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isError: Boolean = false,
    errorMessage: String = ""
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = { Text(placeholder, color = OnSurfaceVariant.copy(alpha = 0.5f)) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = OnSurfaceVariant) },
            isError = isError,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = outlinedFieldColors()
        )
        if (isError && errorMessage.isNotEmpty()) {
            Text(errorMessage, color = Error, fontSize = 11.sp, modifier = Modifier.padding(start = 16.dp, top = 2.dp))
        }
    }
}

@Composable
fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Secondary,
    unfocusedBorderColor = Divider,
    focusedContainerColor = Background,
    unfocusedContainerColor = Background,
    focusedLabelColor = Secondary
)
