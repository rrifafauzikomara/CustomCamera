package com.rifafauzi.customcamerasurfaceview.ui


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rifafauzi.customcamerasurfaceview.R

/**
 * A simple [Fragment] subclass.
 */
class GalleryFragment : Fragment() {

    private lateinit var image: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnProcess: FrameLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        image = view.findViewById(R.id.img)
        recyclerView = view.findViewById(R.id.recycler_view)
        btnProcess = view.findViewById(R.id.btnRecognition)

        val imageFilePath = GalleryFragmentArgs.fromBundle(arguments!!).data

        if (imageFilePath.isBlank()) {
            Log.i(
                "GalleryFragment",
                "Image is Null or Empty"
            )
        } else {
            Glide.with(activity!!)
                .load(imageFilePath)
                .into(image)
        }

    }

}
