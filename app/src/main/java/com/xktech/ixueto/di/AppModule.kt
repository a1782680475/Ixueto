package com.xktech.ixueto.di

import android.content.Context
import com.xktech.ixueto.repository.RulePreferencesRepository
import com.xktech.ixueto.repository.SettingPreferencesRepository
import com.xktech.ixueto.repository.UserInfoPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideRulePreferencesRepository(@ApplicationContext app: Context): RulePreferencesRepository =
        RulePreferencesRepository(app)

    @Provides
    @Singleton
    fun provideUserInfoPreferencesRepository(@ApplicationContext app: Context): UserInfoPreferencesRepository =
        UserInfoPreferencesRepository(app)

    @Provides
    @Singleton
    fun provideSettingPreferencesRepository(@ApplicationContext app: Context): SettingPreferencesRepository =
        SettingPreferencesRepository(app)
}