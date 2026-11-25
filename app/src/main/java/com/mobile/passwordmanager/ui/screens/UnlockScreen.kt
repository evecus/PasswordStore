package com.mobile.passwordmanager.ui.screens

import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.passwordmanager.BiometricAuth
import com.mobile.passwordmanager.CryptoVault
import com.mobile.passwordmanager.FingerprintHelper
import com.mobile.passwordmanager.R
import com.mobile.passwordmanager.ui.components.GradientHeader
import com.mobile.passwordmanager.ui.theme.Indigo500

/**
 * 启动入口：首次运行设置主密码，之后用主密码或指纹解锁。
 */
@Composable
fun UnlockScreen(onUnlocked: () -> Unit) {
    val context = LocalContext.current
    val activity = context as FragmentActivity

    val isSetup = remember { !CryptoVault.isInitialized(context) }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var pendingPassword by remember { mutableStateOf<String?>(null) }
    var askEnableFingerprint by remember { mutableStateOf(false) }

    fun toast(msg: String) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    fun enterApp() {
        password = ""
        confirm = ""
        onUnlocked()
    }

    fun doUnlock(pwd: String) {
        busy = true
        Thread {
            val ok = CryptoVault.unlock(context, pwd)
            activity.runOnUiThread {
                busy = false
                if (ok) {
                    if (BiometricAuth.canAuthenticate(context) && !FingerprintHelper.isEnabled(context)) {
                        pendingPassword = pwd
                        askEnableFingerprint = true
                    } else {
                        enterApp()
                    }
                } else {
                    passwordError = context.getString(R.string.err_wrong_password)
                }
            }
        }.start()
    }

    fun doSetup(pwd: String) {
        busy = true
        Thread {
            CryptoVault.setup(context, pwd)
            activity.runOnUiThread {
                busy = false
                if (BiometricAuth.canAuthenticate(context)) {
                    pendingPassword = pwd
                    askEnableFingerprint = true
                } else {
                    enterApp()
                }
            }
        }.start()
    }

    fun handleAction() {
        passwordError = null
        confirmError = null
        if (password.isEmpty()) {
            passwordError = context.getString(R.string.err_empty_password)
            return
        }
        if (isSetup) {
            if (password.length < 6) {
                passwordError = context.getString(R.string.err_password_too_short)
                return
            }
            if (password != confirm) {
                confirmError = context.getString(R.string.err_password_mismatch)
                return
            }
            doSetup(password)
        } else {
            doUnlock(password)
        }
    }

    fun startFingerprintEnroll(pwd: String) {
        val cipher = FingerprintHelper.getEncryptCipher()
        if (cipher == null) {
            toast(context.getString(R.string.fingerprint_key_invalid))
            enterApp()
            return
        }
        BiometricAuth.prompt(
            activity = activity,
            title = context.getString(R.string.fingerprint_setup_title),
            subtitle = context.getString(R.string.fingerprint_setup_subtitle),
            cipher = BiometricPrompt.CryptoObject(cipher),
            onSuccess = { result ->
                val c = result?.cipher
                if (c != null) FingerprintHelper.saveEncryptedPassword(context, c, pwd)
                pendingPassword = null
                enterApp()
            },
            onError = { _, _ ->
                pendingPassword = null
                enterApp()
            }
        )
    }

    fun startFingerprintUnlock() {
        val cipher = FingerprintHelper.getDecryptCipher(context)
        if (cipher == null) {
            FingerprintHelper.disable(context)
            passwordError = context.getString(R.string.fingerprint_key_invalid)
            return
        }
        BiometricAuth.prompt(
            activity = activity,
            title = context.getString(R.string.fingerprint_unlock_title),
            subtitle = context.getString(R.string.fingerprint_unlock_subtitle),
            cipher = BiometricPrompt.CryptoObject(cipher),
            onSuccess = { result ->
                val c = result?.cipher
                val pwd = if (c != null) FingerprintHelper.retrievePassword(context, c) else null
                if (pwd != null) doUnlock(pwd)
                else passwordError = context.getString(R.string.fingerprint_auth_failed)
            }
        )
    }

    LaunchedEffect(Unit) {
        if (!isSetup && FingerprintHelper.isEnabled(context)) {
            startFingerprintUnlock()
        }
    }

    if (askEnableFingerprint) {
        AlertDialog(
            onDismissRequest = { askEnableFingerprint = false; enterApp() },
            title = { Text(stringResource(R.string.enable_fingerprint), style = MaterialTheme.typography.titleLarge) },
            text = { Text(stringResource(R.string.enable_fingerprint_msg)) },
            confirmButton = {
                TextButton(onClick = {
                    askEnableFingerprint = false
                    val pwd = pendingPassword
                    if (pwd != null) startFingerprintEnroll(pwd) else enterApp()
                }) { Text(stringResource(R.string.enable)) }
            },
            dismissButton = {
                TextButton(onClick = { askEnableFingerprint = false; enterApp() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GradientHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        Modifier
                            .size(132.dp)
                            .clip(CircleShape)
                            .drawBehind { drawCircle(Color.White.copy(alpha = 0.12f)) }
                    )
                    Box(
                        Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .drawBehind { drawCircle(Color.White.copy(alpha = 0.20f)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(46.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(if (isSetup) R.string.title_setup else R.string.title_unlock),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 30.sp),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(if (isSetup) R.string.subtitle_setup else R.string.subtitle_unlock),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(28.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; passwordError = null },
                    label = { Text(stringResource(R.string.hint_master_password)) },
                    singleLine = true,
                    isError = passwordError != null,
                    supportingText = passwordError?.let { { Text(it) } },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                AnimatedVisibility(visible = isSetup) {
                    Column {
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = confirm,
                            onValueChange = { confirm = it; confirmError = null },
                            label = { Text(stringResource(R.string.hint_confirm_password)) },
                            singleLine = true,
                            isError = confirmError != null,
                            supportingText = confirmError?.let { { Text(it) } },
                            visualTransformation = PasswordVisualTransformation(),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                AnimatedVisibility(visible = isSetup) {
                    val warnBg = MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                    Text(
                        text = stringResource(R.string.warn_master_password),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .drawBehind { drawRect(color = warnBg) }
                            .padding(12.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { handleAction() },
                    enabled = !busy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Indigo500,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    if (busy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            stringResource(if (isSetup) R.string.action_create else R.string.action_unlock),
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
                        )
                    }
                }

                val fpEnabled = remember { FingerprintHelper.isEnabled(context) }
                AnimatedVisibility(visible = !isSetup && fpEnabled) {
                    Column {
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { startFingerprintUnlock() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null)
                            Spacer(Modifier.size(10.dp))
                            Text(stringResource(R.string.fingerprint_unlock))
                        }
                    }
                }
            }
        }
    }
}
