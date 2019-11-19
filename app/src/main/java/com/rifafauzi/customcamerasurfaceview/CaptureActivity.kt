package com.rifafauzi.customcamerasurfaceview

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import java.io.IOException

class CaptureActivity : AppCompatActivity() {

    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null
    private var mPicture: Camera.PictureCallback? = null
    private var capture: Button? = null
    private var myContext: Context? = null
    private var cameraPreview: LinearLayout? = null

    private val pictureCallback: Camera.PictureCallback
        get() = Camera.PictureCallback { data, _ ->
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            val intent = Intent(this@CaptureActivity, PictureActivity::class.java)
            startActivity(intent)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)

        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        myContext = this

        initCamera()

    }

    private fun initCamera(){

        cameraPreview = findViewById(R.id.cPreview)
        mPreview = CameraPreview(myContext as CaptureActivity, mCamera)
        cameraPreview!!.addView(mPreview)

        mCamera = Camera.open()
        mCamera!!.setDisplayOrientation(90)

        try {
            mPicture = pictureCallback
            mPreview!!.refreshCamera(mCamera)
            mCamera!!.startPreview()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        capture = findViewById(R.id.btnCam)
        capture!!.setOnClickListener { mCamera!!.takePicture(null, null, mPicture) }

    }

    public override fun onResume() {

        super.onResume()
        if (mCamera == null) {
            mCamera = Camera.open()
            mCamera!!.setDisplayOrientation(90)
            mPicture = pictureCallback
            mPreview!!.refreshCamera(mCamera)
            Log.d("noooo", "null")
        } else {
            Log.d("noooo", "no null")
        }

    }

    override fun onPause() {
        super.onPause()
        //when on Pause, release camera in order to be used from other applications
        releaseCamera()
    }

    private fun releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera!!.stopPreview()
            mCamera!!.setPreviewCallback(null)
            mCamera!!.release()
            mCamera = null
        }
    }

    companion object {
        lateinit var bitmap: Bitmap
    }

}