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

class CaptureActivity : AppCompatActivity() {

    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null
    private var mPicture: Camera.PictureCallback? = null
    private var capture: Button? = null
    private var switchCamera: Button? = null
    private var myContext: Context? = null
    private var cameraPreview: LinearLayout? = null
    private var cameraFront = false

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

        initCamere()

    }

    private fun initCamere(){

        mCamera = Camera.open()
        mCamera!!.setDisplayOrientation(90)
        cameraPreview = findViewById(R.id.cPreview)
        mPreview = CameraPreview(myContext as CaptureActivity, mCamera)
        cameraPreview!!.addView(mPreview)

        capture = findViewById(R.id.btnCam)
        capture!!.setOnClickListener { mCamera!!.takePicture(null, null, mPicture) }

        switchCamera = findViewById(R.id.btnSwitch)
        switchCamera!!.setOnClickListener {
            //get the number of cameras
            val camerasNumber = Camera.getNumberOfCameras()
            if (camerasNumber > 1) {
                //release the old camera instance
                //switch camera, from the front and the back and vice versa

                releaseCamera()
                chooseCamera()
            } else {

            }
        }

        mCamera!!.startPreview()

    }

    private fun findFrontFacingCamera(): Int {

        var cameraId = -1
        // Search for the front facing camera
        val numberOfCameras = Camera.getNumberOfCameras()
        for (i in 0 until numberOfCameras) {
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(i, info)
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i
                cameraFront = true
                break
            }
        }
        return cameraId

    }

    private fun findBackFacingCamera(): Int {
        var cameraId = -1
        //Search for the back facing camera
        //get the number of cameras
        val numberOfCameras = Camera.getNumberOfCameras()
        //for every camera check
        for (i in 0 until numberOfCameras) {
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(i, info)
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i
                cameraFront = false
                break

            }

        }
        return cameraId
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

    fun chooseCamera() {
        //if the camera preview is the front
        if (cameraFront) {
            val cameraId = findBackFacingCamera()
            if (cameraId >= 0) {
                //open the backFacingCamera
                //set a picture callback
                //refresh the preview

                mCamera = Camera.open(cameraId)
                mCamera!!.setDisplayOrientation(90)
                mPicture = pictureCallback
                mPreview!!.refreshCamera(mCamera)
            }
        } else {
            val cameraId = findFrontFacingCamera()
            if (cameraId >= 0) {
                //open the backFacingCamera
                //set a picture callback
                //refresh the preview
                mCamera = Camera.open(cameraId)
                mCamera!!.setDisplayOrientation(90)
                mPicture = pictureCallback
                mPreview!!.refreshCamera(mCamera)
            }
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