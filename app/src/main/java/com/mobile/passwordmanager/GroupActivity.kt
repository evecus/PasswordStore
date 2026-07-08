package com.mobile.passwordmanager

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobile.passwordmanager.databinding.ActivityGroupBinding

/**
 * 查看某个分组内的密码列表。FAB 新建的密码自动归属此分组。
 */
class GroupActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_GROUP_ID = "group_id"
    }

    private lateinit var binding: ActivityGroupBinding
    private var groupId: String? = null

    private val adapter = EntriesAdapter(
        onClick = { editEntry(it) },
        onCopyPassword = { ClipboardHelper.copy(this, "密码", it.password) },
        onDelete = { confirmDelete(it) }
    )

    private val editLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { refresh() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        groupId = intent.getStringExtra(EXTRA_GROUP_ID)
        if (groupId == null) { finish(); return }

        val group = CryptoVault.groups.firstOrNull { it.id == groupId }
        title = group?.name ?: getString(R.string.title_group)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, AddEditActivity::class.java).apply {
                putExtra(AddEditActivity.EXTRA_GROUP_ID, groupId)
            }
            editLauncher.launch(intent)
        }

        refresh()
    }

    override fun onResume() {
        super.onResume()
        if (!CryptoVault.unlocked) {
            startActivity(Intent(this, UnlockActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            finish()
            return
        }
        refresh()
    }

    private fun refresh() {
        val gid = groupId ?: return
        val sorted = CryptoVault.entriesInGroup(gid).sortedBy { it.title.lowercase() }
        adapter.submitList(sorted)
        binding.emptyLayout.visibility = if (sorted.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun editEntry(entry: Entry) {
        val intent = Intent(this, AddEditActivity::class.java).apply {
            putExtra(AddEditActivity.EXTRA_ID, entry.id)
        }
        editLauncher.launch(intent)
    }

    private fun confirmDelete(entry: Entry) {
        androidx.appcompat.app.AlertDialog.Builder(this)
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

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
