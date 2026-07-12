package com.mobile.passwordmanager

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mikepenz.iconics.IconicsDrawable
import com.mobile.passwordmanager.databinding.ActivityMainBinding
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val adapter = HomeAdapter(
        onGroupClick = { openGroup(it) },
        onGroupDelete = { confirmDeleteGroup(it) },
        onEntryClick = { editEntry(it) },
        onEntryCopy = { ClipboardHelper.copy(this, "密码", it.password) },
        onEntryDelete = { confirmDeleteEntry(it) }
    )

    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { refresh() }

    private val groupLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { refresh() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = getString(R.string.app_name)

        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter
        binding.recycler.itemAnimator?.let {
            it.addDuration = 280
            it.changeDuration = 200
            it.moveDuration = 280
        }

        binding.fabAdd.setOnClickListener { showAddDialog() }
        refresh()
    }

    override fun onResume() {
        super.onResume()
        // 若已被锁定(例如从后台返回),回到解锁界面
        if (!CryptoVault.unlocked) {
            startActivity(Intent(this, UnlockActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            finish()
            return
        }
        refresh()
    }

    private fun refresh() {
        val groups = CryptoVault.groups.sortedBy { it.name.lowercase() }
        val homeEntries = CryptoVault.entries
            .filter { it.groupId == null }
            .sortedBy { it.title.lowercase() }

        if (groups.isEmpty() && homeEntries.isEmpty()) {
            adapter.submitList(emptyList())
            binding.emptyLayout.visibility = View.VISIBLE
            return
        }
        binding.emptyLayout.visibility = View.GONE

        val items = mutableListOf<HomeItem>()

        // ---- 分组区域(在上) ----
        if (groups.isNotEmpty()) {
            items.add(HomeItem.GroupHeader(getString(R.string.section_groups)))
            groups.forEach { g ->
                items.add(HomeItem.GroupRow(g, CryptoVault.entryCountInGroup(g.id)))
            }
        }

        // ---- 密码区域(在下) ----
        items.add(HomeItem.EntryHeader(getString(R.string.section_passwords)))
        homeEntries.forEach { e ->
            items.add(HomeItem.EntryRow(e))
        }

        adapter.submitList(items)
    }

    // ---- FAB:选择创建分组或密码 ----

    private fun showAddDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_choose_add_type, null)
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_choose_add)
            .setView(view)
            .create()

        view.findViewById<View>(R.id.optionGroup).setOnClickListener {
            dialog.dismiss()
            showNewGroupDialog()
        }
        view.findViewById<View>(R.id.optionPassword).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, AddEditActivity::class.java))
        }

        dialog.show()
    }

    private fun showNewGroupDialog(existingGroup: Group? = null) {
        val view = layoutInflater.inflate(R.layout.dialog_new_group, null)
        val til = view.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilGroupName)
        val input = view.findViewById<EditText>(R.id.etGroupName)
        val rowIconPicker = view.findViewById<View>(R.id.rowGroupIconPicker)
        val ivDefault = view.findViewById<android.widget.ImageView>(R.id.ivGroupIconDefault)
        val ivBrand = view.findViewById<com.mikepenz.iconics.view.IconicsImageView>(R.id.ivGroupIconBrand)

        var selectedIconKey: String? = existingGroup?.iconKey
        input.setText(existingGroup?.name.orEmpty())

        fun refreshIconPreview() {
            val entry = IconCatalog.find(selectedIconKey)
            if (entry != null) {
                ivBrand.setIcon(IconicsDrawable(view.context, entry.icon))
                ivBrand.visibility = View.VISIBLE
                ivDefault.visibility = View.GONE
            } else {
                ivBrand.visibility = View.GONE
                ivDefault.visibility = View.VISIBLE
            }
        }
        refreshIconPreview()

        rowIconPicker.setOnClickListener {
            IconPickerDialog.show(this, selectedIconKey) { picked ->
                selectedIconKey = picked
                refreshIconPreview()
            }
        }

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(if (existingGroup != null) R.string.title_group else R.string.dialog_new_group)
            .setView(view)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save) { _, _ -> }
            .create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val name = input.text?.toString()?.trim().orEmpty()
            if (name.isEmpty()) {
                til.error = getString(R.string.err_group_name_required)
                return@setOnClickListener
            }
            til.error = null
            val group = Group(
                id = existingGroup?.id ?: UUID.randomUUID().toString(),
                name = name,
                createdAt = existingGroup?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                iconKey = selectedIconKey
            )
            CryptoVault.upsertGroup(group)
            CryptoVault.save(this)
            dialog.dismiss()
            refresh()
        }
    }

    // ---- 分组操作 ----

    private fun openGroup(group: Group) {
        val intent = Intent(this, GroupActivity::class.java).apply {
            putExtra(GroupActivity.EXTRA_GROUP_ID, group.id)
        }
        groupLauncher.launch(intent)
    }

    private fun confirmDeleteGroup(group: Group) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete)
            .setMessage(getString(R.string.confirm_delete_group, group.name.ifBlank { getString(R.string.untitled) }))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                CryptoVault.deleteGroup(group.id)
                CryptoVault.save(this)
                refresh()
            }
            .show()
    }

    // ---- 密码操作 ----

    private fun editEntry(entry: Entry) {
        val intent = Intent(this, AddEditActivity::class.java).apply {
            putExtra(AddEditActivity.EXTRA_ID, entry.id)
        }
        editLauncher.launch(intent)
    }

    private fun confirmDeleteEntry(entry: Entry) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete)
            .setMessage(getString(R.string.confirm_delete, entry.title.ifBlank { getString(R.string.untitled) }))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                CryptoVault.deleteEntry(entry.id)
                CryptoVault.save(this)
                refresh()
            }
            .show()
    }

    // ---- 菜单 ----

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_lock -> { lockAndExit(); true }
        R.id.action_change_password -> { showChangePasswordDialog(); true }
        R.id.action_fingerprint -> { showFingerprintToggle(); true }
        else -> super.onOptionsItemSelected(item)
    }

    private fun lockAndExit() {
        CryptoVault.lock()
        startActivity(Intent(this, UnlockActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        finish()
    }

    private fun showChangePasswordDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.action_change_password)
            .setView(view)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save) { _, _ -> }
            .create()
        dialog.show()

        // 用 show 后取 button,避免点错时直接关闭
        val btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        btn.setOnClickListener {
            val old = view.findViewById<EditText>(R.id.etOld).text.toString()
            val new = view.findViewById<EditText>(R.id.etNew).text.toString()
            val confirm = view.findViewById<EditText>(R.id.etNewConfirm).text.toString()
            when {
                new.length < 6 -> toast(R.string.err_password_too_short)
                new != confirm -> toast(R.string.err_password_mismatch)
                else -> {
                    Thread {
                        val ok = CryptoVault.changeMasterPassword(this, old, new)
                        runOnUiThread {
                            if (ok) {
                                // 主密码已变更，旧指纹密文失效，清除指纹
                                FingerprintHelper.clearAll(this)
                                toast(R.string.msg_password_changed)
                                dialog.dismiss()
                            }
                            else toast(R.string.err_wrong_password)
                        }
                    }.start()
                }
            }
        }
    }

    // ---- 指纹设置 ----

    private fun showFingerprintToggle() {
        if (!FingerprintHelper.canAuthenticate(this)) {
            Toast.makeText(this, R.string.fingerprint_not_available, Toast.LENGTH_SHORT).show()
            return
        }

        if (FingerprintHelper.isEnabled(this)) {
            // 已启用 → 询问是否关闭
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.fingerprint_disable_title)
                .setMessage(R.string.fingerprint_disable_msg)
                .setPositiveButton(R.string.delete) { _, _ ->
                    FingerprintHelper.clearAll(this)
                    Toast.makeText(this, R.string.fingerprint_disabled_toast, Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        } else {
            // 未启用 → 启用流程：需要当前主密码来加密保存
            promptCurrentPasswordForFingerprint()
        }
    }

    /**
     * 启用指纹前需验证当前主密码（作为加密对象）。
     */
    private fun promptCurrentPasswordForFingerprint() {
        val input = EditText(this).apply {
            hint = getString(R.string.hint_master_password)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setPadding(48, 32, 48, 0)
        }
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.enable_fingerprint)
            .setMessage(R.string.enable_fingerprint_msg)
            .setView(input)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.enable) { _, _ -> }
            .create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val pwd = input.text?.toString().orEmpty()
            if (pwd.isEmpty()) {
                input.error = getString(R.string.err_empty_password)
                return@setOnClickListener
            }
            // 验证密码是否正确
            Thread {
                val ok = CryptoVault.unlock(this, pwd)
                runOnUiThread {
                    if (ok) {
                        dialog.dismiss()
                        startFingerprintEnroll(pwd)
                    } else {
                        input.error = getString(R.string.err_wrong_password)
                    }
                }
            }.start()
        }
    }

    private fun startFingerprintEnroll(masterPassword: String) {
        val cipher = FingerprintHelper.getEncryptCipher()
        if (cipher == null) {
            Toast.makeText(this, R.string.fingerprint_key_invalid, Toast.LENGTH_SHORT).show()
            return
        }
        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                result.cryptoObject?.cipher?.let { c ->
                    if (FingerprintHelper.saveEncryptedPassword(this@MainActivity, c, masterPassword)) {
                        Toast.makeText(this@MainActivity, R.string.fingerprint_enabled_toast, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.fingerprint_setup_title))
            .setSubtitle(getString(R.string.fingerprint_setup_subtitle))
            .setNegativeButtonText(getString(R.string.cancel))
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
        prompt.authenticate(info, BiometricPrompt.CryptoObject(cipher))
    }

    private fun toast(resId: Int) =
        android.widget.Toast.makeText(this, resId, android.widget.Toast.LENGTH_SHORT).show()
}
