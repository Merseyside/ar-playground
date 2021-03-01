package com.merseyside.ar.sample

import com.merseyside.archy.BaseApplication
import com.merseyside.core.di.CoreComponent
import com.merseyside.core.di.DaggerCoreComponent
import com.merseyside.core.di.modules.ContextModule

class App : BaseApplication() {

    lateinit var coreComponent: CoreComponent

    companion object {
        private lateinit var application : App

        fun getInstance(): App = application

        fun getCoreCompomponent(): CoreComponent
            = getInstance().coreComponent
    }

    override fun onCreate() {
        super.onCreate()
        application = this

        initCoreDependencyInjection()
        //initAppDependencyInjection()
    }

//    private fun initAppDependencyInjection() {
//        DaggerAppComponent
//            .builder()
//            .coreComponent(coreComponent)
//            .build().inject(this)
//    }

    private fun initCoreDependencyInjection() {
        coreComponent = DaggerCoreComponent
            .builder()
            .contextModule(ContextModule(this))
            .build()
    }
}