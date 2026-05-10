package com.passmanager.di

import android.content.Context
import androidx.room.Room
import com.passmanager.data.local.AppDatabase
import com.passmanager.data.local.GroupDao
import com.passmanager.data.local.PasswordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "passmanager.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun providePasswordDao(db: AppDatabase): PasswordDao = db.passwordDao()

    @Provides
    fun provideGroupDao(db: AppDatabase): GroupDao = db.groupDao()
}
