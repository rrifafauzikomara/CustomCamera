package com.rifafauzi.customcamerasurfaceview.utils

/**
 * Created by rrifafauzikomara on 2019-11-20.
 */

object FileCreator {

    const val JPEG_FORMAT = ".jpg"

    fun createTempFile(fileFormat: String) =
        createTempFile(System.currentTimeMillis().toString(), fileFormat)

}