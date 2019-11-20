package com.rifafauzi.customcamerasurfaceview

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.StrictMode
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        navController = Navigation.findNavController(this, R.id.mainContent)

        if (isCameraPermissionGranted()) {
            displayCameraFragment()
        } else {
            requestCameraPermission()
        }

//        requestReadPermissions()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            when {
                grantResults.contains(PackageManager.PERMISSION_GRANTED) -> displayCameraFragment()
                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> requestCameraPermission()
                else -> displayErrorMessage()
            }
        }
    }

    private fun isCameraPermissionGranted(): Boolean {
        val permission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        return permission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        requestPermissions(arrayOf(Manifest.permission.CAMERA),
            CAMERA_REQUEST_CODE
        )
    }

//    private fun requestReadPermissions() {
//        Dexter.withActivity(this)
//            .withPermissions(Manifest.permission.CAMERA)
//            .withListener(object : MultiplePermissionsListener {
//                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
//                    if (report.areAllPermissionsGranted()) {
//                        displayCamera()
//                    }
//
//                    // check for permanent denial of any permission
//                    if (report.isAnyPermissionPermanentlyDenied) {
//                        // show alert dialog navigating to Settings
//                        displayErrorMessage()
//                    }
//                }
//
//                override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
//                    token.continuePermissionRequest()
//                }
//
//            }).withErrorListener { Toast.makeText(applicationContext, "Some Error! ", Toast.LENGTH_SHORT).show() }
//            .onSameThread()
//            .check()
//    }

    private fun displayCameraFragment() {
        navController.navigate(R.id.cameraFragment)
    }

    private fun displayErrorMessage() {
        Snackbar.make(
            root_layout,
            "The camera permission must be granted in order to use this app",
            Snackbar.LENGTH_INDEFINITE
        ).setAction("Retry") { requestCameraPermission() }
            .show()
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 20
    }

}