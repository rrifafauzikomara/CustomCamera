package com.rifafauzi.customcamerasurfaceview.ui

import android.graphics.*
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
import kotlinx.android.synthetic.main.fragment_gallery.*

/**
 * A simple [Fragment] subclass.
 */
class GalleryFragment : Fragment() {

    private lateinit var imageView: ImageView
//    private lateinit var recyclerView: RecyclerView
    private lateinit var btnProcess: FrameLayout
    private lateinit var tvButton: TextView
    private lateinit var progressBar: ProgressBar

//    private val textRecognitionModels = ArrayList<TextModel>()

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
//        recyclerView = view.findViewById(R.id.recycler_view)
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

//        recyclerView.layoutManager = LinearLayoutManager(context)
//        recyclerView.adapter = TextAdapter(context!!, textRecognitionModels)

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

//        textRecognitionModels.clear()
//        recyclerView.adapter?.notifyDataSetChanged()
        showProgress()

        val firebaseVisionImage = FirebaseVisionImage.fromBitmap(image)
        val textRecognizer = FirebaseVision.getInstance().onDeviceTextRecognizer
        textRecognizer.processImage(firebaseVisionImage)
            .addOnSuccessListener {

                val mutableImage = image.copy(Bitmap.Config.ARGB_8888, true)

                recognizeText(it, mutableImage)

                imageView.setImageBitmap(mutableImage)
                hideProgress()
//                recyclerView.adapter?.notifyDataSetChanged()
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
            Toast.makeText(context,"Gunakan potrait mode agar teks bisa terbaca", Toast.LENGTH_SHORT).show()
            return
        }

        //test
        val words = result.text.split("\n")
        val res = mutableListOf<String>()
        Log.e("TAG LIST STRING", words.toString())
        for (word in words) {
            Log.e("TAG STRING", word)
//            word.matches(Regex("gol. darah|nik|kewarganegaraan|nama|status perkawinan|berlaku hingga|alamat|agama|tempat/tgl lahir|jenis kelamin|gol darah|rt/rw|kel|desa|kecamatan"))
            word.replace(":","")
            if (word.contains("NIK")) {
                word.replace("NIK", "")
            }

            if (word != "") {
                res.add(word)
            }
            Log.e("TAG RES", res.toString())

//            //REGEX for detecting a NIK
//            if (word.replace(" ", "").matches(Regex("^(?:4[0-9]{12}(?:[0-9]{3})?|[25][1-7][0-9]{14}|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|(?:2131|1800|35\\d{3})\\d{11})\$")))
//                tvNIK.text = word
//            //Find a better way to do this
//            if (word.contains("/")) {
//                for (year in word.split(" ")) {
//                    if (year.contains("/"))
//                        tvCardExpiry.text = year
//                }
//            }
        }
        Log.e("TAG JADI", res.toString())
        tvProvinsi.text = res[0]
        tvKota.text = res[1]
        tvNIK.text = res[2]
//        tvNama.text = res[3]
//        tvTgl.text = res[4]
        //tets

//        var index = 0
//        for (block in result.textBlocks) {
//            for (line in block.lines) {
//                textRecognitionModels.add(TextModel(index++, line.text))
//            }
//        }
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
