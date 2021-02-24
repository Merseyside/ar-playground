package com.merseyside.template.news.di

import com.merseyside.core.di.CoreComponent
import com.merseyside.template.news.application

interface HasCoreComponent {

    val coreComponent: CoreComponent
        get() {
            return application.coreComponent
        }
}