package com.mikrochek.plugins

import com.mikrochek.di.appModule
import org.koin.core.context.startKoin

fun configurePlugins(){
    startKoin {
        modules(appModule)
    }
}