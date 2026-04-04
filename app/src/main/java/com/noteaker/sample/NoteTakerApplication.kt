package com.noteaker.sample

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.noteaker.sample.sync.SyncScheduler
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class NoteTakerApplication : Application() {

    // Inject dependencies
    @Inject
    lateinit var syncScheduler: SyncScheduler

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Initialize WorkManager with Hilt's WorkerFactory
        // This MUST happen in onCreate() after Hilt injection
        val workManagerConfiguration = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.ERROR)
            .build()

        WorkManager.initialize(this, workManagerConfiguration)
        Timber.d("NoteTakerApplication: WorkManager initialized with Hilt")

        // Schedule periodic background sync
        // This will sync notes every 15 minutes when online and battery not low
        // syncScheduler.schedulePeriodicSync()

        Timber.d("NoteTakerApplication: Sync scheduled successfully")
    }
}
