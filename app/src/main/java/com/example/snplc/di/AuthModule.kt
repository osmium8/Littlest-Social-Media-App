package com.example.snplc.di

import com.example.snplc.repositories.AuthRepository
import com.example.snplc.repositories.DefaultAuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

/**
 * @InstallIn(ActivityComponent::class)
 * lives as long as the authentication
 *
 * dependencies will be binded to the lifetime of an activity
 */

@Module
@InstallIn(ActivityComponent::class)
object AuthModule {

    @ActivityScoped
    @Provides
    fun providesAuthRepository() = DefaultAuthRepository() as AuthRepository // AuthViewModel requries
    // AuthRepository so cast DefaultAuthRepository
}