package com.merseyside.template.news.di

import com.merseyside.core.di.CoreComponent
import com.merseyside.template.news.App
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    dependencies = [CoreComponent::class],
    modules = [AppModule::class])
interface AppComponent {

    fun inject(): App
}