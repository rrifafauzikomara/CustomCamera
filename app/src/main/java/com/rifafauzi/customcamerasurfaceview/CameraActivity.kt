package com.rifafauzi.customcamerasurfaceview

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Camera
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.SparseIntArray
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import java.util.*

class CameraActivity : AppCompatActivity(), SurfaceHolder.Callback, Camera.PictureCallback {

    private val orientations = SparseIntArray()

    init {
        orientations.append(0, 90)
        orientations.append(90, 0)
        orientations.append(180, 270)
        orientations.append(270, 180)
    }

    private var surfaceHolder: SurfaceHolder? = null
    private var camera: Camera? = null
    private var surfaceView: SurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        surfaceView = findViewById(R.id.surfaceView)
        setupSurfaceHolder()
    }

    private fun setViewVisibility(id: Int, visibility: Int) {
        val view = findViewById<View>(id)
        view!!.visibility = visibility
    }

    private fun setupSurfaceHolder() {
        setViewVisibility(R.id.linear, View.VISIBLE)
        setViewVisibility(R.id.surfaceView, View.VISIBLE)
        surfaceHolder = surfaceView!!.holder
        surfaceHolder!!.addCallback(this)
        setBtnClick()
    }

    private fun setBtnClick() {
        val startBtn = findViewById<Button>(R.id.startBtn)
        val backBtn = findViewById<Button>(R.id.btnBack)
        startBtn?.setOnClickListener { captureImage() }
        backBtn?.setOnClickListener {  }
    }

    private fun captureImage() {
        if (camera != null) {
            camera!!.takePicture(null, null, this)
        }
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        startCamera()
    }

    private fun startCamera() {
        camera = Camera.open()
        camera!!.setDisplayOrientation(90)
        try {
            camera!!.setPreviewDisplay(surfaceHolder)
            camera!!.startPreview()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
        resetCamera()
    }

    private fun resetCamera() {
        if (surfaceHolder!!.surface == null) {
            // Return if preview surface does not exist
            return
        }

        // Stop if preview surface is already running.
        camera!!.stopPreview()
        try {
            // Set preview display
            camera!!.setPreviewDisplay(surfaceHolder)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Start the camera preview...
        camera!!.startPreview()
    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        releaseCamera()
    }

    private fun releaseCamera() {
        camera!!.stopPreview()
        camera!!.release()
        camera = null
    }

    override fun onPictureTaken(bytes: ByteArray, camera: Camera) {
        saveImage(bytes)
/*        bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        saveImage(bitmap)*/
        startActivity(Intent(this@CameraActivity, MainActivity::class.java))
        resetCamera()
    }

    // save as bitmap
//    private fun saveImage(myBitmap: Bitmap?) {
//        val outStream: FileOutputStream
//        val bytes = ByteArrayOutputStream()
//        myBitmap?.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
//
//        val wallpaperDirectory = File(
//            Environment.getExternalStorageDirectory().toString() + IMAGE_DIRECTORY
//        )
//
//        if (!wallpaperDirectory.exists()) {
//            wallpaperDirectory.mkdirs()
//        }
//
//        try {
//            val file = File(wallpaperDirectory, Calendar.getInstance()
//                .timeInMillis.toString() + ".jpg")
//            file.createNewFile()
//
//            outStream = FileOutputStream(file)
//            outStream.write(bytes.toByteArray())
//            MediaScannerConnection.scanFile(
//                this,
//                arrayOf(file.path),
//                arrayOf("image/jpeg"), null
//            )

//            outStream.close()
//        } catch (e: FileNotFoundException) {
//            e.printStackTrace()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//
//    }

    // save as byte array
    private fun saveImage(bytes: ByteArray) {
        val outStream: FileOutputStream

        val wallpaperDirectory = File(
            Environment.getExternalStorageDirectory().toString() + IMAGE_DIRECTORY
        )
        // have the object build the directory structure, if needed.

        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs()
        }

        val cameraInfo = Camera.CameraInfo()
        val cameraOrientation = cameraInfo.orientation

        val captureOrientation = orientations.get(cameraOrientation)
        Log.i("camera-tag","Capture orientation: $captureOrientation")

        Log.i("camera-tag","Start loading byte array to bitmap.")
        val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        Log.i("camera-tag","Done loading byte array to bitmap.")


        try {
            val file = File(wallpaperDirectory, Calendar.getInstance()
                .timeInMillis.toString() + ".jpg")
            file.createNewFile()


            //we create finalBitmap, from image bitmap
            Log.i("camera-tag","Start rotating bitmap if needed.")
            val finalBitmap = if (captureOrientation != 0){
                val w = image.width
                val h = image.height

                val matrix = Matrix()
                matrix.postRotate(captureOrientation.toFloat())

                Bitmap.createBitmap(image, 0, 0, w, h, matrix, false)
            }
            else image
            Log.i("camera-tag","Done rotating bitmap.")

            //now saving it to file
            Log.i("camera-tag","Start saving bitmap to a file.")
            outStream = FileOutputStream(file)
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
            Log.i("camera-tag","Done saving bitmap to a file.")

            MediaScannerConnection.scanFile(
                this,
                arrayOf(file.path),
                arrayOf("image/jpeg"), null
            )
            outStream.close()

            bitmap = image

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val IMAGE_DIRECTORY = "/CustomCamera"
        var bitmap: Bitmap? = null
    }
}