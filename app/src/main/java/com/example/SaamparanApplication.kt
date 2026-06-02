package com.example

import android.app.Application
import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.work.VideoCleanupWorker
import java.util.concurrent.TimeUnit

class SaamparanApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        scheduleWeeklyCleanup()
    }

    private fun scheduleWeeklyCleanup() {
        try {
            // Enqueue background task running once every 7 days
            val cleanupRequest = PeriodicWorkRequestBuilder<VideoCleanupWorker>(
                7, TimeUnit.DAYS
            ).build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "SaamparanWeeklyCleanup",
                ExistingPeriodicWorkPolicy.KEEP,
                cleanupRequest
            )
        } catch (e: Exception) {
            android.util.Log.e("SaamparanApplication", "WorkManager scheduling failed: ${e.message}", e)
        }
    }

    companion object {
        private var instance: SaamparanApplication? = null

        fun getAppContext(): Context {
            val inst = instance
            if (inst != null) {
                return inst.applicationContext
            }
            // Fallback for Robolectric/testing environments when Application life cycle is mocked/altered
            return try {
                val clazz = Class.forName("androidx.test.core.app.ApplicationProvider")
                val method = clazz.getMethod("getApplicationContext")
                method.invoke(null) as Context
            } catch (e: Exception) {
                // If everything else fails, we can't do much, but let's throw a clean exception with context
                throw IllegalStateException("SaamparanApplication.instance is null and no testing provider was found.", e)
            }
        }
    }
}
