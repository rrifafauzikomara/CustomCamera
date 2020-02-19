package com.rifafauzi.customcamerasurfaceview.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
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
        progressBar = view.findViewById(R.id.btnProgress)

        arguments?.let {
            val safeArgs = GalleryFragmentArgs.fromBundle(it)
            image = safeArgs.data
        }

        val bitmap = BitmapFactory.decodeFile(image)

        activity?.let {
            Glide.with(it)
                .load(bitmap)
                .into(imageView)
        }

        analyzeImage(bitmap)

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

        val res = mutableListOf<String>()

        if (result == null || image == null) {
            return Toast.makeText(context, "There was some error", Toast.LENGTH_SHORT).show()
        } else if (result.textBlocks.size == 0) {
            Toast.makeText(context,"Gunakan potrait mode agar teks bisa terbaca", Toast.LENGTH_SHORT).show()
            return
        }

        val words = result.text.split("\n")
        for (word in words) {
            val stringRegex = word.replace(
                Regex(
                    """(?i)Gol\. Darah|NIK|Nama|Tempat/Tgl Lahir|Jenis kelamin|Alamat|RT/RW|Kel/Desa|Kecamatan|Agama|Status Perkawinan|Pekerjaan|Kewarganegaraan|Berlaku Hingga|Gol. Darah|TempatTglLahir|TempatTgl Lahir|Gol Darah|RTRW|Kewarganegataan|NZK|Tempat glahir|Jenis keiamn|GoL Daah|Tempat Tglahit|Jenis keiami|Gol Daah|Tempet TglLahir|Jenis kelemin|GoL Da ah|RIRW|KelOes|TempatTgiLahir|Jenis keilamis|Alamal|Goi Dazah|RT/BW|Kel/Oesa|Tempat/ TglLahir|Tempat/TglLahir|R1/RW|R1RW|Tempat Tgl Lahir|GolDarah|KelDesa|Tempat/Tgi Lahir|KevDesa|Aiamat|Ke/Desa|Tempat TglLahir|KeiDesaa"""),
                ""
            )
            val replace = stringRegex.replace(":", "")
//                .replace("Gol. Darah", "")
//                .replace("TempatTglLahir", "")
//                .replace("TempatTgl Lahir", "")
//                .replace("Gol Darah", "")
//                .replace("RTRW", "")
//                .replace("Kewarganegataan", "")
//                .replace("NZK","")
//                .replace("Tempat glahir", "")
//                .replace("Jenis keiamn","")
//                .replace("GoL Daah","")
//                .replace("Tempat Tglahit","")
//                .replace("Jenis keiami","")
//                .replace("Gol Daah", "")
            if (replace != "") {
                res.add(replace)
            }
        }

        if (res.size < 16) {
            Toast.makeText(context, "Gambar tidak sesuai", Toast.LENGTH_SHORT).show()
        } else {
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

    }

    private fun showProgress() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        progressBar.visibility = View.GONE
    }


}
