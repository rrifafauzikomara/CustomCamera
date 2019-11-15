package com.rifafauzi.customcamerasurfaceview

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class MainActivity : AppCompatActivity() {

    private val imageView by lazy { findViewById<ImageView>(R.id.text_recognition_image_view) }
    private val btnCamera by lazy { findViewById<Button>(R.id.btnCamera) }
    private val btnProcess by lazy { findViewById<Button>(R.id.btnRecognition) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bitmap = CameraActivity.bitmap
        val rotatedBitmap = bitmap?.rotate(90)
        Glide.with(applicationContext)
            .load(rotatedBitmap)
            .into(imageView)
        btnCamera.setOnClickListener {
            requestReadPermissions()
        }

        btnProcess.setOnClickListener {

        }
    }

    private fun Bitmap.rotate(degree:Int):Bitmap{
        // Initialize a new matrix
        val matrix = Matrix()

        // Rotate the bitmap
        matrix.postRotate(degree.toFloat())

        // Resize the bitmap
        val scaledBitmap = Bitmap.createScaledBitmap(
            this,
            width,
            height,
            true
        )

        // Create and return the rotated bitmap
        return Bitmap.createBitmap(
            scaledBitmap,
            0,
            0,
            scaledBitmap.width,
            scaledBitmap.height,
            matrix,
            true
        )
    }

    private fun requestReadPermissions() {

        Dexter.withActivity(this)
            .withPermissions( Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    // check if all permissions are granted
                    if (report.areAllPermissionsGranted()) {
                        startActivity(Intent(this@MainActivity, CameraActivity::class.java))
                        finish()
                    }

                    // check for permanent denial of any permission
                    if (report.isAnyPermissionPermanentlyDenied) {
                        // show alert dialog navigating to Settings
                        Toast.makeText(applicationContext, "All permissions are denied by user!", Toast.LENGTH_SHORT)
                            .show()
                        //openSettingsDialog();
                    }
                }

                override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener { Toast.makeText(applicationContext, "Some Error! ", Toast.LENGTH_SHORT).show() }
            .onSameThread()
            .check()
    }

}