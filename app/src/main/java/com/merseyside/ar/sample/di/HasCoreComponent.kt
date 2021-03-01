package com.merseyside.ar.sample.di

import com.merseyside.core.di.CoreComponent
import com.merseyside.ar.sample.application

interface HasCoreComponent {

    val coreComponent: CoreComponent
        get() {
            return application.coreComponent
        }
}