package com.rifafauzi.customcamerasurfaceview

import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.rifafauzi.customcamerasurfaceview.adapter.TextAdapter
import com.rifafauzi.customcamerasurfaceview.model.TextModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class PictureActivity : AppCompatActivity() {

    private var imageView: ImageView? = null
    private val btnRecognition by lazy { findViewById<FrameLayout>(R.id.btnRecognition) }
    private val recyclerView by lazy { findViewById<RecyclerView>(R.id.recycler_view) }
    private val textRecognitionModels = ArrayList<TextModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture)
        FirebaseApp.initializeApp(this)

        imageView = findViewById(R.id.img)

        val bitmap = CaptureActivity.bitmap
        val rotatedBitmap = bitmap.rotate(90)

        imageView!!.setImageBitmap(rotatedBitmap)
        saveImage(rotatedBitmap)

        btnRecognition.setOnClickListener {
            analyzeImage(rotatedBitmap)
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

    private fun saveImage(myBitmap: Bitmap): String {
        val bytes = ByteArrayOutputStream()
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
        val wallpaperDirectory = File(
            Environment.getExternalStorageDirectory().toString() + IMAGE_DIRECTORY
        )
        // have the object build the directory structure, if needed.

        if (!wallpaperDirectory.exists()) {
            Log.d("dirrrrrr", "" + wallpaperDirectory.mkdirs())
            wallpaperDirectory.mkdirs()
        }

        try {
            val f = File(
                wallpaperDirectory, Calendar.getInstance()
                    .timeInMillis.toString() + ".jpg"
            )
            f.createNewFile()   //give read write permission
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(
                this,
                arrayOf(f.path),
                arrayOf("image/jpeg"), null
            )
            fo.close()
            Log.d("TAG", "File Saved::--->" + f.absolutePath)

            return f.absolutePath
        } catch (e1: IOException) {
            e1.printStackTrace()
        }

        return ""

    }

    private fun analyzeImage(image: Bitmap?) {
        if (image == null) {
            Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show()
            return
        }

        textRecognitionModels.clear()
        recyclerView.adapter?.notifyDataSetChanged()
        showProgress()

        val firebaseVisionImage = FirebaseVisionImage.fromBitmap(image)
        val textRecognizer = FirebaseVision.getInstance().onDeviceTextRecognizer
        textRecognizer.processImage(firebaseVisionImage)
            .addOnSuccessListener {
                val mutableImage = image.copy(Bitmap.Config.ARGB_8888, true)

                recognizeText(it, mutableImage)

                imageView!!.setImageBitmap(mutableImage)
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

    companion object {
        private const val IMAGE_DIRECTORY = "/CustomImage"
    }
}