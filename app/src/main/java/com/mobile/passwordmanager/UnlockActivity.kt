package com.mobile.passwordmanager

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mobile.passwordmanager.databinding.ActivityUnlockBinding

/**
 * 启动入口:首次运行设置主密码,之后用主密码解锁。
 * 支持指纹解锁:设置密码后可启用,之后进入时优先弹出指纹。
 */
class UnlockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUnlockBinding
    private var isSetup = false
    private var busy = false
    private var pendingPassword: String? = null   // 设置密码后待加密保存

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnlockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isSetup = !CryptoVault.isInitialized(this)
        applyMode()

        binding.btnAction.setOnClickListener { handleAction() }
        binding.btnFingerprint.setOnClickListener { startFingerprintUnlock() }

        binding.etConfirm.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handleAction(); true
            } else false
        }
        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                handleAction(); true
            } else false
        }

        // 已设置密码 + 已启用指纹 → 自动弹出指纹
        if (!isSetup && FingerprintHelper.isEnabled(this)) {
            binding.btnFingerprint.post { startFingerprintUnlock() }
        }
    }

    private fun applyMode() {
        if (isSetup) {
            binding.tvTitle.text = getString(R.string.title_setup)
            binding.tvSubtitle.text = getString(R.string.subtitle_setup)
            binding.tilConfirm.visibility = View.VISIBLE
            binding.tvWarn.visibility = View.VISIBLE
            binding.btnAction.text = getString(R.string.action_create)
            binding.btnFingerprint.visibility = View.GONE
            binding.etPassword.imeOptions = EditorInfo.IME_ACTION_NEXT
        } else {
            binding.tvTitle.text = getString(R.string.title_unlock)
            binding.tvSubtitle.text = getString(R.string.subtitle_unlock)
            binding.tilConfirm.visibility = View.GONE
            binding.tvWarn.visibility = View.GONE
            binding.btnAction.text = getString(R.string.action_unlock)
            // 指纹已启用时显示指纹按钮
            binding.btnFingerprint.visibility =
                if (FingerprintHelper.isEnabled(this)) View.VISIBLE else View.GONE
            binding.etPassword.imeOptions = EditorInfo.IME_ACTION_DONE
        }
    }

    private fun setBusy(b: Boolean) {
        busy = b
        binding.progress.visibility = if (b) View.VISIBLE else View.GONE
        binding.btnAction.isEnabled = !b
        binding.etPassword.isEnabled = !b
        binding.etConfirm.isEnabled = !b
    }

    private fun handleAction() {
        if (busy) return
        binding.tilPassword.error = null
        binding.tilConfirm.error = null

        val pwd = binding.etPassword.text?.toString().orEmpty()
        if (pwd.isEmpty()) {
            binding.tilPassword.error = getString(R.string.err_empty_password)
            return
        }
        if (isSetup) {
            val confirm = binding.etConfirm.text?.toString().orEmpty()
            if (pwd.length < 6) {
                binding.tilPassword.error = getString(R.string.err_password_too_short)
                return
            }
            if (pwd != confirm) {
                binding.tilConfirm.error = getString(R.string.err_password_mismatch)
                return
            }
            runInBackground { doSetup(pwd) }
        } else {
            runInBackground { doUnlock(pwd) }
        }
    }

    private fun runInBackground(work: () -> Unit) {
        setBusy(true)
        Thread {
            try {
                work()
            } catch (e: Exception) {
                runOnUiThread {
                    setBusy(false)
                    binding.tilPassword.error = e.message ?: getString(R.string.err_unknown)
                }
            }
        }.start()
    }

    private fun doSetup(pwd: String) {
        CryptoVault.setup(this, pwd)
        runOnUiThread {
            setBusy(false)
            // 设置成功后，如果设备支持指纹，询问是否启用
            if (FingerprintHelper.canAuthenticate(this)) {
                promptEnableFingerprint(pwd)
            } else {
                enterApp()
            }
        }
    }

    private fun doUnlock(pwd: String) {
        val ok = CryptoVault.unlock(this, pwd)
        runOnUiThread {
            setBusy(false)
            if (ok) {
                // 解锁成功后，如果设备支持指纹但尚未启用，询问是否启用
                if (FingerprintHelper.canAuthenticate(this) && !FingerprintHelper.isEnabled(this)) {
                    promptEnableFingerprint(pwd)
                } else {
                    enterApp()
                }
            } else {
                binding.tilPassword.error = getString(R.string.err_wrong_password)
            }
        }
    }

    // ======================== 指纹相关 ========================

    /**
     * 询问用户是否启用指纹解锁（首次设置或密码解锁后）。
     */
    private fun promptEnableFingerprint(masterPassword: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.enable_fingerprint)
            .setMessage(R.string.enable_fingerprint_msg)
            .setPositiveButton(R.string.enable) { _, _ ->
                pendingPassword = masterPassword
                startFingerprintSetup()
            }
            .setNegativeButton(R.string.cancel) { _, _ -> enterApp() }
            .setCancelable(true)
            .setOnCancelListener { enterApp() }
            .show()
    }

    /**
     * 启用指纹：用加密 Cipher + 生物认证来保存主密码。
     */
    private fun startFingerprintSetup() {
        val cipher = FingerprintHelper.getEncryptCipher()
        if (cipher == null) {
            enterApp()
            return
        }
        val prompt = createBiometricPrompt(
            title = getString(R.string.fingerprint_setup_title),
            subtitle = getString(R.string.fingerprint_setup_subtitle),
            onSuccess = { cryptoObject ->
                val pwd = pendingPassword
                if (pwd != null) {
                    FingerprintHelper.saveEncryptedPassword(this, cryptoObject.cipher!!, pwd)
                }
                pendingPassword = null
                enterApp()
            },
            onError = { _, _ ->
                pendingPassword = null
                enterApp()
            }
        )
        prompt.authenticate(
            buildPromptInfo(
                getString(R.string.fingerprint_setup_title),
                getString(R.string.fingerprint_setup_subtitle)
            ),
            BiometricPrompt.CryptoObject(cipher)
        )
    }

    /**
     * 解锁：用解密 Cipher + 生物认证来取出主密码。
     */
    private fun startFingerprintUnlock() {
        val cipher = FingerprintHelper.getDecryptCipher(this)
        if (cipher == null) {
            // 密钥已失效，关闭指纹，回退到手动输入
            FingerprintHelper.disable(this)
            binding.btnFingerprint.visibility = View.GONE
            binding.tilPassword.error = getString(R.string.fingerprint_key_invalid)
            return
        }
        val prompt = createBiometricPrompt(
            title = getString(R.string.fingerprint_unlock_title),
            subtitle = getString(R.string.fingerprint_unlock_subtitle),
            onSuccess = { cryptoObject ->
                val pwd = FingerprintHelper.retrievePassword(this, cryptoObject.cipher!!)
                if (pwd != null) {
                    runInBackground { doUnlock(pwd) }
                } else {
                    binding.tilPassword.error = getString(R.string.fingerprint_auth_failed)
                }
            },
            onError = { _, _ -> }
        )
        prompt.authenticate(
            buildPromptInfo(
                getString(R.string.fingerprint_unlock_title),
                getString(R.string.fingerprint_unlock_subtitle)
            ),
            BiometricPrompt.CryptoObject(cipher)
        )
    }

    private fun createBiometricPrompt(
        title: String,
        subtitle: String,
        onSuccess: (BiometricPrompt.CryptoObject) -> Unit,
        onError: (Int, String) -> Unit
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(this)
        return BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                result.cryptoObject?.let { onSuccess(it) }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(errorCode, errString.toString())
            }
        })
    }

    private fun buildPromptInfo(title: String, subtitle: String): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(getString(R.string.cancel))
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
    }

    private fun enterApp() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
