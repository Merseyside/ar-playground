package com.merseyside.ar.sample.di

import android.content.Context
import com.merseyside.ar.sample.App
import dagger.Module
import dagger.Provides

@Module
class AppModule {

    @Provides
    fun provideContext(application: App): Context = application.applicationContext
}