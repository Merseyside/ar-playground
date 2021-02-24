package com.merseyside.template.news.di

import android.content.Context
import com.merseyside.template.news.App
import dagger.Module
import dagger.Provides

@Module
class AppModule {

    @Provides
    fun provideContext(application: App): Context = application.applicationContext
}