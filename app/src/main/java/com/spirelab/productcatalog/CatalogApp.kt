package com.spirelab.productcatalog

import android.app.Application
import com.spirelab.productcatalog.di.AppContainer

class CatalogApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
