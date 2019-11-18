package com.rifafauzi.customcamerasurfaceview

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class MainActivity : AppCompatActivity() {

    private val imageView by lazy { findViewById<ImageView>(R.id.text_recognition_image_view) }
    private val btnCamera by lazy { findViewById<Button>(R.id.btnCamera) }
    private val btnProcess by lazy { findViewById<FrameLayout>(R.id.btnRecognition) }
    private val recyclerView by lazy { findViewById<RecyclerView>(R.id.recycler_view) }
    private val textRecognitionModels = ArrayList<TextModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)

        Log.i("camera-tag","Getting bitmap from main activity.")
        val bitmap = CameraActivity.bitmap
        val rotatedBitmap = bitmap?.rotate(90)
        Glide.with(applicationContext)
            .load(rotatedBitmap)
            .into(imageView)
        Log.i("camera-tag","Glide finished loading.")
        btnCamera.setOnClickListener {
            requestReadPermissions()
        }

        btnProcess.setOnClickListener {

        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = TextAdapter(this, textRecognitionModels)
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

    private fun analyzeImage(image: Bitmap?) {
        if (image == null) {
            Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show()
            return
        }

        imageView.setImageBitmap(null)
        textRecognitionModels.clear()
        recyclerView.adapter?.notifyDataSetChanged()
        showProgress()

        val firebaseVisionImage = FirebaseVisionImage.fromBitmap(image)
        val textRecognizer = FirebaseVision.getInstance().onDeviceTextRecognizer
        textRecognizer.processImage(firebaseVisionImage)
            .addOnSuccessListener {
                val mutableImage = image.copy(Bitmap.Config.ARGB_8888, true)

                recognizeText(it, mutableImage)

                imageView.setImageBitmap(mutableImage)
                hideProgress()
                recyclerView.adapter?.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show()
                hideProgress()
            }
    }


    private fun recognizeText(result: FirebaseVisionText?, image: Bitmap?) {

        if (result == null || image == null) {
            return Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show()
        } else if (result.textBlocks.size == 0) {
            Toast.makeText(this,"Gunakan landscape mode agar teks bisa terbaca", Toast.LENGTH_SHORT).show()
            return
        }

//        val canvas = Canvas(image)
//        val rectPaint = Paint()
//        rectPaint.color = Color.RED
//        rectPaint.style = Paint.Style.STROKE
//        rectPaint.strokeWidth = 4F
//        val textPaint = Paint()
//        textPaint.color = Color.RED
//        textPaint.textSize = 40F

        var index = 0
        for (block in result.textBlocks) {
            for (line in block.lines) {
//                canvas.drawRect(line.boundingBox!!, rectPaint)
//                canvas.drawText(index.toString(), line.cornerPoints!![2].x.toFloat(), line.cornerPoints!![2].y.toFloat(), textPaint)
                textRecognitionModels.add(TextModel(index++, line.text))
            }
        }
    }

    private fun showProgress() {
        findViewById<View>(R.id.btnText).visibility = View.GONE
        findViewById<View>(R.id.btnProgress).visibility = View.VISIBLE
    }

    private fun hideProgress() {
        findViewById<View>(R.id.btnText).visibility = View.VISIBLE
        findViewById<View>(R.id.btnProgress).visibility = View.GONE
    }

}