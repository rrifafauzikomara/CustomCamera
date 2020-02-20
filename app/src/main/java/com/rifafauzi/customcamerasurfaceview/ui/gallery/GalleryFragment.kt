package com.rifafauzi.customcamerasurfaceview.ui.gallery

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.text.isDigitsOnly
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.rifafauzi.customcamerasurfaceview.R
import com.rifafauzi.customcamerasurfaceview.databinding.FragmentGalleryBinding
import com.rifafauzi.customcamerasurfaceview.model.KTPModel
import com.rifafauzi.customcamerasurfaceview.common.ResultState

/**
 * A simple [Fragment] subclass.
 */
class GalleryFragment : Fragment() {

    private lateinit var binding: FragmentGalleryBinding
    private lateinit var image: String

    private val vm: GalleryViewModel by lazy {
        ViewModelProvider(this).get(GalleryViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_gallery, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val safeArgs =
                GalleryFragmentArgs.fromBundle(
                    it
                )
            image = safeArgs.data
        }

        val bitmap = BitmapFactory.decodeFile(image)

        activity?.let {
            Glide.with(it)
                .load(bitmap)
                .into(binding.img)
        }

        vm.nik.observe(viewLifecycleOwner, Observer {
            it?.let {
                when (it) {
                    is ResultState.Loading -> {
                        showProgress()
                        hideContent()
                        hideNIK()
                    }
                    is ResultState.HasData -> {
                        hideProgress()
                        hideNIK()
                        showContent()
                        displayData(it.data)
                    }
                    is ResultState.NoData -> {
                        hideProgress()
                        hideContent()
                        showNIK()
                        showDialog("Data tidak ditemukan")
                    }
                    is ResultState.NoInternetConnection -> {
                        hideProgress()
                        hideContent()
                        showNIK()
                        message(getString(R.string.no_internet_connection))
                    }
                    is ResultState.Timeout -> {
                        hideProgress()
                        hideContent()
                        showNIK()
                        message(getString(R.string.timeout))
                    }
                    is ResultState.Error -> {
                        hideProgress()
                        hideContent()
                        showNIK()
                        message(getString(R.string.unknown_error))
                    }
                }
            }
        })

        analyzeImage(bitmap)

    }

    private fun displayData(data: List<KTPModel>) {
        binding.model = data[0]
    }

    private fun analyzeImage(image: Bitmap?) {
        if (image == null) {
            Toast.makeText(context, "Image is null", Toast.LENGTH_SHORT).show()
            return
        }

        showProgress()
        hideNIK()

        val firebaseVisionImage = FirebaseVisionImage.fromBitmap(image)
        val textRecognizer = FirebaseVision.getInstance().onDeviceTextRecognizer
        textRecognizer.processImage(firebaseVisionImage)
            .addOnSuccessListener {

                val mutableImage = image.copy(Bitmap.Config.ARGB_8888, true)

                recognizeText(it, mutableImage)

                binding.img.setImageBitmap(mutableImage)
                hideProgress()
                showNIK()
            }
            .addOnFailureListener {
                Toast.makeText(context, "There was some error", Toast.LENGTH_SHORT).show()
                hideProgress()
                showNIK()
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
            if (replace != "") {
                res.add(replace)
            }
        }

        if (res.size <= 2) {
            showDialog("NIK tidak terbaca, silahkan ketik manual")
        } else {
            if (res[2].isDigitsOnly()) {
                hideNIK()
                vm.getListKTP(res[2].toLong())
            } else {
                showDialog("NIK tidak terbaca, silahkan ketik manual")
                binding.etNIK.setText(res[2])
                binding.etNIK.addTextChangedListener(object : TextWatcher {

                    override fun afterTextChanged(s: Editable) {

                    }

                    override fun beforeTextChanged(s: CharSequence, start: Int,
                                                   count: Int, after: Int) {
                    }

                    override fun onTextChanged(s: CharSequence, start: Int,
                                               before: Int, count: Int) {
                        if (s.length == 16) {
                            vm.getListKTP(s.toString().toLong())
                        }
                    }
                })
            }
        }

    }

    private fun showProgress() {
        binding.showLoading = true
    }

    private fun showContent() {
        binding.showContent = true
    }

    private fun hideContent() {
        binding.showContent = false
    }

    private fun hideProgress() {
        binding.showLoading = false
    }

    private fun message(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    private fun showNIK() {
        binding.showNIK = true
    }

    private fun hideNIK() {
        binding.showNIK = false
    }

    private fun showDialog(message: String) {
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .show()
    }


}
