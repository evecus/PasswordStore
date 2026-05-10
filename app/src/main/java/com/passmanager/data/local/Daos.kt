package com.passmanager.data.local

import androidx.room.*
import com.passmanager.data.model.Group
import com.passmanager.data.model.PasswordEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Query("SELECT * FROM groups ORDER BY name ASC")
    fun getAllGroups(): Flow<List<Group>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: Group): Long

    @Update
    suspend fun updateGroup(group: Group)

    @Delete
    suspend fun deleteGroup(group: Group)

    @Query("SELECT * FROM groups WHERE id = :id")
    suspend fun getGroupById(id: Long): Group?
}

@Dao
interface PasswordDao {
    @Query("SELECT * FROM passwords ORDER BY updatedAt DESC")
    fun getAllPasswords(): Flow<List<PasswordEntry>>

    @Query("SELECT * FROM passwords WHERE groupId = :groupId ORDER BY updatedAt DESC")
    fun getPasswordsByGroup(groupId: Long): Flow<List<PasswordEntry>>

    @Query("SELECT * FROM passwords WHERE groupId IS NULL ORDER BY updatedAt DESC")
    fun getUngroupedPasswords(): Flow<List<PasswordEntry>>

    @Query("SELECT * FROM passwords WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoritePasswords(): Flow<List<PasswordEntry>>

    @Query("""
        SELECT * FROM passwords 
        WHERE name LIKE '%' || :query || '%' 
        OR siteName LIKE '%' || :query || '%' 
        OR account LIKE '%' || :query || '%'
        ORDER BY updatedAt DESC
    """)
    fun searchPasswords(query: String): Flow<List<PasswordEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassword(entry: PasswordEntry): Long

    @Update
    suspend fun updatePassword(entry: PasswordEntry)

    @Delete
    suspend fun deletePassword(entry: PasswordEntry)

    @Query("SELECT * FROM passwords WHERE id = :id")
    suspend fun getPasswordById(id: Long): PasswordEntry?

    @Query("SELECT COUNT(*) FROM passwords WHERE groupId = :groupId")
    fun getPasswordCountByGroup(groupId: Long): Flow<Int>
}
