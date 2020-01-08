package com.rifafauzi.customcamerasurfaceview.ui

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.rifafauzi.customcamerasurfaceview.R
import kotlinx.android.synthetic.main.fragment_gallery.*


/**
 * A simple [Fragment] subclass.
 */
class GalleryFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var btnProcess: FrameLayout
    private lateinit var tvButton: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var image: String

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
        btnProcess = view.findViewById(R.id.btnRecognition)
        tvButton = view.findViewById(R.id.btnText)
        progressBar = view.findViewById(R.id.btnProgress)

        arguments?.let {
            val safeArgs = GalleryFragmentArgs.fromBundle(it)
            image = safeArgs.data
        }

        val charset = Charsets.UTF_8
        val byteArray = image.toByteArray(charset)
        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

        Glide.with(activity!!)
            .load(bitmap)
            .into(imageView)

        btnProcess.setOnClickListener {
            analyzeImage(bitmap)
        }

    }

    private fun analyzeImage(image: Bitmap?) {
        if (image == null) {
            Toast.makeText(context, "Image is null", Toast.LENGTH_SHORT).show()
            return
        }

        showProgress()

        val firebaseVisionImage = FirebaseVisionImage.fromBitmap(image)
        val textRecognizer = FirebaseVision.getInstance().onDeviceTextRecognizer
        textRecognizer.processImage(firebaseVisionImage)
            .addOnSuccessListener {

                val mutableImage = image.copy(Bitmap.Config.ARGB_8888, true)

                recognizeText(it, mutableImage)

                imageView.setImageBitmap(mutableImage)
                hideProgress()
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

        val lines = result.text.split("\n")
        val res = mutableListOf<String>()
        for (line in lines) {
            val stringRegex = line.replace(Regex("""(?i)Gol\. Darah|NIK|Nama|Tempat/Tgl Lahir|Jenis kelamin|Alamat|RT/RW|Kel/Desa|Kecamatan|Agama|Status Perkawinan|Pekerjaan|Kewarganegaraan|Berlaku Hingga|Gol. Darah|TempatTglLahir|TempatTgl Lahir|Gol Darah|RTRW|Kewarganegataan"""), "")
            val replace = stringRegex.replace(":", "")
            Log.e("TAG AFTER REGEX", replace)
            if (replace != "") {
                res.add(replace)
            }
        }
        tvProvinsi.text = res[0]
        tvKota.text = res[1]
        tvNIK.text = res[2]
        tvNama.text = res[3]
        tvTgl.text = res[4]
        tvGender.text = res[5]
        tvGol.text = res[6]
        tvAddress.text = res[7]
        tvRTRW.text = res[8]
        tvKelDes.text = res[9]
        tvKec.text = res[10]
        tvAgama.text = res[11]
        tvStatus.text = res[12]
        tvJob.text = res[13]
        tvWNI.text = res[14]
        tvExpired.text = res[15]

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
