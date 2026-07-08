package com.mobile.passwordmanager

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.biometric.BiometricManager
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * 指纹解锁辅助类。
 * 使用 Android Keystore 中受生物认证保护的 AES 密钥来加密/解密主密码，
 * 从而实现"指纹验证成功 → 取出主密码 → 解锁金库"的流程。
 */
object FingerprintHelper {

    private const val PREFS = "fingerprint_prefs"
    private const val KEY_ENABLED = "fp_enabled"
    private const val KEY_CIPHERTEXT = "fp_ciphertext"
    private const val KEY_IV = "fp_iv"

    private const val KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "pw_manager_fp_key"
    private const val GCM_TAG_BITS = 128

    /** 设备是否支持指纹认证 */
    fun canAuthenticate(context: Context): Boolean {
        val bm = BiometricManager.from(context)
        return bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
                BiometricManager.BIOMETRIC_SUCCESS
    }

    /** 指纹解锁是否已启用 */
    fun isEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_ENABLED, false) && getStoredCiphertext(context) != null
    }

    /**
     * 创建用于加密主密码的 Cipher（需要生物认证才能使用）。
     * 返回 null 表示密钥不可用或创建失败。
     */
    fun getEncryptCipher(): Cipher? {
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
            cipher
        } catch (e: KeyPermanentlyInvalidatedException) {
            removeKey()
            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 创建用于解密主密码的 Cipher（需要生物认证才能使用）。
     * 返回 null 表示密钥已失效或数据不存在。
     */
    fun getDecryptCipher(context: Context): Cipher? {
        val ivStr = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_IV, null) ?: return null
        return try {
            val iv = Base64.decode(ivStr, Base64.NO_WRAP)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, getKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
            cipher
        } catch (e: KeyPermanentlyInvalidatedException) {
            removeKey()
            clearStored(context)
            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 使用已通过生物认证的 Cipher 加密并保存主密码。
     */
    fun saveEncryptedPassword(context: Context, cipher: Cipher, masterPassword: String): Boolean {
        return try {
            val encrypted = cipher.doFinal(masterPassword.toByteArray(Charsets.UTF_8))
            val iv = cipher.iv
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putBoolean(KEY_ENABLED, true)
                .putString(KEY_CIPHERTEXT, Base64.encodeToString(encrypted, Base64.NO_WRAP))
                .putString(KEY_IV, Base64.encodeToString(iv, Base64.NO_WRAP))
                .apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 使用已通过生物认证的 Cipher 解密并返回主密码。
     */
    fun retrievePassword(context: Context, cipher: Cipher): String? {
        val ciphertext = getStoredCiphertext(context) ?: return null
        return try {
            val decrypted = cipher.doFinal(Base64.decode(ciphertext, Base64.NO_WRAP))
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    /** 关闭指纹解锁，清除存储的密文（不删除 Keystore 密钥） */
    fun disable(context: Context) {
        clearStored(context)
    }

    /** 彻底清除：存储数据 + Keystore 密钥 */
    fun clearAll(context: Context) {
        clearStored(context)
        removeKey()
    }

    // ---------- 内部方法 ----------

    private fun getStoredCiphertext(context: Context): String? {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_CIPHERTEXT, null)
    }

    private fun clearStored(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .clear()
            .apply()
    }

    private fun getKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE)
        keyStore.load(null)
        return (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE)
        keyStore.load(null)
        if (keyStore.containsAlias(KEY_ALIAS)) {
            return (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        }
        return generateKey()
    }

    private fun generateKey(): SecretKey {
        val builder = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setInvalidatedByBiometricEnrollment(true)
        }

        val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
        keyGen.init(builder.build())
        return keyGen.generateKey()
    }

    private fun removeKey() {
        try {
            val keyStore = KeyStore.getInstance(KEYSTORE)
            keyStore.load(null)
            keyStore.deleteEntry(KEY_ALIAS)
        } catch (_: Exception) {
        }
    }
}
