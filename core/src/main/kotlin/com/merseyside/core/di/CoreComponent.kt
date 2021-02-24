package com.merseyside.core.di

import android.content.Context
import com.merseyside.core.di.modules.ContextModule
import com.merseyside.core.di.modules.DatabaseModule
import com.merseyside.core.di.modules.NetworkModule
import com.merseyside.core.di.modules.UtilsModule
import dagger.Component
import javax.inject.Singleton

/**
 * Core component that all module's components depend on.
 *
 * @see Component
 */
@Singleton
@Component(modules = [
    ContextModule::class,
    NetworkModule::class,
    DatabaseModule::class,
    UtilsModule::class])
interface CoreComponent {

    /**
     * Provide dependency graph Context
     *
     * @return Context
     */
    fun context(): Context
}
