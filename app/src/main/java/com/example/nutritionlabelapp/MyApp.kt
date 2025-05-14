// File: app/src/main/java/com/example/nutritionlabelapp/MyApp.kt
package com.example.nutritionlabelapp

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MyApp : Application(), CameraXConfig.Provider {
    // public so fragments can shut it down
    val cameraXExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    override fun onCreate() {
        super.onCreate()
        // nothing else needed here
    }

    override fun getCameraXConfig(): CameraXConfig =
        CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig())
            .setCameraExecutor(cameraXExecutor)
            .setSchedulerHandler(Handler(Looper.getMainLooper()))
            .build()
}
