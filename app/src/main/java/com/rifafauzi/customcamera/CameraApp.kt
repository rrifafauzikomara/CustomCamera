package com.rifafauzi.customcamera

import android.app.Application
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig

/**
 * Created by rrifafauzikomara on 2/26/20.
 */

class CameraApp : Application(), CameraXConfig.Provider {
    override fun getCameraXConfig() = Camera2Config.defaultConfig()
}