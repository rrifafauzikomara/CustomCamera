package com.rifafauzi.customcamerasurfaceview.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.rifafauzi.customcamerasurfaceview.R
import com.rifafauzi.customcamerasurfaceview.adapter.TextAdapter
import com.rifafauzi.customcamerasurfaceview.model.TextModel

/**
 * A simple [Fragment] subclass.
 */
class GalleryFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnProcess: FrameLayout
    private lateinit var tvButton: TextView
    private lateinit var progressBar: ProgressBar

    private val textRecognitionModels = ArrayList<TextModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageView = view.findViewById(R.id.img)
        recyclerView = view.findViewById(R.id.recycler_view)
        btnProcess = view.findViewById(R.id.btnRecognition)
        tvButton = view.findViewById(R.id.btnText)
        progressBar = view.findViewById(R.id.btnProgress)

        val imageFilePath = GalleryFragmentArgs.fromBundle(arguments!!).data
        val bitmap = BitmapFactory.decodeFile(imageFilePath)
        val rotatedBitmap = bitmap.rotate(90)

        if (imageFilePath.isBlank()) {
            Log.i(
                "GalleryFragment",
                "Image is Null or Empty"
            )
        } else {
            Glide.with(activity!!)
                .load(rotatedBitmap)
                .into(imageView)
        }

        btnProcess.setOnClickListener {
            analyzeImage(rotatedBitmap)
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = TextAdapter(context!!, textRecognitionModels)

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

    private fun analyzeImage(image: Bitmap?) {
        if (image == null) {
            Toast.makeText(context, "Image is null", Toast.LENGTH_SHORT).show()
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

                imageView.setImageBitmap(mutableImage)
                hideProgress()
                recyclerView.adapter?.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(context, "There was some error", Toast.LENGTH_SHORT).show()
                hideProgress()
            }
    }


    private fun recognizeText(result: FirebaseVisionText?, image: Bitmap?) {

        if (result == null || image == null) {
            return Toast.makeText(context, "There was some error", Toast.LENGTH_SHORT).show()
        } else if (result.textBlocks.size == 0) {
            Toast.makeText(context,"Gunakan landscape mode agar teks bisa terbaca", Toast.LENGTH_SHORT).show()
            return
        }

        var index = 0
        for (block in result.textBlocks) {
            for (line in block.lines) {
                textRecognitionModels.add(TextModel(index++, line.text))
            }
        }
    }

    private fun showProgress() {
        tvButton.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        tvButton.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
    }


}
