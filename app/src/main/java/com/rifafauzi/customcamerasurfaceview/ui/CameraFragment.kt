package com.rifafauzi.customcamerasurfaceview.ui


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraX
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.rifafauzi.customcamerasurfaceview.R
import com.rifafauzi.customcamerasurfaceview.utils.FileCreator
import com.rifafauzi.customcamerasurfaceview.utils.FileCreator.JPEG_FORMAT
import com.rifafauzi.customcamerasurfaceview.utils.UseCaseConfigBuilder
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.*
import java.util.concurrent.Executors


/**
 * A simple [Fragment] subclass.
 */
class CameraFragment : Fragment() {

    private lateinit var rectangle: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rectangle = view.findViewById(R.id.rectangle)

        viewFinder.post { setupCamera() }
    }

    private fun setupCamera() {
        CameraX.unbindAll()
        CameraX.bindToLifecycle(
            this,
            buildPreviewUseCase(),
            buildImageCaptureUseCase(),
            buildImageAnalysisUseCase()
        )
    }

    private fun buildPreviewUseCase(): Preview {
        val preview = Preview(
            UseCaseConfigBuilder.buildPreviewConfig(
                viewFinder.display
            )
        )
        preview.setOnPreviewOutputUpdateListener { previewOutput ->
            updateViewFinderWithPreview(previewOutput)
            correctPreviewOutputForDisplay(previewOutput.textureSize)
        }
        return preview
    }

    private fun updateViewFinderWithPreview(previewOutput: Preview.PreviewOutput) {
        val parent = viewFinder.parent as ViewGroup
        parent.removeView(viewFinder)
        parent.addView(viewFinder, 0)
        viewFinder.surfaceTexture = previewOutput.surfaceTexture
    }

    /**
     * Corrects the camera/preview's output to the display, by scaling
     * up/down and/or rotating the camera/preview's output.
     */
    private fun correctPreviewOutputForDisplay(textureSize: Size) {
        val matrix = Matrix()

        val centerX = viewFinder.width / 2f
        val centerY = viewFinder.height / 2f

        val displayRotation = getDisplayRotation()
        val (dx, dy) = getDisplayScalingFactors(textureSize)

        matrix.postRotate(displayRotation, centerX, centerY)
        matrix.preScale(dx, dy, centerX, centerY)

        // Correct preview output to account for display rotation and scaling
        viewFinder.setTransform(matrix)
    }

    private fun getDisplayRotation(): Float {
        val rotationDegrees = when (viewFinder.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> throw IllegalStateException("Unknown display rotation ${viewFinder.display.rotation}")
        }
        return -rotationDegrees.toFloat()
    }

    /**
     * Crop a image taking a reference a view parent like a frame, and a view child like final
     * reference
     *
     * @param bitmap image to crop
     * @param frame where the image is set it
     * @param reference frame to take reference for crop the image
     * @return image already cropped
     */
    private fun cropImage(bitmap: Bitmap, frame: View, reference: View): ByteArray {
        val heightOriginal = frame.height
        val widthOriginal = frame.width
        val heightFrame = reference.height
        val widthFrame = reference.width
        val leftFrame = reference.left
        val topFrame = reference.top
        val heightReal = bitmap.height
        val widthReal = bitmap.width
        val widthFinal = widthFrame * widthReal / widthOriginal
        val heightFinal = heightFrame * heightReal / heightOriginal
        val leftFinal = leftFrame * widthReal / widthOriginal
        val topFinal = topFrame * heightReal / heightOriginal
        val bitmapFinal = Bitmap.createBitmap(
            bitmap,
            leftFinal, topFinal, widthFinal, heightFinal
        )
        val stream = ByteArrayOutputStream()
        bitmapFinal.compress(
            Bitmap.CompressFormat.JPEG,
            100,
            stream
        ) //100 is the best quality possibe
        return stream.toByteArray()
    }

    // Save the image cropped
    private fun saveImage(bytes: ByteArray) : String {
        val outStream: FileOutputStream
        val fileName = "KTP" + System.currentTimeMillis() + ".jpg"
        val directoryName = File(Environment.getExternalStorageDirectory().toString() + IMAGE_DIRECTORY)
        val file = File(directoryName, fileName)

        if (!directoryName.exists()) {
            directoryName.mkdirs()
        }

        try {
            file.createNewFile()
            outStream = FileOutputStream(file)
            outStream.write(bytes)
            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.path),
                arrayOf("image/jpeg"), null
            )
            outStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file.absolutePath
    }


    // Rotate the image from Landscape to Portrait
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

    private fun getDisplayScalingFactors(textureSize: Size): Pair<Float, Float> {
        val cameraPreviewRation = textureSize.height / textureSize.width.toFloat()
        val scaledWidth: Int
        val scaledHeight: Int
        if (viewFinder.width > viewFinder.height) {
            scaledHeight = viewFinder.width
            scaledWidth = (viewFinder.width * cameraPreviewRation).toInt()
        } else {
            scaledHeight = viewFinder.height
            scaledWidth = (viewFinder.height * cameraPreviewRation).toInt()
        }
        val dx = scaledWidth / viewFinder.width.toFloat()
        val dy = scaledHeight / viewFinder.height.toFloat()
        return Pair(dx, dy)
    }

    private fun buildImageCaptureUseCase(): ImageCapture {
        val capture = ImageCapture(
            UseCaseConfigBuilder.buildImageCaptureConfig(
                viewFinder.display
            )
        )
        cameraCaptureImageButton.setOnClickListener {
            capture.takePicture(
                FileCreator.createTempFile(JPEG_FORMAT),
                Executors.newSingleThreadExecutor(),
                object : ImageCapture.OnImageSavedListener {
                    override fun onImageSaved(file: File) {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        val rotatedBitmap = bitmap.rotate(90)
                        val croppedImage = cropImage(rotatedBitmap, viewFinder, rectangle)
                        val path = saveImage(croppedImage) //<-- Image cropped is save in device
                        requireActivity().runOnUiThread {
                            launchGalleryFragment(path)
                        }
                    }

                    override fun onError(
                        imageCaptureError: ImageCapture.ImageCaptureError,
                        message: String,
                        cause: Throwable?
                    ) {
                        Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_LONG)
                            .show()
                        Log.e("CameraFragment", "Capture error $imageCaptureError: $message", cause)
                    }
                })
        }
        return capture
    }

    private fun buildImageAnalysisUseCase(): ImageAnalysis {
        val analysis = ImageAnalysis(
            UseCaseConfigBuilder.buildImageAnalysisConfig(
                viewFinder.display
            )
        )
        analysis.setAnalyzer(
            Executors.newSingleThreadExecutor(),
            ImageAnalysis.Analyzer { image, rotationDegrees ->
                Log.d(
                    "CameraFragment",
                    "Image analysis: $image - Rotation degrees: $rotationDegrees"
                )
            })
        return analysis
    }

    private fun launchGalleryFragment(path: String) {
        val action = CameraFragmentDirections.actionLaunchGalleryFragment(path)
        findNavController().navigate(action)
    }

    companion object {
        private const val IMAGE_DIRECTORY = "/CustomImage"
    }

}
