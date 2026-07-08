package com.mobile.passwordmanager

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mobile.passwordmanager.databinding.ActivityAddEditBinding
import java.util.UUID

/**
 * 新增 / 编辑一条密码记录。传入 [EXTRA_ID] 时为编辑模式。
 * 传入 [EXTRA_GROUP_ID] 时新建的记录将归属于该分组。
 */
class AddEditActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "entry_id"
        const val EXTRA_GROUP_ID = "group_id"
    }

    private lateinit var binding: ActivityAddEditBinding
    private var editingId: String? = null
    private var groupId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = getString(R.string.title_edit)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        editingId = intent.getStringExtra(EXTRA_ID)
        groupId = intent.getStringExtra(EXTRA_GROUP_ID)
        if (editingId != null) loadEntry(editingId!!) else binding.btnDelete.visibility = View.GONE

        // 复制用户名
        binding.tilUsername.setEndIconOnClickListener {
            val u = binding.etUsername.text?.toString().orEmpty()
            if (u.isNotEmpty()) ClipboardHelper.copy(this, "用户名", u)
        }

        binding.btnGenerate.setOnClickListener {
            binding.etPassword.setText(PasswordGenerator.generate())
        }

        binding.btnSave.setOnClickListener { save() }
        binding.btnDelete.setOnClickListener { confirmDelete() }
    }

    private fun loadEntry(id: String) {
        val entry = CryptoVault.entries.firstOrNull { it.id == id } ?: run {
            finish(); return
        }
        groupId = entry.groupId
        binding.etTitle.setText(entry.title)
        binding.etUsername.setText(entry.username)
        binding.etPassword.setText(entry.password)
        binding.etUrl.setText(entry.url)
        binding.etNotes.setText(entry.notes)
    }

    private fun save() {
        val title = binding.etTitle.text?.toString()?.trim().orEmpty()
        if (title.isEmpty()) {
            binding.tilTitle.error = getString(R.string.err_title_required)
            return
        }
        binding.tilTitle.error = null

        val entry = Entry(
            id = editingId ?: UUID.randomUUID().toString(),
            title = title,
            username = binding.etUsername.text?.toString().orEmpty(),
            password = binding.etPassword.text?.toString().orEmpty(),
            url = binding.etUrl.text?.toString().orEmpty(),
            notes = binding.etNotes.text?.toString().orEmpty(),
            groupId = groupId,
            updatedAt = System.currentTimeMillis()
        )
        CryptoVault.upsert(entry)
        CryptoVault.save(this)
        finish()
    }

    private fun confirmDelete() {
        val id = editingId ?: return
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete)
            .setMessage(getString(R.string.confirm_delete_entry))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                CryptoVault.deleteEntry(id)
                CryptoVault.save(this)
                finish()
            }
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
