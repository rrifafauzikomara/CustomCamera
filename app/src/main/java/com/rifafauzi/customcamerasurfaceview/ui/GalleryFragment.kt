package com.rifafauzi.customcamerasurfaceview.ui


import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.Navigation
import com.rifafauzi.customcamerasurfaceview.R

/**
 * A simple [Fragment] subclass.
 */
class GalleryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_gallery, container, false)
        return ImageView(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageFilePath = arguments?.getString(KEY_IMAGE_FILE_PATH)
        if (imageFilePath.isNullOrBlank()) {
            Navigation.findNavController(requireActivity(), R.id.mainContent).popBackStack()
        } else {
            (view as ImageView).setImageURI(Uri.parse(imageFilePath))
        }
    }

    companion object {
        private const val KEY_IMAGE_FILE_PATH = "key_image_file_path"
        fun arguments(absolutePath: String): Bundle {
            return Bundle().apply { putString(KEY_IMAGE_FILE_PATH, absolutePath) }
        }
    }

}
