package com.axoloth.captaggenerator

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        
        // Staggered Firebase initialization to reduce startup CPU spike
        applicationScope.launch {
            // 1. Initialized Firebase App (Essential)
            FirebaseApp.initializeApp(this@MainApplication)
            
            // 2. Immediate Crashlytics (Safety first)
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
            
            delay(3000) // Give breath for UI and DB loading
            
            // 3. Initialized Performance Monitoring (Non-critical)
            FirebasePerformance.getInstance().isPerformanceCollectionEnabled = true
            
            delay(5000) // Further delay analytics to avoid background noise during first user interaction
            
            // 4. Analytics and Sessions (Delayed)
            // Note: Analytics is usually auto-initialized, but by delaying other services 
            // and keeping CPU free, we improve the overall perception of speed.
            com.google.firebase.analytics.FirebaseAnalytics.getInstance(this@MainApplication)
                .setAnalyticsCollectionEnabled(true)
        }
    }
}
