package com.example.snplc.di

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.snplc.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

/**
 *  With dependency injection, we can create our variables and objects
 *  in an app at a central place
 *  and inject them into classes where we need them.
 *
 *  dependency examples -> glide, applicationContext
 *  fragment B requires glide
 *  glide --(Dagger-hilt-DI)--> fragment B
 *
 * @Singleton :
 * @Scope :
 */


/**
 * @AppModule : lives as long as our app
 *
 * @InstallIn(ApplicationComponent::class)
 * The dependencies in ApplicationComponent module will live as long as our application does
 */
@Module
@InstallIn(ApplicationComponent::class) // lives as long as our application
object AppModule {
    /**
     * contains functions
     * that instructs how we gonna
     * create our dependencies
     *
     * @WeCreate : actual objects
     */

    @Singleton // ensures only one instance exist
    @Provides // this module provide the dependency, application context
    fun provideApplicationContext(
        @ApplicationContext context: Context
    ) = context

    @Singleton
    @Provides
    fun provideGlideInstance(
        @ApplicationContext context: Context // glide needs context
    ) = Glide.with(context).setDefaultRequestOptions(
        RequestOptions()
            .placeholder(R.drawable.ic_image)
            .error(R.drawable.ic_error)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
    )

    @Singleton
    @Provides
    fun providesMainDispatcher() = Dispatchers.Main  as CoroutineDispatcher
    // CoroutineDispatcher is parent class of MainCoroutineDispatcher


}