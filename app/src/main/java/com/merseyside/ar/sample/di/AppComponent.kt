package com.merseyside.ar.sample.di

import com.merseyside.core.di.CoreComponent
import com.merseyside.ar.sample.App
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    dependencies = [CoreComponent::class],
    modules = [AppModule::class])
interface AppComponent {

    fun inject(): App
}