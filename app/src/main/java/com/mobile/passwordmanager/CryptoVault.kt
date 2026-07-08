package com.mobile.passwordmanager

import android.content.Context
import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * 加密保险库:用主密码经 PBKDF2 派生 AES-256 密钥,再用 AES-GCM 加密所有记录。
 *
 * 落盘结构(SharedPreferences "vault"):
 *   salt  : Base64(16 字节随机盐)
 *   ver   : Base64(IV || 密文)  加密的固定校验串 "VAULT_OK"
 *   data  : Base64(IV || 密文)  加密的密码记录 JSON 数组
 *   gdata : Base64(IV || 密文)  加密的分组 JSON 数组
 *
 * 解锁后 [key]、[entries] 与 [groups] 保留在内存;[lock] 清空它们。
 */
object CryptoVault {

    private const val PREFS = "vault"
    private const val KEY_SALT = "salt"
    private const val KEY_VER = "ver"
    private const val KEY_DATA = "data"
    private const val KEY_GDATA = "gdata"
    private const val VERIFY_PLAIN = "VAULT_OK"

    private const val SALT_LEN = 16
    private const val IV_LEN = 12
    private const val GCM_TAG_BITS = 128
    private const val PBKDF2_ITER = 200_000
    private const val KEY_LEN_BITS = 256

    /** 全局解锁状态:Activity 间共享,进程被杀后需重新输入主密码。 */
    @Volatile
    private var key: SecretKey? = null

    @Volatile
    var entries: MutableList<Entry> = ArrayList()
        private set

    @Volatile
    var groups: MutableList<Group> = ArrayList()
        private set

    @Volatile
    var unlocked: Boolean = false
        private set

    fun isInitialized(ctx: Context): Boolean =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).contains(KEY_SALT)

    private fun deriveKey(password: CharArray, salt: ByteArray): SecretKey {
        val spec = PBEKeySpec(password, salt, PBKDF2_ITER, KEY_LEN_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val derived = factory.generateSecret(spec).encoded
        spec.clearPassword()
        return SecretKeySpec(derived, "AES")
    }

    private fun encrypt(plain: ByteArray, sk: SecretKey): ByteArray {
        val iv = ByteArray(IV_LEN).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, sk, GCMParameterSpec(GCM_TAG_BITS, iv))
        val ct = cipher.doFinal(plain)
        return iv + ct
    }

    private fun decrypt(blob: ByteArray, sk: SecretKey): ByteArray {
        require(blob.size > IV_LEN) { "损坏的密文" }
        val iv = blob.copyOfRange(0, IV_LEN)
        val ct = blob.copyOfRange(IV_LEN, blob.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, sk, GCMParameterSpec(GCM_TAG_BITS, iv))
        return cipher.doFinal(ct)
    }

    private fun b64Encode(b: ByteArray): String =
        Base64.encodeToString(b, Base64.NO_WRAP)

    private fun b64Decode(s: String): ByteArray =
        Base64.decode(s, Base64.NO_WRAP)

    /** 首次使用:用主密码初始化保险库(生成盐、写入校验串、空记录)。 */
    fun setup(ctx: Context, masterPassword: String) {
        require(masterPassword.isNotEmpty()) { "主密码不能为空" }
        val salt = ByteArray(SALT_LEN).also { SecureRandom().nextBytes(it) }
        val sk = deriveKey(masterPassword.toCharArray(), salt)
        val verBlob = encrypt(VERIFY_PLAIN.toByteArray(Charsets.UTF_8), sk)
        val dataBlob = encrypt(Entry.listToJson(emptyList()).toByteArray(Charsets.UTF_8), sk)
        val gdataBlob = encrypt(Group.listToJson(emptyList()).toByteArray(Charsets.UTF_8), sk)
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_SALT, b64Encode(salt))
            .putString(KEY_VER, b64Encode(verBlob))
            .putString(KEY_DATA, b64Encode(dataBlob))
            .putString(KEY_GDATA, b64Encode(gdataBlob))
            .apply()
        key = sk
        entries = ArrayList()
        groups = ArrayList()
        unlocked = true
    }

    /** 用主密码解锁:校验通过则把记录与分组读入内存。 */
    fun unlock(ctx: Context, masterPassword: String): Boolean {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val saltStr = prefs.getString(KEY_SALT, null) ?: return false
        val verStr = prefs.getString(KEY_VER, null) ?: return false
        val dataStr = prefs.getString(KEY_DATA, null) ?: return false

        val salt = b64Decode(saltStr)
        val sk = deriveKey(masterPassword.toCharArray(), salt)
        return try {
            val verPlain = decrypt(b64Decode(verStr), sk).toString(Charsets.UTF_8)
            if (verPlain != VERIFY_PLAIN) return false
            val dataPlain = decrypt(b64Decode(dataStr), sk).toString(Charsets.UTF_8)
            key = sk
            entries = ArrayList(Entry.listFromJson(dataPlain))
            // gdata 可能在旧版本中不存在,此时视为空分组列表
            val gdataStr = prefs.getString(KEY_GDATA, null)
            groups = if (gdataStr != null) {
                ArrayList(Group.listFromJson(decrypt(b64Decode(gdataStr), sk).toString(Charsets.UTF_8)))
            } else {
                ArrayList()
            }
            unlocked = true
            true
        } catch (e: Exception) {
            false
        }
    }

    /** 锁定:清空内存中的密钥、记录与分组。 */
    fun lock() {
        key = null
        entries = ArrayList()
        groups = ArrayList()
        unlocked = false
    }

    /** 修改主密码:用新密码重新加密现有数据。 */
    fun changeMasterPassword(ctx: Context, oldPassword: String, newPassword: String): Boolean {
        if (!unlock(ctx, oldPassword)) return false
        require(newPassword.isNotEmpty()) { "新主密码不能为空" }
        val salt = ByteArray(SALT_LEN).also { SecureRandom().nextBytes(it) }
        val sk = deriveKey(newPassword.toCharArray(), salt)
        val verBlob = encrypt(VERIFY_PLAIN.toByteArray(Charsets.UTF_8), sk)
        val dataBlob = encrypt(
            Entry.listToJson(entries).toByteArray(Charsets.UTF_8), sk
        )
        val gdataBlob = encrypt(
            Group.listToJson(groups).toByteArray(Charsets.UTF_8), sk
        )
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_SALT, b64Encode(salt))
            .putString(KEY_VER, b64Encode(verBlob))
            .putString(KEY_DATA, b64Encode(dataBlob))
            .putString(KEY_GDATA, b64Encode(gdataBlob))
            .apply()
        key = sk
        unlocked = true
        return true
    }

    /** 把当前 [entries] 与 [groups] 重新加密落盘。要求已解锁。 */
    fun save(ctx: Context) {
        val sk = key ?: throw IllegalStateException("保险库未解锁")
        val dataBlob = encrypt(
            Entry.listToJson(entries).toByteArray(Charsets.UTF_8), sk
        )
        val gdataBlob = encrypt(
            Group.listToJson(groups).toByteArray(Charsets.UTF_8), sk
        )
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_DATA, b64Encode(dataBlob))
            .putString(KEY_GDATA, b64Encode(gdataBlob))
            .apply()
    }

    // ---- Entry CRUD ----

    fun upsert(entry: Entry) {
        val idx = entries.indexOfFirst { it.id == entry.id }
        if (idx >= 0) entries[idx] = entry else entries.add(entry)
    }

    fun deleteEntry(id: String) {
        entries.removeAll { it.id == id }
    }

    // ---- Group CRUD ----

    fun upsertGroup(group: Group) {
        val idx = groups.indexOfFirst { it.id == group.id }
        if (idx >= 0) groups[idx] = group else groups.add(group)
    }

    fun deleteGroup(id: String) {
        groups.removeAll { it.id == id }
        // 分组内的密码移回主页(groupId = null)
        entries.forEachIndexed { i, e ->
            if (e.groupId == id) {
                entries[i] = e.copy(groupId = null)
            }
        }
    }

    fun entriesInGroup(groupId: String): List<Entry> =
        entries.filter { it.groupId == groupId }

    fun entryCountInGroup(groupId: String): Int =
        entries.count { it.groupId == groupId }
}
