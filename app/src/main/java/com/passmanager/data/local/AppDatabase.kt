package com.passmanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.passmanager.data.model.Group
import com.passmanager.data.model.PasswordEntry

@Database(
    entities = [Group::class, PasswordEntry::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun groupDao(): GroupDao
    abstract fun passwordDao(): PasswordDao
}
