package com.dma.finance

import android.app.Application
import com.dma.finance.data.sync.SyncCoordinator
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class FinanceApp : Application() {

    @Inject
    lateinit var syncCoordinator: SyncCoordinator

    override fun onCreate() {
        super.onCreate()
        syncCoordinator.start()
    }
}
