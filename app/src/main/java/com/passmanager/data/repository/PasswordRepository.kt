package com.passmanager.data.repository

import com.passmanager.data.local.GroupDao
import com.passmanager.data.local.PasswordDao
import com.passmanager.data.model.Group
import com.passmanager.data.model.PasswordEntry
import com.passmanager.utils.CryptoManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PasswordRepository @Inject constructor(
    private val passwordDao: PasswordDao,
    private val groupDao: GroupDao,
    private val cryptoManager: CryptoManager
) {
    fun getAllPasswords(): Flow<List<PasswordEntry>> = passwordDao.getAllPasswords()
    fun getPasswordsByGroup(groupId: Long): Flow<List<PasswordEntry>> = passwordDao.getPasswordsByGroup(groupId)
    fun getUngroupedPasswords(): Flow<List<PasswordEntry>> = passwordDao.getUngroupedPasswords()
    fun getFavoritePasswords(): Flow<List<PasswordEntry>> = passwordDao.getFavoritePasswords()
    fun searchPasswords(query: String): Flow<List<PasswordEntry>> = passwordDao.searchPasswords(query)
    fun getAllGroups(): Flow<List<Group>> = groupDao.getAllGroups()
    fun getPasswordCountByGroup(groupId: Long): Flow<Int> = passwordDao.getPasswordCountByGroup(groupId)

    suspend fun getPasswordById(id: Long): PasswordEntry? = passwordDao.getPasswordById(id)

    suspend fun savePassword(entry: PasswordEntry): Long {
        val encrypted = entry.copy(
            encryptedPassword = if (entry.encryptedPassword.isNotEmpty())
                cryptoManager.encrypt(entry.encryptedPassword) else "",
            updatedAt = System.currentTimeMillis()
        )
        return if (entry.id == 0L) passwordDao.insertPassword(encrypted)
        else { passwordDao.updatePassword(encrypted); entry.id }
    }

    fun decryptPassword(encryptedPassword: String): String =
        if (encryptedPassword.isEmpty()) "" else cryptoManager.decrypt(encryptedPassword)

    suspend fun deletePassword(entry: PasswordEntry) = passwordDao.deletePassword(entry)
    suspend fun toggleFavorite(entry: PasswordEntry) =
        passwordDao.updatePassword(entry.copy(isFavorite = !entry.isFavorite, updatedAt = System.currentTimeMillis()))

    suspend fun saveGroup(group: Group): Long =
        if (group.id == 0L) groupDao.insertGroup(group)
        else { groupDao.updateGroup(group); group.id }

    suspend fun deleteGroup(group: Group) = groupDao.deleteGroup(group)
    suspend fun getGroupById(id: Long): Group? = groupDao.getGroupById(id)
}
