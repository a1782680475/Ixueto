package com.xktech.ixueto.di

import android.content.Context
import androidx.room.Room
import com.xktech.ixueto.data.local.StudyDatabase
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
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): StudyDatabase {
        return Room.databaseBuilder(
            context,
            StudyDatabase::class.java,
            "ixueto"
        ).build()
    }
}